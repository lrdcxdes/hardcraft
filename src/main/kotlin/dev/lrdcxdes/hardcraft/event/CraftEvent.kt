package dev.lrdcxdes.hardcraft.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class CraftEvent: Listener {
    @EventHandler
    fun onDurability(event: PrepareItemCraftEvent) {
        if (event.isRepair) {
            event.inventory.result = null
        }
    }
}