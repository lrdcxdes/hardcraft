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
                    // Check if the flint&steel customModelData is 4 and then 50% chance to cancel event
                    val meta = event.item!!.itemMeta as org.bukkit.inventory.meta.Damageable
                    if (meta.hasCustomModelData() && meta.customModelData == 4) {
                        if (Math.random() < 0.5) {
                            event.isCancelled = true

                            meta.damage += 1

                            if (meta.damage >= event.item!!.type.maxDurability) {
                                event.item!!.amount -= 1
                            } else {
                                event.item!!.itemMeta = meta
                            }

                            return
                        }
                    }
                }
            }
        }
    }
}