package dev.lrdcxdes.hardcraft.nms.mobs

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
import net.minecraft.world.entity.animal.Cow
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.PotatoBlock
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.*

class CustomCow(world: ServerLevel, private val isFriendly: Boolean = false) : Cow(EntityType.COW, world), NeutralMob {
    private val PERSISTENT_ANGER_TIME = UniformInt.of(20, 39)
    private var remainingAngerTime = 0
    private var persistentAngerTarget: UUID? = null

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))

        if (isFriendly) {
            goalSelector.addGoal(1, PanicGoal(this, 2.0))
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
                this, 1.25,
                { itemstack: ItemStack -> itemstack.`is`(ItemTags.COW_FOOD) }, false
            )
        )
        goalSelector.addGoal(5, FollowParentGoal(this, 1.25))
        goalSelector.addGoal(5, RaidGardenGoal(this, listOf(CropBlock::class.java, PotatoBlock::class.java)))
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
        this.setPosRaw(loc.x, loc.y, loc.z)
        this.persist = true
        (loc.world as CraftWorld).handle.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        println("CustomCow spawned")
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
        println("Cow anger time: $angerTime")
    }

    override fun getPersistentAngerTarget(): UUID? {
        return this.persistentAngerTarget
    }

    override fun setPersistentAngerTarget(angryAt: UUID?) {
        this.persistentAngerTarget = angryAt
        println("Cow set angry at: $angryAt")
    }

    override fun startPersistentAngerTimer() {
        this.remainingPersistentAngerTime = PERSISTENT_ANGER_TIME.sample(this.random)
    }

    override fun getAttributes(): AttributeMap {
        return AttributeMap(createAttributes().build())
    }

    private var moreFoodTicks = 0

    fun wantsMoreFood(): Boolean {
        return this.moreFoodTicks <= 0
    }

    fun setMoreFoodTicks(ticks: Int) {
        this.moreFoodTicks = ticks
    }

    override fun customServerAiStep() {
        super.customServerAiStep()

        if (this.moreFoodTicks > 0) {
            this.moreFoodTicks -= random.nextInt(3)
            if (this.moreFoodTicks < 0) {
                this.moreFoodTicks = 0
            }
        }
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20000000298023224)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
        }
    }
}