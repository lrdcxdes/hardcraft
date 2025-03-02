package dev.lrdcxdes.hardcraft.event

import conditionSystem
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class QuitListener: Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        // Remove player from the condition system
        conditionSystem.playerQuit(event.player)
    }
}