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
        } else if (event.item?.type == Material.FLOWER_BANNER_PATTERN && event.item?.itemMeta?.customModelData == 3) {
            event.item!!.amount -= 1
            // /playsound minecraft:entity.blaze.ambient master lrdcxdes 0.2 2.0
            // +0.5 block right in front of your eyes
            event.player.playSound(
                event.player.eyeLocation.add(event.player.location.direction.multiply(0.1)).block.location,
                "minecraft:entity.blaze.ambient",
                0.2f,
                2.0f
            )

            event.player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 0))
            event.player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 60 * 20, 0))
            event.player.addPotionEffect(PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 20, 0))

            // random if 3%
            if (Hardcraft.instance.random.nextInt(100) < 3) {
                event.player.playSound(event.player.location, "minecraft:music_disc.stal", 0.6f, 1.5f)
            }

            event.player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 0))
            event.player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20 * 20, 0))
            event.player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 20, 0))
        }
    }
}