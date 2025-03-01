package dev.lrdcxdes.hardcraft.groups

import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.meta.PotionMeta

class Rogue : Listener {
    //Уникальные способности
    //Может бросать любую стрелу.

    @EventHandler
    fun onThrow(event: PlayerItemConsumeEvent) {
        val group = event.player.getGroup()
        if (group == Group.ROGUE && event.item.type == Material.ARROW) {
            // Стрельнуть как из лука
            val arrow = event.player.launchProjectile(Arrow::class.java)
            arrow.shooter = event.player
            arrow.itemStack = event.item.clone().apply { amount = 1 }
            arrow.basePotionType = (event.item as? PotionMeta)?.basePotionType
            // Default arrow velocity like from bow
            // arrow.velocity = event.player.location.direction.multiply(2)
            event.player.world.playSound(
                event.player.location,
                "minecraft:entity.arrow.shoot",
                1F,
                1F
            )
        }
    }
}