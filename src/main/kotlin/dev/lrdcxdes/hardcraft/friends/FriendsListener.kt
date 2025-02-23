package dev.lrdcxdes.hardcraft.friends

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scoreboard.Team

class FriendsListener: Listener {
    private val scoreboardManager by lazy { Hardcraft.instance.server.scoreboardManager.mainScoreboard }
    private val incognitoTeam: Team by lazy {
        scoreboardManager.getTeam("incognito") ?: scoreboardManager.registerNewTeam("incognito").apply {
            setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        updateIncognitoTeam(event.player)
    }

    private fun updateIncognitoTeam(player: Player) {
        incognitoTeam.addEntry(player.name)
    }
}