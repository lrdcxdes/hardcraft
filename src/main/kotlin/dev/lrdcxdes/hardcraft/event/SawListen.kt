package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.customtables.Saw
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class SawListen : Listener {
    @EventHandler
    fun onSaw(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        val item = event.item ?: return
        // check for saw
        if (item.type != Material.STONE_AXE) return
        val meta = item.itemMeta
        if (!meta.hasCustomModelData()) return
        val modelData = meta.customModelData
        if (modelData != 5 && modelData != 4) return

        // if saw
        Saw.open(event.player)
    }
}