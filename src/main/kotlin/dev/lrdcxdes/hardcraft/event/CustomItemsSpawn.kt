package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class CustomItemsSpawn : Listener {
    private val itemCheckedKey = Hardcraft.instance.key("itemChecked")

    @EventHandler
    fun onItemSpawn(event: ItemSpawnEvent) {
        val item = event.entity.itemStack

        val meta = item.itemMeta ?: return
        if (meta.persistentDataContainer.has(itemCheckedKey, PersistentDataType.BOOLEAN)) return

        when (item.type) {
            Material.APPLE -> {
                val random = listOf(0, 3, 4).random()
                meta.setCustomModelData(random)
            }

            Material.DIAMOND -> {
                // replace to amethyst_shard
                val amethystShard = ItemStack(Material.AMETHYST_SHARD, item.amount)
                meta.persistentDataContainer.set(itemCheckedKey, PersistentDataType.BOOLEAN, true)
                amethystShard.itemMeta = meta
                event.entity.itemStack = amethystShard
                return
            }

            Material.REDSTONE -> {
                // 50% chance to replace to redstone_dust
                if (Math.random() < 0.5) {
                    event.entity.itemStack.amount = 0
                    return
                }
            }

            else -> return
        }

        meta.persistentDataContainer.set(itemCheckedKey, PersistentDataType.BOOLEAN, true)
        item.itemMeta = meta
    }
}