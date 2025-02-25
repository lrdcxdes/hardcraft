package dev.lrdcxdes.hardcraft.races

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class Snolem : Listener {
    private val lastSnowGolem = mutableMapOf<String, Long>()

    @EventHandler
    fun onUse(event: PlayerInteractEvent) {
        // use 10 exp points and 3 hunger bars (6 fp) to summon snow golem
        val player = event.player
        val race = player.getRace() ?: return
        if (race != Race.SNOLEM) return
        if (lastSnowGolem[player.name] != null && System.currentTimeMillis() - lastSnowGolem[player.name]!! < 5000) {
            return
        }
        if (player.isSneaking && event.action.isRightClick && event.item?.type?.isBlock != true) {
            if (player.totalExperience >= 15 && player.foodLevel >= 6) {
                player.totalExperience -= 15
                player.foodLevel -= 6
                player.world.spawnEntity(player.location, EntityType.SNOW_GOLEM)
                player.world.playSound(
                    player.location,
                    "minecraft:block.snow_step",
                    1.0f,
                    1.0f
                )

                lastSnowGolem[player.name] = System.currentTimeMillis()
            }
        }
    }
}