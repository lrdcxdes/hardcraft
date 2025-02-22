package dev.lrdcxdes.hardcraft.nms.mobs

import dev.lrdcxdes.hardcraft.Hardcraft
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.animal.AgeableWaterCreature
import net.minecraft.world.entity.animal.Squid
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.entity.CraftSquid
import org.bukkit.entity.EntityType
import org.bukkit.entity.Ghast
import org.bukkit.persistence.PersistentDataType
import java.util.*
import org.bukkit.entity.Entity as BukkitEntity


class CustomSquid {
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
                squid.move(MoverType.SELF, Vec3(direction.x, direction.y, direction.z))
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
            ghast.getAttribute(Attribute.MAX_HEALTH)?.baseValue = 40.0
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
//            squid.removeAllGoals { true }
//
//            squid.goalSelector.addGoal(2, SquidRandomMovementGoal(squid))
//            squid.goalSelector.addGoal(3, SquidFleeGoal(squid))
            val goal1 = squid.goalSelector.availableGoals.first()
            val goal2 = squid.goalSelector.availableGoals.last()
            squid.goalSelector.removeGoal(goal1)
            squid.goalSelector.removeGoal(goal2)
            squid.goalSelector.addGoal(2, goal1)
            squid.goalSelector.addGoal(3, goal2)
            squid.goalSelector.addGoal(1, SquidMoveToPlayerGoal(squid))
            squid.goalSelector.addGoal(0, SquidBossRandomGoal(squid))
        }
    }
}