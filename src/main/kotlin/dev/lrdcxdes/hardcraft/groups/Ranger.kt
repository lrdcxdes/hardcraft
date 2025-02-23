package dev.lrdcxdes.hardcraft.groups

import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent

class Ranger: Listener {
    // Ranger
    //Уникальные способности
    //При успешном попадании стрелой, возвращает стрелу обратно.

    @EventHandler
    fun onHit(event: ProjectileHitEvent) {
        val player = event.entity.shooter as? Player ?: return
        if (event.hitEntity != null && (event.entityType == EntityType.ARROW || event.entityType == EntityType.SPECTRAL_ARROW) && player.getGroup() == Group.RANGER) {
            // Вернуть стрелу обратно
            event.entity.remove()
            // player.playSound()
            val arrow = (event.entity as AbstractArrow).itemStack
            val map = player.inventory.addItem(arrow)

            for ((_, value) in map) {
                player.world.dropItem(player.location, value)
            }
        }
    }
}