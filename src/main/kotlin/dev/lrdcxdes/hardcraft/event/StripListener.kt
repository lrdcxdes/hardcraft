package dev.lrdcxdes.hardcraft.event

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


class StripListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onStrip(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val clickedBlock: Block? = event.clickedBlock
        if (clickedBlock != null) {
            if (!clickedBlock.type.name.startsWith("STRIPPED_")) return
            if (!isAxe(event.item)) return

            clickedBlock.type = Material.CRAFTING_TABLE
            event.player.playSound(clickedBlock.location, "minecraft:item.axe.strip", 1.0f, 1.0f)
        }
    }

    companion object {
        private fun isAxe(item: ItemStack?): Boolean {
            if (item == null) return false
            if (item.amount == 0) return false
            return item.type.name.endsWith("_AXE")
        }
    }
}