package dev.lrdcxdes.hardcraft.nms.mobs

import dev.lrdcxdes.hardcraft.Hardcraft
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.animal.Squid
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.entity.CraftSquid
import org.bukkit.entity.EntityType
import org.bukkit.entity.Ghast
import org.bukkit.persistence.PersistentDataType
import java.util.function.Predicate
import org.bukkit.entity.Entity as BukkitEntity

class CustomSquid {
    class SquidFleeGoal(private val squid: Squid) : Goal() {
        private var fleeTicks = 0

        override fun canUse(): Boolean {
            val livingEntity: LivingEntity? = squid.getLastHurtByMob()
            return squid.isInWater && livingEntity != null && squid.distanceToSqr(livingEntity) < 100.0
        }

        override fun start() {
            this.fleeTicks = 0
        }

        override fun requiresUpdateEveryTick(): Boolean {
            return true
        }

        override fun tick() {
            fleeTicks++
            val livingEntity: LivingEntity? = squid.getLastHurtByMob()
            if (livingEntity != null) {
                var vec3 = Vec3(
                    squid.x - livingEntity.x,
                    squid.y - livingEntity.y,
                    squid.z - livingEntity.z
                )
                val blockState: BlockState = squid.level()
                    .getBlockState(
                        BlockPos.containing(
                            squid.x + vec3.x,
                            squid.y + vec3.y,
                            squid.z + vec3.z
                        )
                    )
                val fluidState: FluidState = squid.level()
                    .getFluidState(
                        BlockPos.containing(
                            squid.x + vec3.x,
                            squid.y + vec3.y,
                            squid.z + vec3.z
                        )
                    )
                if (fluidState.`is`(FluidTags.WATER) || blockState.isAir) {
                    val d = vec3.length()
                    if (d > 0.0) {
                        vec3.normalize()
                        var e = 3.0
                        if (d > 5.0) {
                            e -= (d - 5.0) / 5.0
                        }

                        if (e > 0.0) {
                            vec3 = vec3.scale(e)
                        }
                    }

                    if (blockState.isAir) {
                        vec3 = vec3.subtract(0.0, vec3.y, 0.0)
                    }

                    squid.setMovementVector(
                        vec3.x.toFloat() / 20.0f,
                        vec3.y.toFloat() / 20.0f,
                        vec3.z.toFloat() / 20.0f
                    )
                }

                if (this.fleeTicks % 10 == 5) {
                    squid.level().addParticle(
                        ParticleTypes.BUBBLE,
                        squid.x,
                        squid.y,
                        squid.z, 0.0, 0.0, 0.0
                    )
                }
            }
        }

        companion object {
            private const val SQUID_FLEE_SPEED = 3.0f
            private const val SQUID_FLEE_MIN_DISTANCE = 5.0f
            private const val SQUID_FLEE_MAX_DISTANCE = 10.0f
        }
    }

    class SquidRandomMovementGoal(private val squid: Squid) : Goal() {
        override fun canUse(): Boolean {
            return true
        }

        override fun tick() {
            val i = squid.noActionTime
            if (i > 100) {
                squid.setMovementVector(0.0f, 0.0f, 0.0f)
            } else if (squid.getRandom()
                    .nextInt(reducedTickDelay(50)) == 0 || !squid.wasTouchingWater || !squid.hasMovementVector()
            ) {
                val f = squid.getRandom().nextFloat() * (Math.PI * 2).toFloat()
                val g = Mth.cos(f) * 0.2f
                val h = -0.1f + squid.getRandom().nextFloat() * 0.2f
                val j = Mth.sin(f) * 0.2f
                squid.setMovementVector(g, h, j)
            }
        }
    }

    class SquidMoveToPlayerGoal(private val squid: Squid) : Goal() {
        private var targetPlayer: Player? = null

        override fun canUse(): Boolean {
            val level: Level = squid.level()
            val isNight = !level.isDay
            val isRaining = level.isRaining
            if (isNight || isRaining) {
                // findNearbyBukkitPlayers(Double, Double, Double, Double, Predicate<Entity!>?)
                // only if player is swimming or in water
                val nearestPlayer = level.findNearbyBukkitPlayers(
                    squid.x,
                    squid.y,
                    squid.z,
                    20.0
                ) { it.isInWater }.firstOrNull()
                if (nearestPlayer != null) {
                    targetPlayer = (nearestPlayer as CraftPlayer).handle as Player
                    return true
                }
            }
            return false
        }

        override fun tick() {
            targetPlayer?.let {
                val direction = it.position().subtract(squid.position()).normalize()
                squid.setMovementVector(
                    direction.x.toFloat() * 0.1f,
                    direction.y.toFloat() * 0.1f,
                    direction.z.toFloat() * 0.1f
                )
            }
        }
    }

    class SquidBossRandomGoal(private val squid: Squid) : Goal() {
        private var alrUsed = false
        private val CHANCE = 5

        override fun canUse(): Boolean {
            val level: Level = squid.level()
            val isYes = !alrUsed && level.isThundering
            if (isYes) {
                val isGood = squid.random.nextInt(100) < CHANCE
                if (isGood) {
                    return true
                } else {
                    alrUsed = true
                    squid.goalSelector.removeGoal(this)
                }
            }
            return false
        }

        private fun spawnBossSquid(location: Location) {
            val ghast = location.world.spawnEntity(location, EntityType.GHAST) as Ghast

            // GENERIC_MAX_HEALTH
            ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 40.0
            ghast.health = 40.0

            // custom drop
            val key = NamespacedKey(Hardcraft.instance, "boss_squid")
            ghast.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, true)
        }

        override fun tick() {
            if (alrUsed) return
            alrUsed = true
            val location = squid.bukkitEntity.location
            location.world.strikeLightningEffect(location)
            squid.bukkitEntity.remove()
            spawnBossSquid(location)
        }
    }

    companion object {
        fun setGoals(ent: BukkitEntity) {
            val squid = (ent as CraftSquid).handle
            squid.removeAllGoals { true }

            squid.goalSelector.addGoal(2, SquidRandomMovementGoal(squid))
            squid.goalSelector.addGoal(3, SquidFleeGoal(squid))
            squid.goalSelector.addGoal(1, SquidMoveToPlayerGoal(squid))
            squid.goalSelector.addGoal(0, SquidBossRandomGoal(squid))
        }
    }
}