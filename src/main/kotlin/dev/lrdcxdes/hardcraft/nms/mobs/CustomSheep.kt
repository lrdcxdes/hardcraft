package dev.lrdcxdes.hardcraft.nms.mobs

import dev.lrdcxdes.hardcraft.nms.mobs.CustomCow.Companion
import dev.lrdcxdes.hardcraft.nms.mobs.goals.PoopGoal
import dev.lrdcxdes.hardcraft.nms.mobs.goals.RaidGardenGoal
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.ItemTags
import net.minecraft.util.Mth
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.NeutralMob
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal
import net.minecraft.world.entity.animal.Sheep
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.CropBlock
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.*
import kotlin.math.max

class CustomSheep(world: ServerLevel, private val isFriendly: Boolean = false) : Sheep(EntityType.SHEEP, world),
    NeutralMob {
    private val attributes: AttributeMap = AttributeMap(createAttributes().build())
    private val PERSISTENT_ANGER_TIME = UniformInt.of(20, 39)
    private var remainingAngerTime = 0
    private var persistentAngerTarget: UUID? = null
    private var eatBlockGoal: EatBlockGoal? = null
    private var eatAnimationTick = 0

    override fun registerGoals() {
        this.eatBlockGoal = EatBlockGoal(this)
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(0, PoopGoal(this, 6000..12000))

        if (isFriendly) {
            goalSelector.addGoal(1, PanicGoal(this, 1.25))
        } else {
            this.goalSelector.addGoal(3, MeleeAttackGoal(this, 1.0, true))
            this.targetSelector.addGoal(1, (HurtByTargetGoal(this, *arrayOfNulls(0))).setAlertOthers())
            this.targetSelector.addGoal(2, NearestAttackableTargetGoal(
                this, Player::class.java, 10, true, false
            ) { entity: LivingEntity ->
                this.isAngryAt(entity)
            })
            this.targetSelector.addGoal(3, ResetUniversalAngerTargetGoal(this, true))
        }

        goalSelector.addGoal(2, BreedGoal(this, 1.0))
        goalSelector.addGoal(
            4, TemptGoal(
                this, 1.1,
                { itemstack: ItemStack -> itemstack.`is`(ItemTags.SHEEP_FOOD) }, false
            )
        )
        goalSelector.addGoal(5, FollowParentGoal(this, 1.1))
        goalSelector.addGoal(5, this.eatBlockGoal!!)
        goalSelector.addGoal(5, RaidGardenGoal(this, listOf(CropBlock::class.java)))
        goalSelector.addGoal(6, WaterAvoidingRandomStrollGoal(this, 1.0))
        goalSelector.addGoal(
            7, LookAtPlayerGoal(
                this,
                Player::class.java, 6.0f
            )
        )
        goalSelector.addGoal(8, RandomLookAroundGoal(this))
    }

    fun spawn(loc: org.bukkit.Location) {
        this.moveTo(loc.x, loc.y, loc.z)
        this.persist = true
        (loc.world as CraftWorld).handle.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    override fun aiStep() {
        super.aiStep()
        if (!level().isClientSide) {
            this.updatePersistentAnger(level() as ServerLevel, true)
            this.eatAnimationTick = max(0.0, (this.eatAnimationTick - 1).toDouble()).toInt()
        }
    }

    override fun handleEntityEvent(status: Byte) {
        if (status.toInt() == 10) {
            this.eatAnimationTick = 40
        } else {
            super.handleEntityEvent(status)
        }
    }

    override fun getHeadEatPositionScale(delta: Float): Float {
        return if (this.eatAnimationTick <= 0) 0.0f else (if (this.eatAnimationTick in 4..36) 1.0f else (if (this.eatAnimationTick < 4) (eatAnimationTick.toFloat() - delta) / 4.0f else -((this.eatAnimationTick - 40).toFloat() - delta) / 4.0f))
    }

    override fun getHeadEatAngleScale(delta: Float): Float {
        if (this.eatAnimationTick in 5..36) {
            val f1 = ((this.eatAnimationTick - 4).toFloat() - delta) / 32.0f

            return 0.62831855f + 0.21991149f * Mth.sin(f1 * 28.7f)
        } else {
            return if (this.eatAnimationTick > 0) 0.62831855f else this.xRot * 0.017453292f
        }
    }

    override fun getRemainingPersistentAngerTime(): Int {
        return this.remainingAngerTime
    }

    override fun setRemainingPersistentAngerTime(angerTime: Int) {
        this.remainingAngerTime = angerTime
    }

    override fun getPersistentAngerTarget(): UUID? {
        return this.persistentAngerTarget
    }

    override fun setPersistentAngerTarget(angryAt: UUID?) {
        this.persistentAngerTarget = angryAt
    }

    override fun startPersistentAngerTimer() {
        this.remainingPersistentAngerTime = PERSISTENT_ANGER_TIME.sample(this.random)
    }

    override fun getAttributes(): AttributeMap {
        return attributes
    }

    private var moreFoodTicks = 0

    fun wantsMoreFood(): Boolean {
        return this.moreFoodTicks <= 0
    }

    fun setMoreFoodTicks(ticks: Int) {
        this.moreFoodTicks = ticks
    }

    override fun customServerAiStep() {
        if (this.moreFoodTicks > 0) {
            this.moreFoodTicks -= random.nextInt(3)
            if (this.moreFoodTicks < 0) {
                this.moreFoodTicks = 0
            }
        }

        if (this.getAge() != 0) {
            this.inLove = 0
        }
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23000000417232513)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
        }
    }
}