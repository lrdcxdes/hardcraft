package dev.lrdcxdes.hardcraft.races

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
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

class RaceHandler(private val plugin: Hardcraft, cmd: PluginCommand?) : Listener {
    inner class RaceCommand: CommandExecutor {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (sender !is Player) {
                sender.sendMessage("Only players can use this command")
                return true
            }
            val container = sender.persistentDataContainer
            container.remove(NamespacedKey(Hardcraft.instance, "playerRace"))
            showRaceSelection(sender)
            return true
        }
    }

    init {
        cmd?.setExecutor(RaceCommand())
    }

    private fun Player.getRace(): Race? {
        val container = persistentDataContainer
        val raceName = container.get(NamespacedKey(plugin, "playerRace"), PersistentDataType.STRING)
        return raceName?.let { Race.valueOf(it) } ?: return null
    }

    private fun Player.hasRace(): Boolean {
        val container = persistentDataContainer
        return container.has(NamespacedKey(plugin, "playerRace"), PersistentDataType.STRING)
    }

    private fun assignRace(player: Player, race: Race) {
        if (player.hasRace()) return
        // Пример сохранения через PersistentDataContainer:
        val container = player.persistentDataContainer
        container.set(NamespacedKey(plugin, "playerRace"), PersistentDataType.STRING, race.name)

        // Применение атрибутов:
        RaceManager.getAttributes(race)?.let { attributes ->
            for ((attribute, value) in attributes.baseAttributes) {
                player.getAttribute(attribute)?.baseValue = value
            }
            for (effect in attributes.potionEffects) {
                player.addPotionEffect(effect)
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val container = player.persistentDataContainer
        val raceName = container.get(NamespacedKey(plugin, "playerRace"), PersistentDataType.STRING)

        if (raceName == null) {
            // Вызываем метод для показа GUI или отправляем сообщение с выбором
            showRaceSelection(player)
        } else {
            // Восстанавливаем атрибуты выбранной расы
            val race = Race.valueOf(raceName)
            RaceManager.getAttributes(race)?.let { attributes ->
                for ((attribute, value) in attributes.baseAttributes) {
                    player.getAttribute(attribute)?.baseValue = value
                }
                for (effect in attributes.potionEffects) {
                    player.addPotionEffect(effect)
                }
            }
        }
    }

    private val raceHolder: InventoryHolder = object : InventoryHolder {
        override fun getInventory(): Inventory {
            val inventory = plugin.server.createInventory(
                this,
                36,
                Hardcraft.minimessage.deserialize("<color:#ff0000>Choose your race</color>")
            )
            for (race in Race.entries) {
                val item = ItemStack(Material.PLAYER_HEAD)
                val meta = item.itemMeta as SkullMeta
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(race.name))
                meta.itemName(
                    Hardcraft.minimessage.deserialize(
                        "<color:#00ff00><hover:show_text:'Choose ${race.name}'>${race.name}</hover></color>"
                    )
                )
                meta.persistentDataContainer.set(NamespacedKey(plugin, "race"), PersistentDataType.STRING, race.name)
                item.itemMeta = meta
                inventory.addItem(item)
            }
            return inventory
        }
    }

    // Race Selection
    private fun showRaceSelection(player: Player) {
        // Открываем GUI с выбором расы
        player.openInventory(raceHolder.inventory)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.holder == raceHolder && !(event.player as Player).hasRace()) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                showRaceSelection(event.player as Player)
            }, 1L)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.holder == raceHolder) {
            event.isCancelled = true
            val item = event.currentItem
            if (item != null && item.type == Material.PLAYER_HEAD) {
                val meta = item.itemMeta as SkullMeta
                val raceName =
                    meta.persistentDataContainer.get(NamespacedKey(plugin, "race"), PersistentDataType.STRING)
                if (raceName != null) {
                    assignRace(event.whoClicked as Player, Race.valueOf(raceName))
                    event.whoClicked.closeInventory()
                }
            }
        }
    }
}