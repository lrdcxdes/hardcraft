package dev.lrdcxdes.hardcraft.weather

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent


class WeatherListener(private val plugin: Hardcraft) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        // Initial weather setup for joining players will be handled by the regular update task
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Clean up any player-specific data if needed
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        val player = event.player
        // Reset to default when changing worlds
        player.resetPlayerWeather()
        player.resetPlayerTime()
    }
}
