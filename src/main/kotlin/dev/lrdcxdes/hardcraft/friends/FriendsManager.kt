package dev.lrdcxdes.hardcraft.friends

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FriendsManager {
    // Use ConcurrentHashMap for thread safety
    private val friendsCache = ConcurrentHashMap<String, MutableSet<String>>()
    val pendingRequests = ConcurrentHashMap<String, String>()

    init {
        loadFriendsIntoCache()
    }

    private fun loadFriendsIntoCache() {
        try {
            Hardcraft.database.connection.prepareStatement("SELECT player_uuid, friend_uuid FROM project9_friends")
                .use { st ->
                    st.executeQuery().use { result ->
                        while (result.next()) {
                            val playerUuid = result.getString("player_uuid")
                            val friendUuid = result.getString("friend_uuid")
                            friendsCache.computeIfAbsent(playerUuid) { ConcurrentHashMap.newKeySet() }.add(friendUuid)
                            friendsCache.computeIfAbsent(friendUuid) { ConcurrentHashMap.newKeySet() }.add(playerUuid)
                        }
                    }
                }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error loading friends: ${e.message}")
        }
    }

    fun sendFriendRequest(senderUuid: String, receiverUuid: String) {
        pendingRequests[senderUuid] = receiverUuid
    }

    fun processFriendRequest(player: Player, otherPlayer: Player) {
        val requestedBy = pendingRequests.remove(player.uniqueId.toString())
        if (requestedBy == otherPlayer.uniqueId.toString()) {
            addFriend(player.uniqueId.toString(), otherPlayer.uniqueId.toString())
            Hardcraft.instance.server.getPlayer(UUID.fromString(player.uniqueId.toString()))?.sendMessage(
                Hardcraft.minimessage.deserialize("<lang:friends.added:${otherPlayer.name}>"),
            )
            Hardcraft.instance.server.getPlayer(UUID.fromString(otherPlayer.uniqueId.toString()))?.sendMessage(
                Hardcraft.minimessage.deserialize("<lang:friends.added:${player.name}>"),
            )
        }
    }

    fun areFriends(playerUuid: String, friendUuid: String): Boolean {
        return friendsCache[playerUuid]?.contains(friendUuid) ?: false
    }

    private fun addFriend(playerUuid: String, friendUuid: String) {
        try {
            Hardcraft.database.connection.prepareStatement(
                """
                    INSERT INTO project9_friends (player_uuid, friend_uuid)
                    VALUES (?, ?)
                    """.trimIndent()
            ).use { sql ->
                sql.setString(1, playerUuid)
                sql.setString(2, friendUuid)
                sql.execute()

                friendsCache.computeIfAbsent(playerUuid) { mutableSetOf() }.add(friendUuid)
                friendsCache.computeIfAbsent(friendUuid) { mutableSetOf() }.add(playerUuid)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error adding friend: ${e.message}")
        }
    }

    fun removeFriend(playerUuid: String, friendUuid: String) {
        try {
            Hardcraft.database.connection.prepareStatement(
                """
                    DELETE FROM project9_friends
                    WHERE player_uuid = ? AND friend_uuid = ?
                    """.trimIndent()
            ).use { sql ->
                sql.setString(1, playerUuid)
                sql.setString(2, friendUuid)
                sql.execute()

                sql.setString(1, friendUuid)
                sql.setString(2, playerUuid)
                sql.execute()

                friendsCache[playerUuid]?.remove(friendUuid)
                friendsCache[friendUuid]?.remove(playerUuid)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error removing friend: ${e.message}")
        }
    }

    fun getFriends(playerUuid: String): List<String> {
        return friendsCache[playerUuid]?.toList() ?: emptyList()
    }

    fun listFriends(player: Player) {
        val friends = getFriends(player.uniqueId.toString())
        if (friends.isEmpty()) {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.no-friends>"))
            return
        }

        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.list-header>"))
        friends.forEach { friendUuid ->
            val friend = Hardcraft.instance.server.getPlayer(UUID.fromString(friendUuid))
            if (friend != null) {
                player.sendMessage(
                    Hardcraft.minimessage.deserialize("<lang:friends.command.list-item-online:${friend.name}>")
                )
            } else {
                val username = Hardcraft.database.getPlayerUsername(friendUuid)
                player.sendMessage(
                    Hardcraft.minimessage.deserialize(
                        "<lang:friends.command.list-item-offline:$username>"
                    )
                )
            }
        }
    }
}
