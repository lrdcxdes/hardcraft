package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.races.Race
import dev.lrdcxdes.hardcraft.races.getRace
import dev.lrdcxdes.hardcraft.seasons.getTemperature
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EatFoodListener : Listener {
    private fun addPotionEffect(player: Player, type: PotionEffectType, duration: Int, amplifier: Int = 0) {
        player.addPotionEffect(PotionEffect(type, 20 * duration, amplifier, false, false))
    }

    private val meat = setOf(
        Material.SALMON,
        Material.COD,
        Material.MUTTON,
        Material.CHICKEN,
        Material.BEEF,
        Material.PORKCHOP,
        Material.RABBIT,
        Material.ROTTEN_FLESH,
        Material.SPIDER_EYE,
        Material.PUFFERFISH,
        Material.COOKED_SALMON,
        Material.COOKED_COD,
        Material.COOKED_MUTTON,
        Material.COOKED_CHICKEN,
        Material.COOKED_BEEF,
        Material.COOKED_PORKCHOP,
        Material.COOKED_RABBIT,
        Material.COOKED_SALMON
    )

    private val crops = setOf(
        Material.POTATO,
        Material.CARROT,
        Material.BEETROOT,
        Material.MELON_SLICE,
        Material.PUMPKIN_PIE,
        Material.APPLE,
        Material.GOLDEN_APPLE,
        Material.ENCHANTED_GOLDEN_APPLE,
        Material.SWEET_BERRIES,
        Material.GLOW_BERRIES,
        Material.CHORUS_FRUIT,
        Material.BREAD,
        Material.DRIED_KELP
    )

    private val zazaEffects = listOf(
        PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 0),
        PotionEffect(PotionEffectType.LUCK, 60 * 20, 0),
        PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 20, 0),
        PotionEffect(PotionEffectType.STRENGTH, 20 * 20, 0),
        PotionEffect(PotionEffectType.SPEED, 20 * 20, 0),
        PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 20, 0),
        PotionEffect(PotionEffectType.HASTE, 20 * 20, 0),
    )

    @EventHandler
    fun onSkeletonFood(event: FoodLevelChangeEvent) {
        val player = event.entity as Player
        val race = player.getRace()
        if (race == Race.SKELETON) {
            if (event.foodLevel < 4) {
                event.isCancelled = true
            }
        } else if (race == Race.GIANT) {
            val nowFoodLevel = player.foodLevel
            if (event.foodLevel > nowFoodLevel) {
                val addedFoodLevel = event.foodLevel - nowFoodLevel
                // -50% food level
                event.foodLevel = (nowFoodLevel + addedFoodLevel / 2).coerceAtMost(20)
            }
        }
    }

    @EventHandler
    fun onEat(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item.type
        val random = Hardcraft.instance.random.nextInt(100)

        val race = player.getRace()
        if (race == Race.ELF) {
            // if any meat
            if (meat.contains(item)) {
                event.isCancelled = true
                player.sendMessage("§cElves can't eat meat")
                return
            }
        } else if (race == Race.GOBLIN || race == Race.DRAGONBORN) {
            // if any crops
            if (crops.contains(item)) {
                event.isCancelled = true
                player.sendMessage("§cGoblins can't eat crops")
                return
            }
        } else if (race == Race.SKELETON) {
            // if anything but bone
            if (item != Material.BONE) {
                event.isCancelled = true
                player.sendMessage("§cSkeletons can only eat bones")
                return
            } else {
                // add 2 health
                player.health = (player.health + 2).coerceAtMost(player.getAttribute(Attribute.MAX_HEALTH)!!.value)
                return
            }
        }

        if (item.name.contains("SEEDS")) {
            val r = Hardcraft.instance.random.nextInt(100)
            // 20% nausea, 30% poison
            if (r < 20) {
                event.player.addPotionEffect(PotionEffect(PotionEffectType.NAUSEA, 5 * 20, 0))
            } else if (r < 50) {
                event.player.addPotionEffect(PotionEffect(PotionEffectType.POISON, 5 * 20, 0))
            }
            return
        }
        // if (event.item?.type == Material.FLOWER_BANNER_PATTERN && event.item?.itemMeta?.customModelData == 3) {
        if (item == Material.FLOWER_BANNER_PATTERN && event.item.itemMeta?.customModelData == 3) {
            val loc = event.player.eyeLocation.add(event.player.location.direction.multiply(0.1))

            event.player.playSound(
                loc,
                "minecraft:entity.blaze.ambient",
                0.2f,
                2.0f
            )

            // smoke effect
            loc.world.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, loc, 10, 0.1, 0.1, 0.1, 0.1)

            // random zaza effect
            val effect = zazaEffects[Hardcraft.instance.random.nextInt(zazaEffects.size)]
            event.player.addPotionEffect(effect)

            event.player.foodLevel = (event.player.foodLevel - 3).coerceAtLeast(0)

            // random if 3%
            if (Hardcraft.instance.random.nextInt(100) < 3) {
                event.player.playSound(event.player.location, "minecraft:music_disc.stal", 0.6f, 1.5f)
            }
            return
        }

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
