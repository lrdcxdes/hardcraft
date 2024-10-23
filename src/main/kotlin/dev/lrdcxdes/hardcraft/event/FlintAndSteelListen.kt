package dev.lrdcxdes.hardcraft.event

import org.bukkit.Material
import org.bukkit.block.data.type.Campfire
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class FlintAndSteelListen : Listener {
    @EventHandler
    fun onFlintAndSteelUse(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            if (event.item != null) {
                if (event.item!!.type == Material.FLINT_AND_STEEL) {
                    if (event.clickedBlock != null) {
                        if (event.clickedBlock!!.type == Material.CAMPFIRE) {
                            event.isCancelled = true

                            val meta = event.item!!.itemMeta as org.bukkit.inventory.meta.Damageable
                            meta.damage += 1
                            event.item!!.itemMeta = meta

                            val data = event.clickedBlock!!.blockData as Campfire
                            data.isLit = true
                            event.clickedBlock!!.blockData = data
                        }
                    }
                }
            }
        }
    }
}