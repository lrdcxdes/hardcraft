package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NewFoodListen : Listener {
    @EventHandler
    fun onTryEat(event: PlayerInteractEvent) {
        // check if cacao beans
        if (event.item?.type == Material.COCOA_BEANS) {
            if (event.player.foodLevel < 20 && event.player.saturation < 5 && event.player.exhaustion < 4) {
                // cancel event
                event.isCancelled = true

                event.item!!.amount -= 1

                // play eat sound
                event.player.playSound(event.player.location, "minecraft:entity.generic.eat", 1.0f, 1.0f)

                // set food level to 20
                event.player.foodLevel += 1
            }
        } else if (event.item?.type?.name?.contains("SEEDS") == true) {
            // 20% chance to eat
            if (event.player.foodLevel < 20 && event.player.saturation < 5 && event.player.exhaustion < 4) {
                event.item!!.amount -= 1
                event.player.playSound(event.player.location, "minecraft:entity.generic.eat", 1.0f, 1.0f)
                if (Hardcraft.instance.random.nextInt(100) < 20) {
                    event.player.foodLevel += 1
                } else {
                    event.player.addPotionEffect(PotionEffect(PotionEffectType.NAUSEA, 5 * 20, 0))
                }
            }
        }
    }
}