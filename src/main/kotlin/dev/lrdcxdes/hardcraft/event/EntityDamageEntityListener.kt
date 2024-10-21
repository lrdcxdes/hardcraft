package dev.lrdcxdes.hardcraft.event

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EntityDamageEntityListener : Listener {
    @EventHandler
    fun onEntityDamageEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        val damager = event.damager

        if (damager is Player) {
            // if held item
            if (damager.inventory.itemInMainHand.type == Material.AIR) {
                val damage = event.damage
                event.damage = damage / 2
            }
        }
    }
}