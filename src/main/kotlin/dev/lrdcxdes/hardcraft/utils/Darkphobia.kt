package dev.lrdcxdes.hardcraft.utils

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.block.BlockFace
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Darkphobia {
    private val players = mutableMapOf<String, Double>()
    private val soundTimers = mutableMapOf<String, Int>()

    private val task = object : BukkitRunnable() {
        override fun run() {
            for (player in Hardcraft.instance.server.onlinePlayers) {
                var state = players[player.name] ?: 0.0

                val lightLevel = player.eyeLocation.block.lightLevel
                if (lightLevel < 6) {
                    state += 1.0
                } else {
                    state -= 4.0
                }

                // max state is 500.0
                // 120+ = slow speed 20%
                // 180+ = sound noises
                // 300+ = darkness effect
                // 480+ = hunger effect

                if (state > 500.0) {
                    state = 500.0
                } else if (state < 0.0) {
                    state = 0.0
                }

                if (state >= 480.0) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, 40, 0))
                }

                if (state >= 300.0) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 0))
                }

                if (state >= 180.0) {
                    val timer = soundTimers[player.name] ?: 0
                    if (timer == 0) {
                        soundTimers[player.name] = 1
                        player.playSound(player.location, "minecraft:ambient.cave", 1.0f, 0.5f)
                    } else {
                        // every 30 seconds
                        if (timer % 30 == 0) {
                            soundTimers[player.name] = timer + 1
                            // random minecraft:ambient.cave, minecraft:block.{player_block}.place, minecraft:block.{player_block}.break, minecraft:entity.player.attack.nodamage, minecraft:entity.creeper.primed
                            val sounds = arrayOf(
                                "minecraft:ambient.cave",
                                "minecraft:block.stone.place",
                                "minecraft:block.stone.break",
                                "minecraft:block.stone.step",
                                "minecraft:entity.player.attack.nodamage",
                                "minecraft:entity.creeper.primed"
                            )
                            // if its place/break/step/attack then can be multiple times in a row random count
                            val sound = sounds[Hardcraft.instance.random.nextInt(sounds.size)]
                            if (sound.contains("place") || sound.contains("break") || sound.contains("step") || sound.contains(
                                    "attack"
                                )
                            ) {
                                val randomLoc = player.location.clone().add(
                                    Hardcraft.instance.random.nextInt(10) - 5.0,
                                    Hardcraft.instance.random.nextInt(10) - 5.0,
                                    Hardcraft.instance.random.nextInt(10) - 5.0
                                )
                                val count = Hardcraft.instance.random.nextInt(3) + 1
                                for (i in 0 until count) {
                                    player.playSound(randomLoc, sound, 1.0f, 1.0f)
                                }
                            } else if (sound.contains("cave")) {
                                player.playSound(player.location, sound, 1.0f, 0.5f)
                            } else {
                                // behind player
                                val loc = player.location.clone().add(player.location.direction.multiply(-1))
                                player.playSound(loc, sound, 1.0f, 1.0f)
                            }
                        }
                    }
                }

                if (state >= 120.0) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 0))
                }

                if (state < 180.0) {
                    soundTimers.remove(player.name)
                } else {
                    soundTimers[player.name] = soundTimers[player.name]!!.plus(1)
                }

                players[player.name] = state
            }
        }
    }.runTaskTimer(Hardcraft.instance, 0, 20L)
}