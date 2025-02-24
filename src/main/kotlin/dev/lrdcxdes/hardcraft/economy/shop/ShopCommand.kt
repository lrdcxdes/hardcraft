package dev.lrdcxdes.hardcraft.economy.shop

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ShopCommand(private val shop: Shop) :
    CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: return false

        player.sendMessage("Disabled (Use nitwit villager)")
        return true

//        // if any arg
//        if (args != null) {
//            if (args.isNotEmpty()) {
//                shop.allItemsManager.openInventory(player, 1)
//                return true
//            }
//        }
//
//        // Open the shop inventory for the player
//        shop.openInventory(player)
//
//        // Send a message to the player using MessageManager
//        player.sendMessage(Hardcraft.minimessage.deserialize("<color:#FFB347><lang:shop.open>"))
//
//        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.isNotEmpty() && 2 > args.size) {
            return mutableListOf("all")
        }
        return null
    }
}