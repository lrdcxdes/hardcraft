package dev.lrdcxdes.hardcraft.event

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent

class PoopThrowEvent : Listener {
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR) {
            // throw any item in hand
            val item = event.item ?: return
            if (item.type == Material.BROWN_DYE &&
                item.itemMeta.hasCustomModelData()
                && item.itemMeta.customModelData == 3
            ) {
                val player = event.player
                val snowball = player.launchProjectile(Snowball::class.java)
                snowball.item = item
                item.amount--
            }
        }
    }

    @EventHandler
    fun onHit(event: ProjectileHitEvent) {
        val s = event.entity as? Snowball ?: return
        val item = s.item
        if (item.type == Material.BROWN_DYE &&
            item.itemMeta.hasCustomModelData()
            && item.itemMeta.customModelData == 3
        ) {
            val hitLoc = event.hitBlock?.location ?: event.hitEntity?.location ?: return
            hitLoc.world.playSound(hitLoc, Sound.BLOCK_MUD_HIT, 1.0f, 1.0f)
        }
    }
}