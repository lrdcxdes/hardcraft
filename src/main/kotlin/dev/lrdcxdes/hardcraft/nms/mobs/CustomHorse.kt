package dev.lrdcxdes.hardcraft.nms.mobs

import dev.lrdcxdes.hardcraft.nms.mobs.CustomCow.Companion
import dev.lrdcxdes.hardcraft.nms.mobs.goals.PoopGoal
import dev.lrdcxdes.hardcraft.nms.mobs.goals.RaidGardenGoal
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.ItemTags
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
import net.minecraft.world.entity.animal.horse.AbstractHorse
import net.minecraft.world.entity.animal.horse.Horse
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.CropBlock
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.*

class CustomHorse(world: ServerLevel, private val isFriendly: Boolean = false) :
    Horse(EntityType.HORSE, world),
    NeutralMob {

    private val PERSISTENT_ANGER_TIME = UniformInt.of(20, 39)
    private var remainingAngerTime = 0
    private var persistentAngerTarget: UUID? = null

    override fun registerGoals() {
        goalSelector.addGoal(0, PoopGoal(this, 6000..12000))
        if (isFriendly) {
            goalSelector.addGoal(1, PanicGoal(this, 1.2))
            goalSelector.addGoal(1, RunAroundLikeCrazyGoal(this, 1.2))
        } else {
            this.goalSelector.addGoal(3, MeleeAttackGoal(this, 1.0, true))
            this.targetSelector.addGoal(1, (HurtByTargetGoal(this, *arrayOfNulls(0))).setAlertOthers())
            this.targetSelector.addGoal(2, NearestAttackableTargetGoal(
                this, Player::class.java, 10, true, false
            ) { entity: LivingEntity, server: ServerLevel ->
                this.isAngryAt(entity, server)
            })
            this.targetSelector.addGoal(3, ResetUniversalAngerTargetGoal(this, true))
        }
        goalSelector.addGoal(5, BreedGoal(this, 1.0, AbstractHorse::class.java))
        goalSelector.addGoal(6, FollowParentGoal(this, 1.0))
        goalSelector.addGoal(7, RaidGardenGoal(this, listOf(CropBlock::class.java)))
        goalSelector.addGoal(8, WaterAvoidingRandomStrollGoal(this, 0.7))
        goalSelector.addGoal(
            9, LookAtPlayerGoal(
                this,
                Player::class.java, 6.0f
            )
        )
        goalSelector.addGoal(10, RandomLookAroundGoal(this))
        if (this.canPerformRearing()) {
            goalSelector.addGoal(11, RandomStandGoal(this))
        }

        this.addBehaviourGoals()
    }

    override fun addBehaviourGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(
            4, TemptGoal(
                this, 1.25,
                { itemstack: ItemStack -> itemstack.`is`(ItemTags.HORSE_TEMPT_ITEMS) }, false
            )
        )
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
        return AttributeStore.getAttributes("horse")
    }

    private var moreFoodTicks = 0

    fun wantsMoreFood(): Boolean {
        return this.moreFoodTicks <= 0
    }

    fun setMoreFoodTicks(ticks: Int) {
        this.moreFoodTicks = ticks
    }

    override fun customServerAiStep(level: ServerLevel) {
        super.customServerAiStep(level)

        if (this.moreFoodTicks > 0) {
            this.moreFoodTicks -= random.nextInt(3)
            if (this.moreFoodTicks < 0) {
                this.moreFoodTicks = 0
            }
        }
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createAnimalAttributes()
                .add(Attributes.JUMP_STRENGTH, 0.7)
                .add(Attributes.MAX_HEALTH, 53.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22499999403953552)
                .add(
                    Attributes.STEP_HEIGHT, 1.0
                ).add(Attributes.SAFE_FALL_DISTANCE, 6.0)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
        }
    }
}