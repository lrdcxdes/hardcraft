package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.seasons.getTemperature
import dev.lrdcxdes.hardcraft.seasons.getTemperatureAsync
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class PlayerTemperatureListener : Listener {
    // -45 -- -17 🟥🟥
    //-16 -- -1 🟥
    //-0 -- +7 🟧
    //+8 -- +16 🟩
    //+17 -- +25 🟩🟩
    //+26 -- +32 🟧
    //+33 -- +39 🟥
    //+40 -- +45 🟥🟥

    //🟥🟥 - super bad
    //🟥 - bad
    //🟧 - 50% bad
    //🟩 - good
    //🟩🟩 - super good

    private val lastDamages: MutableMap<String, Long> = mutableMapOf()

    init {
        object : BukkitRunnable() {
            override fun run() {
                for (player in Hardcraft.instance.server.onlinePlayers) {
                    player.getTemperatureAsync { temp ->
                        var tempTemp = temp
                        // calculate and also include if player has leather armor on them
                        // Полный комплект кожаной брони может добавлять около 5-10°C к допустимым температурам. Например, если без брони игрок начинает получать урон при -10°C, то в кожаной броне он выдержит до -15 или -20°C.

                        val helmet = player.inventory.helmet
                        if (helmet != null) {
                            tempTemp += (if (helmet.type.name.contains("LEATHER")) 3 else 2)
                        }
                        val chestplate = player.inventory.chestplate
                        if (chestplate != null) {
                            tempTemp += (if (chestplate.type.name.contains("LEATHER")) 6 else 2)
                        }
                        val leggings = player.inventory.leggings
                        if (leggings != null) {
                            tempTemp += (if (leggings.type.name.contains("LEATHER")) 5 else 2)
                        }
                        val boots = player.inventory.boots
                        if (boots != null) {
                            tempTemp += (if (boots.type.name.contains("LEATHER")) 6 else 1)
                        }

                        // if player is in a biome with a temperature that is too high or too low
                        // then player will start taking damage
                        if (tempTemp < 0) {
                            player.freezeTicks = 200
                        }

                        object : BukkitRunnable() {
                            override fun run() {
                                if (tempTemp >= 33) {
                                    player.addPotionEffect(PotionEffect(PotionEffectType.NAUSEA, 200, 0))
                                }

                                if (tempTemp >= 40) {
                                    player.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 200, 0))
                                }
                            }
                        }.runTask(Hardcraft.instance)

                        val uuid = player.uniqueId.toString()
                        if (tempTemp < -20) {
                            if (lastDamages.containsKey(uuid)) {
                                val lastDamage = lastDamages[uuid] ?: 0
                                if (System.currentTimeMillis() - lastDamage > 1000) {
                                    player.damage(1.0)
                                    lastDamages[uuid] = System.currentTimeMillis()
                                }
                            } else {
                                player.damage(1.0)
                                lastDamages[uuid] = System.currentTimeMillis()
                            }
                        } else if (tempTemp < -10) {
                            if (lastDamages.containsKey(uuid)) {
                                val lastDamage = lastDamages[uuid] ?: 0
                                if (System.currentTimeMillis() - lastDamage > 1000) {
                                    player.damage(0.5)
                                    lastDamages[uuid] = System.currentTimeMillis()
                                }
                            } else {
                                lastDamages[uuid] = System.currentTimeMillis()
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(Hardcraft.instance, 0, 20L * 5)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity is Player) {
            if (event.cause == EntityDamageEvent.DamageCause.FREEZE) {
                event.isCancelled = true
            }
        }
    }
}