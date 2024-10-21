package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class EntityDropListener : Listener {
    val ghastBossKey = NamespacedKey(Hardcraft.instance, "boss_squid")
    val ghastDrop: List<ItemStack>
        get() {
            val boosDrop = mutableListOf<ItemStack>()
            boosDrop.add(ItemStack(Material.INK_SAC, (1..9).random()))
            boosDrop.add(ItemStack(Material.GLOW_INK_SAC, (1..3).random()))
            boosDrop.add(ItemStack(Material.TURTLE_SCUTE, (1..3).random()))
            boosDrop.add(ItemStack(Material.HEART_OF_THE_SEA, 1))
            return boosDrop
        }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val drops = event.drops
        if (entity.persistentDataContainer.has(ghastBossKey, PersistentDataType.BOOLEAN)) {
            drops.clear()
            drops.addAll(ghastDrop)
        }
    }
}