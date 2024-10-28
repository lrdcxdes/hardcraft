package dev.lrdcxdes.hardcraft.friends

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class FriendsCommand(private val friendsManager: FriendsManager) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.players-only>"))
            return true
        }

        when (args.getOrNull(0)?.lowercase()) {
            null, "list" -> friendsManager.listFriends(sender)
            "add" -> handleAddCommand(sender, args)
            "remove" -> handleRemoveCommand(sender, args)
            "accept" -> handleAcceptCommand(sender, args)
            "deny" -> handleDenyCommand(sender, args)
            else -> sendHelpMessage(sender)
        }

        return true
    }

    private fun handleAddCommand(player: Player, args: Array<String>) {
        val targetName = args.getOrNull(1) ?: run {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.usage-add>"))
            return
        }

        val target = Hardcraft.instance.server.getPlayer(targetName) ?: run {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.player-not-found>"))
            return
        }

        if (player == target) {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.cant-add-self>"))
            return
        }

        val playerUuid = player.uniqueId.toString()
        val targetUuid = target.uniqueId.toString()

        if (friendsManager.areFriends(playerUuid, targetUuid)) {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.already-friends:${target.name}>"))
            return
        }

        friendsManager.sendFriendRequest(playerUuid, targetUuid)
        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.request-sent:${target.name}>"))
        target.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.request-received:${player.name}>"))
    }

    private fun handleRemoveCommand(player: Player, args: Array<String>) {
        val targetName = args.getOrNull(1) ?: run {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.usage-remove>"))
            return
        }

        val targetUuid = Hardcraft.database.getPlayerUUID(targetName) ?: run {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.player-not-found>"))
            return
        }

        val playerUuid = player.uniqueId.toString()
        if (!friendsManager.areFriends(playerUuid, targetUuid)) {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.not-friends:$targetName>"))
            return
        }

        friendsManager.removeFriend(playerUuid, targetUuid)
        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.removed:$targetName>"))
    }

    private fun handleAcceptCommand(player: Player, args: Array<String>) {
        val targetName = args.getOrNull(1) ?: run {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.usage-accept>"))
            return
        }

        val target = Hardcraft.instance.server.getPlayer(targetName) ?: run {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.player-not-found>"))
            return
        }

        friendsManager.processFriendRequest(player, target)
    }

    private fun handleDenyCommand(player: Player, args: Array<String>) {
        val targetName = args.getOrNull(1) ?: run {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.usage-deny>"))
            return
        }

        val target = Hardcraft.instance.server.getPlayer(targetName) ?: run {
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.player-not-found>"))
            return
        }

        val playerUuid = player.uniqueId.toString()
        val targetUuid = target.uniqueId.toString()

        if (friendsManager.pendingRequests[targetUuid] == playerUuid) {
            friendsManager.pendingRequests.remove(targetUuid)
            player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.request-denied:${target.name}>"))
            target.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.request-denied:${player.name}>"))
        }
    }

    private fun sendHelpMessage(player: Player) {
        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.help-header>"))
        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.help-list>"))
        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.help-add>"))
        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.help-remove>"))
        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.help-accept>"))
        player.sendMessage(Hardcraft.minimessage.deserialize("<lang:friends.command.help-deny>"))
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        if (sender !is Player) return emptyList()

        return when (args.size) {
            1 -> listOf("list", "add", "remove", "accept", "deny")
                .filter { it.startsWith(args[0].lowercase()) }

            2 -> when (args[0].lowercase()) {
                "add" -> Hardcraft.instance.server.onlinePlayers
                    .map { it.name }
                    .filter { it.lowercase().startsWith(args[1].lowercase()) }

                "remove" -> friendsManager.getFriends(sender.uniqueId.toString())
                    .map { Hardcraft.database.getPlayerUsername(it) }
                    .filter { it.lowercase().startsWith(args[1].lowercase()) }

                "accept", "deny" -> friendsManager.pendingRequests.entries
                    .filter { it.value == sender.uniqueId.toString() }
                    .map { Hardcraft.database.getPlayerUsername(it.key) }
                    .filter { it.lowercase().startsWith(args[1].lowercase()) }

                else -> emptyList()
            }

            else -> emptyList()
        }
    }
}