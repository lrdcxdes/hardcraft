package dev.lrdcxdes.hardcraft.economy

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class EconomyCommands : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (command.name.lowercase()) {
            "economy" -> {
                if (!sender.hasPermission("hardcraft.admin")) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.no-permission>"))
                    return true
                }

                if (args.size < 3) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.command.usage-admin>"))
                    return true
                }

                val action = args[0].lowercase()
                if (action !in listOf("set", "add", "remove")) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.command.invalid-action>"))
                    return true
                }

                val targetPlayer = Bukkit.getPlayer(args[1])
                if (targetPlayer == null) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.player-not-found>"))
                    return true
                }

                val amount = args[2].toDoubleOrNull()
                if (amount == null || amount < 0) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.command.invalid-amount>"))
                    return true
                }

                Hardcraft.database.changePlayerBalance(targetPlayer.uniqueId.toString(), amount, action)
                sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.admin.success:${action}:${amount}:${targetPlayer.name}>"))
                targetPlayer.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.admin.target-notify:${action}:${amount}>"))
                return true
            }

            "pay" -> {
                if (sender !is Player) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.players-only>"))
                    return true
                }

                if (args.size != 2) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.command.usage-pay>"))
                    return true
                }

                val targetPlayer = Bukkit.getPlayer(args[0])
                if (targetPlayer == null) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.player-not-found>"))
                    return true
                }

                if (sender.location.distance(targetPlayer.location) > 5) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.pay.too-far>"))
                    return true
                }

                val amount = args[1].toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.command.invalid-amount>"))
                    return true
                }

                val senderBalance = Hardcraft.database.getBalance(sender.uniqueId.toString())
                if (senderBalance < amount) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.pay.insufficient-funds>"))
                    return true
                }

                Hardcraft.database.changePlayerBalance(sender.uniqueId.toString(), amount, "remove")
                Hardcraft.database.changePlayerBalance(targetPlayer.uniqueId.toString(), amount, "add")

                sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.pay.success-sender:${amount}:${targetPlayer.name}>"))
                targetPlayer.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.pay.success-receiver:${amount}:${sender.name}>"))
                return true
            }

            "balance" -> {
                if (sender !is Player) {
                    sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:common.players-only>"))
                    return true
                }

                val balance = Hardcraft.database.getBalance(sender.uniqueId.toString())
                sender.sendMessage(Hardcraft.minimessage.deserialize("<lang:economy.balance.current:${balance}>"))
                return true
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        when (command.name.lowercase()) {
            "economy" -> {
                return when (args.size) {
                    1 -> listOf("set", "add", "remove")
                        .filter { it.startsWith(args[0].lowercase()) }

                    2 -> Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }

                    else -> emptyList()
                }
            }

            "pay" -> {
                return when (args.size) {
                    1 -> Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[0].lowercase()) }

                    else -> emptyList()
                }
            }
        }
        return emptyList()
    }
}