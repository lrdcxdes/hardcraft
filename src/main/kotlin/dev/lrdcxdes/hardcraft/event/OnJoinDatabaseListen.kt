package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class OnJoinDatabaseListen : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (Hardcraft.database.havePlayer(event.player.uniqueId.toString())) return
        Hardcraft.database.createPlayer(event.player.uniqueId.toString(), event.player.name)
    }
}