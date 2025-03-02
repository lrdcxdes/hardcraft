package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.races.RaceHandler
import dev.lrdcxdes.hardcraft.races.getRace
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType

fun Player.getGroup(): Group? {
    val container = persistentDataContainer
    val name = container.get(NamespacedKey(Hardcraft.instance, "playerGroup"), PersistentDataType.STRING)
    return name?.let { Group.valueOf(it) } ?: return null
}

class GroupHandler(private val plugin: Hardcraft, cmd: PluginCommand?) : Listener {
    inner class GroupCommand : CommandExecutor {
        override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
        ): Boolean {
            if (sender !is Player) {
                sender.sendMessage("Only players can use this command")
                return true
            }
            val container = sender.persistentDataContainer
            container.remove(NamespacedKey(Hardcraft.instance, "playerGroup"))
            showGroupSelection(sender)
            return true
        }
    }

    init {
        cmd?.setExecutor(GroupCommand())
    }

    private fun Player.hasGroup(): Boolean {
        val container = persistentDataContainer
        return container.has(NamespacedKey(plugin, "playerGroup"), PersistentDataType.STRING)
    }

    private fun assignGroup(player: Player, group: Group) {
        if (player.hasGroup()) return
        // Пример сохранения через PersistentDataContainer:
        val container = player.persistentDataContainer
        container.set(NamespacedKey(plugin, "playerGroup"), PersistentDataType.STRING, group.name)

        if (group == Group.ROGUE) {
            //Дополнительно увеличивает скорость скрытного передвижения на 50%.
            player.getAttribute(Attribute.SNEAKING_SPEED)!!.baseValue = 0.6
        } else {
            player.getAttribute(Attribute.SNEAKING_SPEED)!!.baseValue = 0.3
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val container = player.persistentDataContainer
        val groupName = container.get(NamespacedKey(plugin, "playerGroup"), PersistentDataType.STRING)

        if (groupName == null && player.getRace() != null) {
            // Вызываем метод для показа GUI или отправляем сообщение с выбором
            showGroupSelection(player)
        }
    }

    private val groupHolder: InventoryHolder = object : InventoryHolder {
        override fun getInventory(): Inventory {
            val inventory = plugin.server.createInventory(
                this,
                36,
                Hardcraft.minimessage.deserialize("<white>七七七七七七七七㈊")
            )
            for (group in Group.entries) {
                val item = ItemStack(Material.PLAYER_HEAD)
                val meta = item.itemMeta as SkullMeta
                // meta.setOwningPlayer(Bukkit.getOfflinePlayer(group.name))
                meta.itemName(
                    Hardcraft.minimessage.deserialize(
                        "<color:#00ff00><hover:show_text:'Choose ${group.name}'>${group.name}</hover></color>"
                    )
                )
                meta.persistentDataContainer.set(NamespacedKey(plugin, "group"), PersistentDataType.STRING, group.name)
                item.itemMeta = meta
                inventory.addItem(item)
            }
            return inventory
        }
    }

    // Group Selection
    private fun showGroupSelection(player: Player) {
        // Открываем GUI с выбором расы
        player.openInventory(groupHolder.inventory)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if ((event.player as? Player)?.hasGroup() == true) return
        val holder = event.inventory.holder ?: return
        if (holder == groupHolder) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                showGroupSelection(event.player as Player)
            }, 1L)
        } else if (RaceHandler.checkHolder(holder) && event.reason == InventoryCloseEvent.Reason.PLUGIN) {
            // Вызываем метод для показа GUI или отправляем сообщение с выбором
            showGroupSelection(event.player as Player)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.holder == groupHolder) {
            event.isCancelled = true
            val item = event.currentItem
            if (item != null && item.type == Material.PLAYER_HEAD) {
                val meta = item.itemMeta as SkullMeta
                val groupName =
                    meta.persistentDataContainer.get(NamespacedKey(plugin, "group"), PersistentDataType.STRING)
                if (groupName != null) {
                    assignGroup(event.whoClicked as Player, Group.valueOf(groupName))
                    event.whoClicked.closeInventory()
                }
            }
        }
    }
}