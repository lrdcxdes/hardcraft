package dev.lrdcxdes.hardcraft.races

import com.destroystokyo.paper.profile.PlayerProfile
import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.skinsrestorer.api.property.InputDataResult
import net.skinsrestorer.api.storage.PlayerStorage
import org.bukkit.Bukkit
import org.bukkit.Color
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
import org.bukkit.profile.PlayerTextures
import java.net.URI
import java.util.*


fun Player.getRace(): Race? {
    val container = persistentDataContainer
    val raceName = container.get(NamespacedKey(Hardcraft.instance, "playerRace"), PersistentDataType.STRING)
    return raceName?.let { Race.valueOf(it) } ?: return null
}

class RaceHandler(private val plugin: Hardcraft, cmd: PluginCommand?) : Listener {
    inner class RaceCommand : CommandExecutor {
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
            container.remove(NamespacedKey(Hardcraft.instance, "playerRace"))
            openRaceSelection(sender)
            return true
        }
    }

    init {
        cmd?.setExecutor(RaceCommand())
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
        applyRaceAttributes(player, race)
    }

    private fun applyRaceAttributes(player: Player, race: Race) {
        RaceManager.getDefaultAttributes().let { attributes ->
            for ((attribute, value) in attributes.baseAttributes) {
                player.getAttribute(attribute)?.baseValue = value
            }
        }
        RaceManager.getAttributes(race)?.let { attributes ->
            for ((attribute, value) in attributes.baseAttributes) {
                player.getAttribute(attribute)?.baseValue = value
            }
        }

        if (race == Race.AGAR) {
            Agar.getKillerStacks(player).let { stacks ->
                Agar.adjustAttributes(player, stacks, heal = false)
            }
        } else if (race == Race.GOBLIN) {
            Goblin.applyAttributes(player)
        }

        // Skin
        val nowSkinRace = player.persistentDataContainer.getOrDefault(
            NamespacedKey(plugin, "playerRaceSkin"),
            PersistentDataType.STRING,
            ""
        )
        if (nowSkinRace == race.name) return

        // skin
        var coolSkin = false
        var result: Optional<InputDataResult>? = null
        val badSkins = mutableListOf<RaceManager.SkinAttributes>()
        while (!coolSkin) {
            val skin = RaceManager.getRandomSkin(race, badSkins) ?: return
            try {
                result = Hardcraft.skins.skinStorage.findOrCreateSkinData(skin.url)
                if (result == null || result.isEmpty) {
                    badSkins.add(skin)
                    println("Skin not found")
                    return
                } else {
                    coolSkin = true
                }
            } catch (e: Exception) {
                // pass
                badSkins.add(skin)
            }
        }

        val playerStorage: PlayerStorage = Hardcraft.skins.playerStorage
        val skinIden = result!!.get().identifier

        println("Skin found: $skinIden")

        // Associate the skin with the player
        playerStorage.setSkinIdOfPlayer(player.uniqueId, skinIden)

        // Instantly apply skin to the player without requiring the player to rejoin
        Hardcraft.skins.getSkinApplier(Player::class.java).applySkin(player)

        player.persistentDataContainer.set(
            NamespacedKey(plugin, "playerRaceSkin"),
            PersistentDataType.STRING,
            race.name
        )
    }

    // Menu Part

    // Mapping from race keys (in lowercase) to custom symbols.
    private val raceSymbols = mapOf(
        "m_eng_human" to "㈂",
        "m_eng_elf" to "ㇿ",
        "m_eng_dwarf" to "ㇾ",
        "m_eng_amphibian" to "ㇻ",
        "m_eng_kobold" to "㈃",
        "m_eng_goblin" to "㈁",
        "m_eng_giant" to "㈀",
        "m_eng_dragonborn" to "ㇽ",
        "m_eng_vampire" to "㈆",
        "m_eng_skeleton" to "㈄",

        "m_eng_cible" to "ㇼ",
        "m_eng_snolem" to "㈅",
        "m_eng_agar" to "ㇺ",
    )

    // Helper functions to get a symbol and language key suffix for a given race.
    private fun getRaceSymbol(race: Race): String {
        return raceSymbols["m_eng_" + race.name.lowercase()] ?: race.name
    }

    // This class provides the paginated GUI for race selection.
    inner class PaginatedRaceHolder(private val page: Int) : InventoryHolder {
        // Get all races from your Race enum (or similar source).
        private val races = Race.entries.toList()

        override fun getInventory(): Inventory {
            // Ensure the page is valid.
            val currentRace = races[page.coerceIn(0, races.size - 1)]
            // Use the race’s custom symbol as the inventory title.
            val title =
                Component.text("七七七七七七七七" + getRaceSymbol(currentRace)).color(TextColor.color(255, 255, 255))
            // Create an inventory with 54 slots (6 rows of 9)
            val inventory = plugin.server.createInventory(this, 54, title)

            // ----- Slot 40: Info Item -----
            val infoItem = ItemStack(Material.PLAYER_HEAD)
            val infoMeta = infoItem.itemMeta as SkullMeta
            infoMeta.playerProfile = Bukkit.createProfile(UUID.randomUUID()).apply {
                setTextures(
                    textures.apply {
                        setSkin(
                            URI("http://textures.minecraft.net/texture/dd2acd9f2dfc2e05f69d941fe9970e8c3f05527a02a9381157891c8ddb8cf3").toURL(),
                            PlayerTextures.SkinModel.CLASSIC
                        )
                    }
                )
            }
            // Set the display name to the race’s name.
            infoMeta.itemName(
                Hardcraft.minimessage.deserialize(
                    "<color:#ffaa00><bold>${currentRace.name}</bold></color>"
                )
            )
            // Use minimessage to deserialize a lore string.
            // (Assuming your language key is formatted like "<lang:btn.race_lore_human>" etc.)
            // infoMeta.lore(listOf(Hardcraft.minimessage.deserialize("<lang:btn.race_lore_${currentRace.name.lowercase()}>")))

            infoMeta.lore(
                listOf(
                    // The "Permanent" label in bold red as the first line
                    Hardcraft.minimessage.deserialize("<bold><red>Permanent</red></bold>"),
                    // A brief universal description about race info
                    Hardcraft.minimessage.deserialize("<gray>Race Information</gray>"),
                    Hardcraft.minimessage.deserialize("<white>Discover the unique traits, abilities, and lore for every race available on our server.</white>"),
                    // Information directing players to the wiki for more details
                    Hardcraft.minimessage.deserialize("<gold>Learn more at <click:open_url:'https://wiki.btnmc.net'>wiki.btnmc.net</click></gold>")
                )
            )

            // Store the URL in persistent data (you might use it later in your click handler)
            infoMeta.persistentDataContainer.set(
                NamespacedKey(plugin, "wiki"),
                PersistentDataType.STRING,
                "wiki.btnmc.net"
            )

            infoItem.itemMeta = infoMeta
            inventory.setItem(40, infoItem)

            // ----- Slot 41: Left Arrow (Previous Page) -----
            val leftArrow = ItemStack(Material.PLAYER_HEAD)
            val leftMeta = leftArrow.itemMeta as SkullMeta

            leftMeta.playerProfile = Bukkit.createProfile(UUID.randomUUID()).apply {
                setTextures(
                    textures.apply {
                        skin =
                            URI("http://textures.minecraft.net/texture/37aee9a75bf0df7897183015cca0b2a7d755c63388ff01752d5f4419fc645").toURL()
                    }
                )
            }
            leftMeta.itemName(
                Hardcraft.minimessage.deserialize(
                    "<color:#00aa00>Previous</color>"
                )
            )

            leftArrow.itemMeta = leftMeta
            inventory.setItem(41, leftArrow)

            // ----- Slot 42: Right Arrow (Next Page) -----
            val rightArrow = ItemStack(Material.PLAYER_HEAD)
            val rightMeta = rightArrow.itemMeta as SkullMeta

            rightMeta.playerProfile = Bukkit.createProfile(UUID.randomUUID()).apply {
                setTextures(
                    textures.apply {
                        skin =
                            URI("http://textures.minecraft.net/texture/682ad1b9cb4dd21259c0d75aa315ff389c3cef752be3949338164bac84a96e").toURL()
                    }
                )
            }
            rightMeta.itemName(Hardcraft.minimessage.deserialize("<color:#00aa00>Next</color>"))

            rightArrow.itemMeta = rightMeta
            inventory.setItem(42, rightArrow)

            // ----- Slot 43: Confirm Button -----
            val confirmItem = ItemStack(Material.PLAYER_HEAD)
            val confirmMeta = confirmItem.itemMeta as SkullMeta

            confirmMeta.playerProfile = Bukkit.createProfile(UUID.randomUUID()).apply {
                setTextures(
                    textures.apply {
                        skin =
                            URI("http://textures.minecraft.net/texture/a92e31ffb59c90ab08fc9dc1fe26802035a3a47c42fee63423bcdb4262ecb9b6").toURL()
                    }
                )
            }
            confirmMeta.itemName(
                Hardcraft.minimessage.deserialize(
                    "<color:#00aa00><bold>Confirm</color>"
                )
            )

            // Store the race name in the persistent data container.
            confirmMeta.persistentDataContainer.set(
                NamespacedKey(plugin, "race"),
                PersistentDataType.STRING,
                currentRace.name
            )

            confirmItem.itemMeta = confirmMeta
            inventory.setItem(43, confirmItem)

            return inventory
        }

        // Expose the current page so that click events know which page is active.
        fun getPage(): Int = page
    }

    // Opens the race selection GUI for the given page (defaults to page 0).
    private fun openRaceSelection(player: Player, page: Int = 0) {
        val holder = PaginatedRaceHolder(page)
        player.openInventory(holder.inventory)
    }

    // Event: When the player joins, open the GUI if no race is set.
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val container = player.persistentDataContainer
        val raceName = container.get(NamespacedKey(plugin, "playerRace"), PersistentDataType.STRING)

        if (raceName == null) {
            openRaceSelection(player)
        } else {
            val race = Race.valueOf(raceName)
            applyRaceAttributes(player, race)
        }
    }

    // Handle inventory clicks for our paginated race selection.
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder
        if (holder is PaginatedRaceHolder) {
            event.isCancelled = true
            when (event.rawSlot) {
                40 -> { // Info item
                    // Wiki button: send the wiki URL to the player.
                    (event.whoClicked as Player).sendMessage(
                        Hardcraft.minimessage.deserialize(
                            "<color:#AECCE4><click:open_url:https://wiki.btnmc.net/racesandclasses/races>Click here to learn more about races!</click>"
                        )
                    )
                    // Alternatively, you might use a clickable message if your plugin supports it.
                }

                41 -> { // Left arrow: go to previous page if available
                    val currentPage = holder.getPage()
                    if (currentPage > 0) {
                        openRaceSelection(event.whoClicked as Player, currentPage - 1)
                    }
                }

                42 -> { // Right arrow: go to next page if available
                    val races = Race.entries.toList()
                    val currentPage = holder.getPage()
                    if (currentPage < races.size - 1) {
                        openRaceSelection(event.whoClicked as Player, currentPage + 1)
                    }
                }

                43 -> {  // Confirm button: assign the selected
                    val item = event.currentItem
                    if (item != null && item.type == Material.PLAYER_HEAD) {
                        val meta = item.itemMeta as SkullMeta
                        val raceName =
                            meta.persistentDataContainer.get(NamespacedKey(plugin, "race"), PersistentDataType.STRING)
                        if (raceName != null) {
                            assignRace(event.whoClicked as Player, Race.valueOf(raceName))
                            event.whoClicked.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
                        }
                    }
                }
            }
        }
    }

    // Re-open the race selection GUI on close if the player hasn’t chosen a race.
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.reason == InventoryCloseEvent.Reason.OPEN_NEW || event.reason == InventoryCloseEvent.Reason.PLUGIN) return
        if (event.inventory.holder is PaginatedRaceHolder && !(event.player as Player).hasRace()) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                // Reopen the same page that was closed.
                val holder = event.inventory.holder as PaginatedRaceHolder
                openRaceSelection(event.player as Player, holder.getPage())
            }, 1L)
        }
    }

    // add type RaceHolder to cmpanion to use it in other files
    companion object {
        fun checkHolder(holder: InventoryHolder): Boolean {
            return holder is PaginatedRaceHolder
        }
    }
}