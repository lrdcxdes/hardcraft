package dev.lrdcxdes.hardcraft.races

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable

class GoblinListener(private val plugin: Hardcraft): Listener {
    // Regeneration: Self-heal 1 HP every 60 seconds
    //Group Synergy: +5% movement speed per nearby Goblin (max bonus up to +50%, i.e. multiplier up to 0.15000000223)
    private val goblinsMaxHP = RaceManager.getAttributes(Race.GOBLIN)!!.baseAttributes[Attribute.MAX_HEALTH]!!
    private val goblinsSpeed = RaceManager.getDefaultAttributes().baseAttributes[Attribute.MOVEMENT_SPEED]!!

    private val taskRegeneration = object: BukkitRunnable() {
        override fun run() {
            for (player in plugin.server.onlinePlayers) {
                if (player.getRace() != Race.GOBLIN) return
                player.heal(2.0)
            }
        }
    }

    private val taskGroupSynergy = object : BukkitRunnable() {
        override fun run() {
            plugin.server.onlinePlayers.forEach { player ->
                val race = player.getRace()
                if (race == Race.GOBLIN) {
                    updateSpeedBonus(player)
                }
            }
        }
    }

    // Counts nearby goblin players (within a given radius) and returns bonus up to 50%.
    private fun calculateGoblinBonus(player: Player): Double {
        val radius = 10.0  // Example radius; adjust as needed.
        val nearbyGoblinCount = player.location.getNearbyEntities(radius, radius, radius)
            .filterIsInstance<Player>()
            .count { it != player && it.getRace() == Race.GOBLIN }
        val bonus = 0.05 * nearbyGoblinCount
        return bonus.coerceAtMost(0.5)
    }

    private fun updateSpeedBonus(player: Player) {
        val bonusPercentage = calculateGoblinBonus(player)
        val attribute = player.getAttribute(Attribute.MOVEMENT_SPEED) ?: return

        // Calculate bonus as a fraction of the base speed.
        val baseSpeed = goblinsSpeed
        val newSpeed = baseSpeed + baseSpeed * bonusPercentage
        attribute.baseValue = newSpeed
    }

    init {
        taskRegeneration.runTaskTimer(plugin, 0, 20L * 60L)
        taskGroupSynergy.runTaskTimer(plugin, 0, 20L * 5L)
    }
}