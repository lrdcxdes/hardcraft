package dev.lrdcxdes.hardcraft.raids

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Vex
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.sqrt

class Guardian(
    world: ServerLevel,
    private val guardRange: Float,
    private val location: Location,
    ownerId: UUID,
    index: Int
) :
    Vex(EntityType.VEX, world) {

    val UNIQUE_NAME = "${ownerId}_$index"

    override fun getAttributes(): AttributeMap {
        return AttributeMap(createAttributes(guardRange.toDouble()).build())
    }

    override fun move(movementType: MoverType, movement: Vec3) {
        this.setPos(this.x, this.y, this.z)
    }

    private class AttackGoal(
        private var mob: Guardian,
        private var attackIntervalMin: Int,
        private var attackIntervalMax: Int,
        attackRadius: Float
    ) :
        Goal() {
        private var target: LivingEntity? = null
        private var seeTime = 0
        private var attackTime = -1
        private var attackRadiusSqr = attackRadius * attackRadius

        init {
            println("attackRadius: $attackRadius")
            println("attackRadiusSqr: $attackRadiusSqr")
            this.setFlags(EnumSet.of(Flag.LOOK))
        }

        override fun canUse(): Boolean {
            val livingEntity = mob.target
            if (livingEntity != null && livingEntity.isAlive) {
                this.target = livingEntity
                return true
            } else {
                return false
            }
        }

        override fun canContinueToUse(): Boolean {
            return this.canUse() || target!!.isAlive && mob.distanceToSqr(
                target!!.x,
                target!!.y, target!!.z
            ) <= attackRadiusSqr
        }

        override fun stop() {
            this.target = null
            this.seeTime = 0
            this.attackTime = -1
        }

        override fun requiresUpdateEveryTick(): Boolean {
            return true
        }

        override fun tick() {
            val d = target?.let {
                mob.distanceToSqr(
                    it.x,
                    it.y, it.z
                )
            } ?: run {
                return
            }
            val bl = this.target?.let { mob.sensing.hasLineOfSight(it) }
            if (bl == true) {
                seeTime++
            } else {
                this.seeTime = 0
            }

            if (d > attackRadiusSqr || seeTime == 0) {
                return
            } else if (this.target != null) {
                val entityliving = this.target!!
                val d0: Double = 0.1
                var d1: Double = entityliving.x - this.mob.x
                var d2: Double = entityliving.getY(0.5) - this.mob.eyeY
                var d3: Double = entityliving.z - this.mob.z
                val d4 = sqrt(d1 * d1 + d2 * d2 + d3 * d3)

                d1 /= d4
                d2 /= d4
                d3 /= d4
                var d5: Double = this.mob.random.nextDouble()

                while (d5 < d4) {
                    d5 += 1.8 - d0 + this.mob.random.nextDouble() * (1.7 - d0)
                    this.mob.level().addParticle(
                        ParticleTypes.BUBBLE,
                        this.mob.x + d1 * d5,
                        this.mob.eyeY + d2 * d5,
                        this.mob.z + d3 * d5, 0.0, 0.0, 0.0
                    )
                }
            }

            this.target?.let { mob.lookControl.setLookAt(it, 30.0f, 30.0f) }
            if (this.attackTime <= 0) {
                if (bl == false) {
                    return
                }

                this.target?.let { mob.doHurtTarget(it) }
                this.attackTime = mob.random.nextInt(attackIntervalMax - attackIntervalMin) + attackIntervalMin
            } else {
                --this.attackTime
            }
        }
    }


    override fun registerGoals() {
//        goalSelector.addGoal(0, FloatGoal(this))
//        goalSelector.addGoal(2, GuardAttackGoal())
        println("attack range1: $guardRange")
        goalSelector.addGoal(
            2,
            AttackGoal(
                this,
                39, 40, attackRadius = guardRange
            )
        )
        println("attack range2: $guardRange")
        goalSelector.addGoal(
            3, LookAtPlayerGoal(
                this,
                Player::class.java, guardRange, 1.0f
            )
        )
        targetSelector.addGoal(
            1, NearestAttackableTargetGoal(
                this,
                Player::class.java, false, true
            )
        )
    }

    private var noRegenTicks: Int = 20

    override fun tick() {
        super.tick()
        if (!this.isRemoved && !this.dead) {
            if (this.health < this.maxHealth) {
                if (noRegenTicks > 0) {
                    noRegenTicks--
                } else {
                    this.health += 1.0f.coerceAtMost(this.maxHealth - this.health)
                    updateHealth()
                    noRegenTicks = 20
                }
            }
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val result = super.hurt(source, amount)
        updateHealth()
        return result
    }

    override fun die(damageSource: DamageSource) {
        super.die(damageSource)
        Hardcraft.removeRegion(this)
    }

    fun spawn() {
        this.setPosRaw(location.x, location.y, location.z)
        this.persist = true
        this.isSilent = true
        this.getBukkitEntity().persistentDataContainer.set(
            KEY,
            PersistentDataType.STRING,
            UNIQUE_NAME
        )
        (location.world as CraftWorld).handle.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    private val miniMessage = MiniMessage.miniMessage()

    private fun updateHealth() {
        if (this.dead || this.isRemoved) {
            return
        }
        if (this.health < this.maxHealth) {
            this.isCustomNameVisible = true
            val healthStr = String.format("%.2f", this.health)
            this.bukkitEntity.customName(miniMessage.deserialize("$healthStr <color:#ff0000>❤</color>"))
        } else {
            this.isCustomNameVisible = false
        }
    }

    companion object {
        val KEY = NamespacedKey(Hardcraft.instance, "guardian")

        private fun createAttributes(range: Double): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, range)
        }
    }
}