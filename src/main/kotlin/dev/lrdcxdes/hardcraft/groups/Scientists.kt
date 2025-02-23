package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.scheduler.BukkitRunnable

class Scientists(private val plugin: Hardcraft): Listener {
    // Каждые 60 секунд получает 5 XP.
    // Вместо смерти, теряет минимум 30 уровней и полностью восстанавливает здоровье.

    private val minuteXPtask = object : BukkitRunnable() {
        override fun run() {
            for (player in plugin.server.onlinePlayers) {
                if (player.getGroup() == Group.SCIENTIST) {
                    player.giveExp(5)
                }
            }
        }
    }

    init {
        minuteXPtask.runTaskTimer(plugin, 0, 1200)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        if (player.level < 30) return
        if (player.getGroup() == Group.SCIENTIST) {
            event.isCancelled = true
            player.level -= 30
            player.world.strikeLightningEffect(player.location)
            player.health = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        }
    }
}