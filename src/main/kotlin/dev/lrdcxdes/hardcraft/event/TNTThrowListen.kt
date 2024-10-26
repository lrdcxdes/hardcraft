package dev.lrdcxdes.hardcraft.event

import org.bukkit.Material
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class TNTThrowListen : Listener {
    private val dinamites = mapOf(
        3 to ItemStack(Material.GUNPOWDER, 1).apply {
            itemMeta = itemMeta.apply {
                setCustomModelData(3)
            }
        },
        4 to ItemStack(Material.GUNPOWDER, 1).apply {
            itemMeta = itemMeta.apply {
                setCustomModelData(4)
            }
        }
    )

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR) {
            // throw any item in hand
            val item = event.item ?: return
            if (item.type == Material.GUNPOWDER && item.itemMeta.customModelData in 3..4) {
                val modelData = item.itemMeta.customModelData
                val dinamite = dinamites[modelData]
                if (dinamite != null) {
                    val player = event.player
                    val snowball = player.launchProjectile(Snowball::class.java)
                    snowball.item = dinamite
                    val x = if (modelData == 4) 1.0 else 2.0
                    snowball.velocity = player.location.direction.multiply(x)
                    item.amount--
                }
            }
        }
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val entity = event.entity
        if (entity is Snowball) {
            val item = entity.item
            if (item.type == Material.GUNPOWDER && item.itemMeta.customModelData in 3..4) {
                val world = entity.world
                val location = entity.location
                val cmd = item.itemMeta.customModelData
                val power = if (cmd == 3) 1.7f else 3.0f
                world.createExplosion(location, power, false, true)
                entity.remove()
            }
        }
    }
}