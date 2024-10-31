package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.StonecutterInventory
import org.bukkit.inventory.meta.Damageable

class CraftEvent : Listener {
    @EventHandler
    fun onDurability(event: PrepareItemCraftEvent) {
        if (event.isRepair) {
            event.inventory.result = null
            return
        }
    }

    @EventHandler
    fun onStoneCut(event: InventoryClickEvent) {
        if (event.inventory !is StonecutterInventory) {
            return
        }

        val player = event.whoClicked

        var saw: ItemStack = player.inventory.itemInMainHand
        if (saw.type != Material.STONE_AXE) {
            saw =
                player.inventory.contents.find { it?.type == Material.STONE_AXE && it.itemMeta?.customModelData in 4..5 }
                    ?: return
        }

        if (event.rawSlot == 0) {
            // check if input is wood
            val item = event.cursor
            if (item.type == Material.AIR || item.isWood()) {
                return
            } else {
                Hardcraft.instance.logger.info("not wood, cancelling")
                event.isCancelled = true
            }
        } else if (event.rawSlot == 1) {
            if (event.currentItem == null) return

            val meta = saw.itemMeta as Damageable
            meta.damage += 1
            if (meta.damage >= saw.type.maxDurability) {
                saw.amount -= 1
            } else {
                saw.itemMeta = meta
            }
        }
    }
}

fun ItemStack.isWood(): Boolean {
    return this.type.name.endsWith("_LOG") || this.type.name.endsWith("_PLANKS")
}