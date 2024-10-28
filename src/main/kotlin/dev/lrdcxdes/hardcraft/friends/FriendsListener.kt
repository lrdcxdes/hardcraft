package dev.lrdcxdes.hardcraft.friends

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scoreboard.Team
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class FriendsListener(
    private val friendsManager: FriendsManager
) : Listener {
    private val interactionCooldown: LoadingCache<UUID, Boolean> = CacheBuilder.newBuilder()
        .expireAfterWrite(200, TimeUnit.MILLISECONDS)
        .build(CacheLoader.from { _ -> true })

    private val teamCache = ConcurrentHashMap<String, Team>()
    private val scoreboardManager by lazy { Hardcraft.instance.server.scoreboardManager.mainScoreboard }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val damaged = event.entity as? Player ?: return

        val damagerUuid = damager.uniqueId
        val damagedUuid = damaged.uniqueId

        when {
            friendsManager.areFriends(damagerUuid.toString(), damagedUuid.toString()) -> {
                event.isCancelled = true
            }
            friendsManager.pendingRequests[damagedUuid.toString()] == damagerUuid.toString() -> {
                event.isCancelled = true
                val deniedMessage = Hardcraft.minimessage.deserialize("<lang:friends.request-denied:${damaged.name}>")
                damager.sendMessage(deniedMessage)
                damaged.sendMessage(deniedMessage)
                friendsManager.pendingRequests.remove(damagerUuid.toString())
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractAtEntityEvent) {
        val clickedPlayer = event.rightClicked as? Player ?: return
        val player = event.player

        if (!player.isSneaking || clickedPlayer == player) return

        if (interactionCooldown.getIfPresent(player.uniqueId) == true) return
        interactionCooldown.put(player.uniqueId, true)

        handleFriendInteraction(player, clickedPlayer)
    }

    private fun handleFriendInteraction(player: Player, clickedPlayer: Player) {
        val playerUuid = player.uniqueId.toString()
        val clickedPlayerUuid = clickedPlayer.uniqueId.toString()

        when {
            friendsManager.areFriends(playerUuid, clickedPlayerUuid) -> {
                player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.already-friends:${clickedPlayer.name}>"))
            }
            friendsManager.pendingRequests[clickedPlayerUuid] == playerUuid -> {
                friendsManager.processFriendRequest(clickedPlayer, player)
            }
            else -> {
                friendsManager.sendFriendRequest(playerUuid, clickedPlayerUuid)
                player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.request-sent:${clickedPlayer.name}>"))
                clickedPlayer.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.request-received:${player.name}>"))
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        updateTeamsForPlayer(event.player)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        // Only update teams if the player has actually moved to a new block
        if (event.to?.block?.location == event.from.block?.location) return
        updateTeamsForNearbyPlayers(event.player)
    }

    private fun updateTeamsForPlayer(player: Player) {
        val playerUuid = player.uniqueId.toString()
        var hasFriends = false

        Hardcraft.instance.server.onlinePlayers
            .filterNot { it == player }
            .forEach { otherPlayer ->
                val otherPlayerUuid = otherPlayer.uniqueId.toString()
                if (friendsManager.areFriends(playerUuid, otherPlayerUuid)) {
                    addPlayerToTeam(player, otherPlayer)
                    hasFriends = true
                }
            }

        updateIncognitoTeam(player, hasFriends)
    }

    private fun updateTeamsForNearbyPlayers(player: Player) {
        val playerUuid = player.uniqueId.toString()
        var hasFriends = false

        // Only check players within render distance
        player.world.getNearbyPlayers(player.location, 48.0)
            .filterNot { it == player }
            .forEach { otherPlayer ->
                val otherPlayerUuid = otherPlayer.uniqueId.toString()
                if (friendsManager.areFriends(playerUuid, otherPlayerUuid)) {
                    addPlayerToTeam(player, otherPlayer)
                    hasFriends = true
                }
            }

        updateIncognitoTeam(player, hasFriends)
    }

    private fun updateIncognitoTeam(player: Player, hasFriends: Boolean) {
        val incognitoTeamName = "friends_${player.uniqueId}"
        if (!hasFriends) {
            scoreboardManager.getTeam(incognitoTeamName)?.let { team ->
                team.addEntry(player.name)
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
            } ?: scoreboardManager.registerNewTeam(incognitoTeamName).apply {
                addEntry(player.name)
                setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
            }
        } else {
            scoreboardManager.getTeam(incognitoTeamName)?.unregister()
        }
    }

    private fun getOrCreateTeam(player: Player, friend: Player): Team {
        val baseTeamName = "friends_${player.uniqueId}_${friend.uniqueId}"
        val alternateTeamName = "friends_${friend.uniqueId}_${player.uniqueId}"

        return teamCache.computeIfAbsent(baseTeamName) {
            scoreboardManager.getTeam(baseTeamName)
                ?: scoreboardManager.getTeam(alternateTeamName)
                ?: scoreboardManager.registerNewTeam(baseTeamName).apply {
                    setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
                }
        }
    }

    private fun addPlayerToTeam(player: Player, friend: Player) {
        val team = getOrCreateTeam(player, friend)
        team.addEntry(friend.name)
        team.addEntry(player.name)
    }

    fun removePlayerFromTeam(player: Player, friend: Player) {
        val baseTeamName = "friends_${player.uniqueId}_${friend.uniqueId}"
        val alternateTeamName = "friends_${friend.uniqueId}_${player.uniqueId}"

        teamCache.remove(baseTeamName)
        teamCache.remove(alternateTeamName)

        scoreboardManager.getTeam(baseTeamName)?.let { team ->
            team.removeEntry(friend.name)
            team.removeEntry(player.name)
            team.unregister()
        } ?: scoreboardManager.getTeam(alternateTeamName)?.let { team ->
            team.removeEntry(friend.name)
            team.removeEntry(player.name)
            team.unregister()
        }
    }
}