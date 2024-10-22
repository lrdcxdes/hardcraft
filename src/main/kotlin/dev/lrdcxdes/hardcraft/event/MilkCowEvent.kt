package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Cow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta

class MilkCowEvent : Listener {
    @EventHandler
    fun onMilkCow(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity is Cow) {
            val item = player.inventory.itemInMainHand
            if (item.type == Material.GLASS_BOTTLE) {
                // replace to milk potion
                val potion = ItemStack(Material.POTION)
                val meta = potion.itemMeta as PotionMeta
                meta.color = Color.fromRGB(232, 232, 232)
                meta.itemName(Hardcraft.minimessage.deserialize("<lang:bts.milk_bottle>"))
                potion.itemMeta = meta

                // remove glass bottle
                item.amount -= 1

                // add milk bottle
                player.inventory.addItem(potion)
            }
        }
    }
}