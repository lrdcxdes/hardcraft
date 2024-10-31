package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.seasons.getTemperature
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EatFoodListener : Listener {
    private fun addPotionEffect(player: Player, type: PotionEffectType, duration: Int, amplifier: Int = 0) {
        player.addPotionEffect(PotionEffect(type, 20 * duration, amplifier, false, false))
    }

    @EventHandler
    fun onEat(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item.type
        val random = Hardcraft.instance.random.nextInt(100)

        when (item) {
            Material.CHORUS_FRUIT -> {
                if (random < 50) addPotionEffect(player, PotionEffectType.DARKNESS, 5)
            }

            Material.SWEET_BERRIES -> {
                applySweetBerriesEffect(player, random)
            }

            Material.GLOW_BERRIES -> {
                applyGlowBerriesEffect(player, random)
            }

            Material.BEETROOT, Material.CARROT, Material.POTATO -> {
                applyVeggieEffect(player, random)
            }

            Material.SALMON, Material.COD, Material.MUTTON, Material.CHICKEN, Material.BEEF, Material.PORKCHOP, Material.RABBIT -> {
                applyRawMeatEffect(player, random)
            }

            Material.MELON_SLICE -> {
                if (random < 50) {
                    event.isCancelled = true
                    event.item.amount -= 1
                }
            }

            Material.POTION -> {
                val meta = event.item.itemMeta as PotionMeta
                val color = meta.color?.asRGB() ?: 0
                if (color == 0xe8e8e8) {  // Milk bottle
                    if (random < 20) {
                        player.activePotionEffects.forEach {
                            player.removePotionEffect(it.type)
                        }
                    }
                } else if (color == 0x3cc221) {  // Cactus juice
                    player.foodLevel = (player.foodLevel + 2).coerceAtMost(20)
                    addPotionEffect(player, PotionEffectType.NAUSEA, 10, 0)
                }
            }

            else -> {
                // Handle other food types if needed
            }
        }

        val freshness = Hardcraft.instance.foodListener.checkItem(
            event.item,
            player.getTemperature()
        )  // 0.0 - 1.0

        // 🟩 75-100% - норм їда
        //🟧 50-74% - -50% востановлення їжї
        //🟥 25-49% - -50% востановлення їжї / отрава 2lvl на 6
        //🟫 00-24% - -100% востановлення їжї / отрава 2lvl на 6

        if (freshness < 0.25) {
            addPotionEffect(player, PotionEffectType.POISON, 20, 0)
        } else if (freshness < 0.5) {
            addPotionEffect(player, PotionEffectType.POISON, 12, 0)
        } else if (freshness < 0.75) {
            addPotionEffect(player, PotionEffectType.POISON, 6, 0)
        }
    }

    private fun applySweetBerriesEffect(player: Player, chance: Int) {
        when {
            chance < 20 -> addPotionEffect(player, PotionEffectType.REGENERATION, 5)
            chance < 60 -> addPotionEffect(player, PotionEffectType.POISON, 5)
            chance < 80 -> addPotionEffect(player, PotionEffectType.SPEED, 5)
            else -> addPotionEffect(player, PotionEffectType.SLOWNESS, 5)
        }
    }

    private fun applyGlowBerriesEffect(player: Player, chance: Int) {
        when {
            chance < 40 -> addPotionEffect(player, PotionEffectType.REGENERATION, 5)
            chance < 80 -> addPotionEffect(player, PotionEffectType.POISON, 5)
            else -> addPotionEffect(player, PotionEffectType.GLOWING, 5)
        }
    }

    private fun applyVeggieEffect(player: Player, chance: Int) {
        when {
            chance < 20 -> addPotionEffect(player, PotionEffectType.NAUSEA, 5)
            chance < 40 -> addPotionEffect(player, PotionEffectType.HUNGER, 5)
            chance < 50 -> addPotionEffect(player, PotionEffectType.POISON, 5)
            else -> {
                // No effect
            }
        }
    }

    private fun applyRawMeatEffect(player: Player, chance: Int) {
        when {
            chance < 20 -> addPotionEffect(player, PotionEffectType.NAUSEA, 5)
            chance < 40 -> addPotionEffect(player, PotionEffectType.HUNGER, 5)
            chance < 65 -> addPotionEffect(player, PotionEffectType.POISON, 5)
            else -> {
                // No effect
            }
        }
    }
}
