package dev.lrdcxdes.hardcraft.nms.mobs

import net.minecraft.server.level.ServerLevel
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.NeutralMob
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.PanicGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal
import net.minecraft.world.entity.monster.Skeleton
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.player.Player
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.*

class CustomVillager(world: ServerLevel, private val isFriendly: Boolean) : Villager(EntityType.VILLAGER, world),
    NeutralMob {
    private val PERSISTENT_ANGER_TIME = UniformInt.of(20, 39)
    private var remainingAngerTime = 0
    private var persistentAngerTarget: UUID? = null

    fun spawn(loc: org.bukkit.Location) {
        this.moveTo(loc.x, loc.y, loc.z)
        this.persist = true
        (loc.world as CraftWorld).handle.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    override fun registerGoals() {
        super.registerGoals()

        if (isFriendly) {
            goalSelector.addGoal(1, PanicGoal(this, 2.0))
        } else {
            this.goalSelector.addGoal(5, MeleeAttackGoal(this, 1.0, true))
            this.targetSelector.addGoal(1, (HurtByTargetGoal(this, *arrayOfNulls(0))).setAlertOthers())
            this.targetSelector.addGoal(2, NearestAttackableTargetGoal(
                this, Player::class.java, 10, true, false
            ) { entity: LivingEntity ->
                this.isAngryAt(entity)
            })
            this.targetSelector.addGoal(3, NearestAttackableTargetGoal(
                this, Zombie::class.java, 10, true, false
            ) { entity: LivingEntity ->
                this.isAngryAt(entity)
            })
            this.targetSelector.addGoal(4, NearestAttackableTargetGoal(
                this, Skeleton::class.java, 10, true, false
            ) { entity: LivingEntity ->
                this.isAngryAt(entity)
            })
            this.targetSelector.addGoal(5, ResetUniversalAngerTargetGoal(this, true))
        }
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
        return AttributeStore.getAttributes("villager")
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
        }
    }
}