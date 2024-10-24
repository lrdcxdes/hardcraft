package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap

// Optimized temperature effects handler
class TemperatureEffectsHandler {
    private val damageTimers = ConcurrentHashMap<String, Long>()
    private val DAMAGE_COOLDOWN = 1000L
    private val plugin = Hardcraft.instance

    fun startEffectsMonitoring() {
        object : BukkitRunnable() {
            override fun run() {
                plugin.server.onlinePlayers
                    .filter { !it.gameMode.isInvulnerable }
                    .forEach { player ->
                        handlePlayerTemperature(player)
                    }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 40L) // Run every 2 seconds
    }

    private fun handlePlayerTemperature(player: Player) {
        plugin.seasons.getTemperature(player).let { temp ->
            when {
                temp <= -7 -> player.freezeTicks = 200
                temp >= 40 -> applyEffect(player, PotionEffectType.WITHER, 200, 0)
                temp >= 33 -> applyEffect(player, PotionEffectType.NAUSEA, 201, 0)
            }

            // Handle damage
            when {
                temp < -20 -> applyDamage(player, 1.0)
                temp < -10 -> applyDamage(player, 0.5)
            }

            // 8 - +21
            // 7 - +8 -- +20
            // 6 - -7 -- +7
            // 5 - -20 -- -8
            // 4 - -21
            // 3 - (off)

            val thermometer =
                player.inventory.find { it?.type == Material.RAW_COPPER && it.itemMeta?.hasCustomModelData() == true && it.itemMeta?.customModelData in 3..8 }

            if (thermometer != null) {
                val meta = thermometer.itemMeta
                val data = when {
                    temp >= 21 -> 8
                    temp >= 8 -> 7
                    temp >= -7 -> 6
                    temp >= -20 -> 5
                    else -> 4
                }
                Hardcraft.instance.logger.info("Temperature: $temp, Data: $data")
                meta?.setCustomModelData(data)
                thermometer.itemMeta = meta
            }
        }
    }

    private fun applyEffect(player: Player, type: PotionEffectType, duration: Int, amplifier: Int) {
        object : BukkitRunnable() {
            override fun run() {
                player.addPotionEffect(PotionEffect(type, duration, amplifier))
            }
        }.runTask(plugin)
    }

    private fun applyDamage(player: Player, amount: Double) {
        val uuid = player.uniqueId.toString()
        val currentTime = System.currentTimeMillis()
        val lastDamage = damageTimers[uuid] ?: 0L

        if (currentTime - lastDamage > DAMAGE_COOLDOWN) {
            object : BukkitRunnable() {
                override fun run() {
                    player.damage(amount)
                }
            }.runTask(plugin)
            damageTimers[uuid] = currentTime
        }
    }
}

class PlayerTemperatureListener : Listener {
    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity is Player) {
            if (event.cause == EntityDamageEvent.DamageCause.FREEZE) {
                event.isCancelled = true
            }
        }
    }
}