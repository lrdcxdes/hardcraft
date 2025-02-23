package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Cleric(private val plugin: Hardcraft): Listener {
    // Каждые 40 секунд лечит все ближайшие цели на 1 HP.
    // При смерти накладывает эффект «Тьма» (30 секунд) на убийцу.

    private val minuteXPtask = object : BukkitRunnable() {
        override fun run() {
            for (player in plugin.server.onlinePlayers) {
                if (player.getGroup() == Group.CLERIC) {
                    val radius = 5.0
                    val nearbyEntities = player.location.getNearbyEntities(radius, radius, radius)
                    for (entity in nearbyEntities) {
                        val target = entity as Player
                        target.heal(2.0)
                    }
                }
            }
        }
    }

    init {
        minuteXPtask.runTaskTimer(plugin, 0, 40 * 20)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        if (player.getGroup() == Group.CLERIC) {
            val killer = player.killer
            killer?.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 600, 0))
        }
    }
}