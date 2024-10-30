package dev.lrdcxdes.hardcraft.economy.shop

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.utils.formatPrice
import dev.lrdcxdes.hardcraft.utils.formatPrice2
import dev.lrdcxdes.hardcraft.utils.globalMessageManager
import dev.lrdcxdes.hardcraft.utils.sendMessage
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Item
import org.bukkit.entity.Llama
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityMountEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.LlamaInventory
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Team
import org.bukkit.util.Vector
import java.util.*

class Shop : Listener {
    private val plugin = Hardcraft.instance
    private val databaseManager = Hardcraft.database

    data class InvLama(
        val lama: Llama,
        val inv: LlamaInventory,
        val book: Item,
        val bookTask: BukkitTask
    ) {
        val slotIds: MutableMap<Int, Int> = mutableMapOf()
    }

    class AllItemsInv(val page: Int) {
        val inv: Inventory = Hardcraft.instance.server.createInventory(
            null,
            54, globalMessageManager.getMessage(
                "shop.title.all",
                "page" to "<color:#87CEEB>$page",
                color = "<color:#FFB347>"
            )
        )
        val slotIds: MutableMap<Int, Int> = mutableMapOf()
    }

    class AllItemsManager {
        private val allItemsInvs: MutableMap<Int, AllItemsInv> = mutableMapOf()
        private var items: MutableMap<Material, ShopItem> = mutableMapOf()

        // Cache items for better performance
        private val nextPageButton = ItemStack(Material.ARROW).apply {
            itemMeta = itemMeta.apply {
                displayName(globalMessageManager.getMessage("shop.next.page", color = "<color:#FFB347>"))
            }
        }
        private val prevPageButton = ItemStack(Material.ARROW).apply {
            itemMeta = itemMeta.apply {
                displayName(globalMessageManager.getMessage("shop.previous.page", color = "<color:#FFB347>"))
            }
        }
        private val glassPane = ItemStack(Material.WHITE_STAINED_GLASS_PANE)

        fun loadItems(aitems: MutableMap<Material, ShopItem>) {
            items = aitems

            var page = 1
            var slot = 0
            for (item in items.values) {
                val allItemsInv = allItemsInvs.getOrPut(page) { AllItemsInv(page) }
                allItemsInv.slotIds[item.id] = slot
                val itemStack = item.toItemStack()

                val meta = itemStack.itemMeta
                val volumeInfo = if (item.nowVolume > item.volume) {
                    "<color:#87CEEB>" + globalMessageManager.getMessageRaw(
                        "shop.volume.buy",
                        "volumeChange" to ((item.nowVolume - item.volume) / item.volume * 100).formatPrice2()
                    )
                } else {
                    "<color:#FFB347>" + globalMessageManager.getMessageRaw(
                        "shop.volume.sell",
                        "volumeChange" to ((item.volume - item.nowVolume) / item.volume * 100).formatPrice2()
                    )
                }
                val loreAll = listOf(
                    globalMessageManager.getMessage(
                        "shop.price.buy",
                        "buyPrice" to "<color:#FFB347>" + item.buyPrice.formatPrice(),
                        color = "<color:#FFB347>"
                    ),
                    globalMessageManager.getMessage(
                        "shop.price.sell",
                        "sellPrice" to "<color:#87CEEB>" + item.sellPrice.formatPrice(),
                        color = "<color:#87CEFA>"
                    ),
                    globalMessageManager.getMessage(
                        "shop.volume.info",
                        "volumeInfo" to "<color:#4169E1>" + item.nowVolume.toInt().toString(),
                        color = "<color:#87CEFA>"
                    ),
                    globalMessageManager.getMessage(
                        "shop.volume.change",
                        "volumeChange" to "<color:#FFB347>$volumeInfo",
                        color = "<color:#87CEFA>"
                    ),
                )
                meta.lore(loreAll)
                itemStack.itemMeta = meta

                allItemsInv.inv.setItem(slot, itemStack)
                slot++
                if (slot > 45) {
                    page++
                    slot = 0

                    // Add next and previous page buttons
                    allItemsInv.inv.setItem(53, nextPageButton)
                    allItemsInv.inv.setItem(45, prevPageButton)

                    for (i in 46 until 53) {
                        allItemsInv.inv.setItem(i, glassPane)
                    }
                }
            }

            val lastInv = allItemsInvs.values.last()
            // Add next and previous page buttons
            lastInv.inv.setItem(53, nextPageButton)
            lastInv.inv.setItem(45, prevPageButton)

            for (i in 46 until 53) {
                lastInv.inv.setItem(i, glassPane)
            }
        }

        fun refreshItemUI(item: ShopItem, lore: List<Component>) {
            println("[allitems] Refreshing item UI for ${item.material.name}")
            // find item in allItemsInv
            for (allItemsInv in allItemsInvs.values) {
                if (allItemsInv.slotIds.containsKey(item.id)) {
                    println("[allitems] Found item in allItemsInv for ${item.material.name} at page ${allItemsInv.page}")
                    val slotId = allItemsInv.slotIds[item.id] ?: continue
                    val stack = allItemsInv.inv.getItem(slotId)
                    val meta = stack!!.itemMeta
                    meta.lore(lore)
                    stack.itemMeta = meta
                }
            }
        }

        fun openInventory(player: Player, page: Int) {
            val allItemsInv = allItemsInvs[page] ?: return
            player.openInventory(allItemsInv.inv)
        }

        private fun checkInventory(inv: Inventory): Boolean {
            return allItemsInvs.values.any { it.inv == inv }
        }

        fun handleClickEvent(event: InventoryClickEvent) {
            val player = event.whoClicked as? Player ?: return
            val inv = event.inventory
            if (!checkInventory(inv)) {
                return
            }
            event.isCancelled = true
            val allItemsInv = allItemsInvs.values.find { it.inv == inv } ?: return
            val slotId = event.slot

            player.playSound(player.location, Sound.ENTITY_ARMADILLO_BRUSH, 1.0f, 1.0f)

            when (slotId) {
                53 -> {
                    // Next page
                    openInventory(player, allItemsInv.page + 1)
                }

                45 -> {
                    // Previous page
                    openInventory(player, allItemsInv.page - 1)
                }

                else -> {
                    val item = items.values.find { allItemsInv.slotIds[it.id] == slotId } ?: return
                    val volumeChange = if (item.nowVolume > item.volume) {
                        "<color:#87CEEB>" + globalMessageManager.getMessageRaw(
                            "shop.volume.buy",
                            "volumeChange" to ((item.nowVolume - item.volume) / item.volume * 100).formatPrice2()
                        )
                    } else {
                        "<color:#FFB347>" + globalMessageManager.getMessageRaw(
                            "shop.volume.sell",
                            "volumeChange" to ((item.volume - item.nowVolume) / item.volume * 100).formatPrice2()
                        )
                    }
                    player.closeInventory()
                    player.sendMessage(
                        "shop.item.info",
                        "material" to item.material.name,
                        "buyPrice" to item.buyPrice.formatPrice(),
                        "sellPrice" to item.sellPrice.formatPrice(),
                        "volume" to item.nowVolume.toInt().toString(),
                        "volumeChange" to volumeChange,
                        color = "<color:#87CEFA>"
                    )
                }
            }
        }
    }

    private val allItemsManager = AllItemsManager()

    private val configFile = plugin.dataFolder.resolve("shop.yml")
    private val config = YamlConfiguration.loadConfiguration(configFile)
    val refreshInterval = config.getInt("refresh-interval")
    private val passiveStabilizeDelay = config.getInt("passive-stabilize-delay")

    private lateinit var refreshTask: BukkitTask

    var refreshIntervalTicks: Long = 0
    private var passiveStabilizeDelayTicks: Long = 0

    private val items: MutableMap<Material, ShopItem> = mutableMapOf()
    val inventories: MutableMap<UUID, InvLama> = mutableMapOf()
    private var nowItems: List<ShopItem> = listOf()

    private val shopTeam = plugin.server.scoreboardManager.mainScoreboard.getTeam("shop")
        ?: plugin.server.scoreboardManager.mainScoreboard.registerNewTeam("shop")

    init {
        shopTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        shopTeam.prefix(null)

        loadItems()
        allItemsManager.loadItems(items)
        startTasks()
    }

    private fun loadItems() {
        val categories = config.getConfigurationSection("categories") ?: return

        for (category in categories.getKeys(false)) {
            val categorySection = categories.getConfigurationSection(category) ?: continue
            val initPrice = categorySection.getDouble("init_price")
            val volume = categorySection.getInt("volume").toDouble()
            val itemsList = categorySection.getStringList("items")
            val materialList = itemsList.mapNotNull {
                try {
                    Material.getMaterial(it)
                } catch (e: IllegalArgumentException) {
                    plugin.server.logger.warning("Failed to load item $it from the shop")
                    null
                }
            }

            // go through the materialList
            for (material in materialList) {
                // Find the material in the database
                val hasItem = databaseManager.hasItem(material)
                if (!hasItem) {
                    // Add the item to the database
                    databaseManager.addItem(material, initPrice, volume)
                }

                val dbItem = databaseManager.getItem(material)
                if (dbItem == null) {
                    plugin.server.logger.warning("Failed to load item $material from the database")
                    continue
                }
                val id = dbItem.getInt("id")
                val dbVolume = dbItem.getDouble("volume")
                val dbInitPrice = dbItem.getDouble("init_price")
                val dbInitVolume = dbItem.getDouble("init_volume")

                dbItem.close()

                if (dbInitVolume != volume || dbInitPrice != initPrice) {
                    // Update the item in the database
                    databaseManager.updateItem(id, initPrice, volume)
                }

                // Create a new ShopItem
                val shopItem = ShopItem(id, initPrice, volume, material, dbVolume)
                items[material] = shopItem
            }
        }
    }

    private fun refreshItemUI() {
        for (item in nowItems) {
            refreshItemUI(item)
        }
    }

    private fun refreshItemUI(llamaInv: InvLama) {
        for (item in nowItems) {
            refreshItemUI(llamaInv, item)
        }

        llamaInv.inv.decor = ItemStack(Material.KNOWLEDGE_BOOK).apply {
            itemMeta = itemMeta.apply {
                itemName(Hardcraft.minimessage.deserialize("<green><lang:shop.all-items.item>"))
//                lore(Hardcraft.minimessage.deserialize())
            }
        }
    }

    private fun refreshItemUI(item: ShopItem) {
        val buyPrice = item.buyPrice
        val sellPrice = item.sellPrice
        val nowVolume = item.nowVolume

        val maxSell = calcBetterShift(item.volume, item.nowVolume)
        val maxBuy = calcBetterShift(item.volume, item.nowVolume)

        val bulkBuyPrice = item.calculateBullkBuyPrice(maxBuy.toDouble())
        val bulkSellPrice = item.calculateBulkSellPrice(maxSell.toDouble())

        for (llamaInv in inventories.values) {
            val slotId = llamaInv.slotIds[item.id] ?: continue
            val stack = llamaInv.inv.getItem(slotId)
            val meta = stack!!.itemMeta

            val volumeInfo = if (nowVolume > item.volume) {
                "<color:#87CEEB>" + globalMessageManager.getMessageRaw(
                    "shop.volume.buy",
                    "volumeChange" to ((nowVolume - item.volume) / item.volume * 100).formatPrice2()
                )
            } else {
                "<color:#FFB347>" + globalMessageManager.getMessageRaw(
                    "shop.volume.sell",
                    "volumeChange" to ((item.volume - nowVolume) / item.volume * 100).formatPrice2()
                )
            }

            val lore = mutableListOf(
                globalMessageManager.getMessage(
                    "shop.price.buy",
                    "buyPrice" to "<color:#FFB347>" + buyPrice.formatPrice(),
                    color = "<color:#FFB347>"
                ),
                globalMessageManager.getMessage(
                    "shop.price.sell",
                    "sellPrice" to "<color:#87CEEB>" + sellPrice.formatPrice(),
                    color = "<color:#87CEFA>"
                ),
                globalMessageManager.getMessage(
                    "shop.volume.info",
                    "volumeInfo" to "<color:#4169E1>$volumeInfo",
                    color = "<color:#87CEFA>"
                ),
                Component.empty(),
                globalMessageManager.getMessage("shop.actions.left-click"),
                globalMessageManager.getMessage("shop.actions.right-click"),
                globalMessageManager.getMessage(
                    "shop.actions.shift-left-click",
                    "max" to maxBuy.toString(),
                    "price" to bulkBuyPrice.formatPrice()
                ),
                globalMessageManager.getMessage(
                    "shop.actions.shift-right-click",
                    "max" to maxSell.toString(),
                    "price" to bulkSellPrice.formatPrice()
                )
            )
            meta.lore(lore)
            stack.itemMeta = meta
        }

        val volumeInfo = if (nowVolume > item.volume) {
            "<color:#87CEEB>" + globalMessageManager.getMessageRaw(
                "shop.volume.buy",
                "volumeChange" to ((nowVolume - item.volume) / item.volume * 100).formatPrice2()
            )
        } else {
            "<color:#FFB347>" + globalMessageManager.getMessageRaw(
                "shop.volume.sell",
                "volumeChange" to ((item.volume - nowVolume) / item.volume * 100).formatPrice2()
            )
        }

        // copy list and remove last 4 elements
        val loreAll = listOf(
            globalMessageManager.getMessage(
                "shop.price.buy",
                "buyPrice" to "<color:#FFB347>" + item.buyPrice.formatPrice(),
                color = "<color:#FFB347>"
            ),
            globalMessageManager.getMessage(
                "shop.price.sell",
                "sellPrice" to "<color:#87CEEB>" + item.sellPrice.formatPrice(),
                color = "<color:#87CEFA>"
            ),
            globalMessageManager.getMessage(
                "shop.volume.info",
                "volumeInfo" to "<color:#4169E1>" + item.nowVolume.toInt().toString(),
                color = "<color:#87CEFA>"
            ),
            globalMessageManager.getMessage(
                "shop.volume.change",
                "volumeChange" to "<color:#FFB347>$volumeInfo",
                color = "<color:#87CEFA>"
            ),
        )
        allItemsManager.refreshItemUI(item, loreAll)
    }

    private fun refreshItemUI(llamaInv: InvLama, item: ShopItem) {
        val buyPrice = item.buyPrice
        val sellPrice = item.sellPrice
        val nowVolume = item.nowVolume

        val volumeInfo = if (nowVolume > item.volume) {
            "<color:#87CEEB>" + globalMessageManager.getMessageRaw(
                "shop.volume.buy",
                "volumeChange" to ((nowVolume - item.volume) / item.volume * 100).formatPrice2()
            )
        } else {
            "<color:#FFB347>" + globalMessageManager.getMessageRaw(
                "shop.volume.sell",
                "volumeChange" to ((item.volume - nowVolume) / item.volume * 100).formatPrice2()
            )
        }

        val maxSell = calcBetterShift(item.volume, item.nowVolume)
        val maxBuy = calcBetterShift(item.volume, item.nowVolume)

        val bulkBuyPrice = item.calculateBullkBuyPrice(maxBuy.toDouble())
        val bulkSellPrice = item.calculateBulkSellPrice(maxSell.toDouble())

        item.lore = mutableListOf(
            globalMessageManager.getMessage(
                "shop.price.buy",
                "buyPrice" to "<color:#FFB347>" + buyPrice.formatPrice(),
                color = "<color:#FFB347>"
            ),
            globalMessageManager.getMessage(
                "shop.price.sell",
                "sellPrice" to "<color:#87CEEB>" + sellPrice.formatPrice(),
                color = "<color:#87CEFA>"
            ),
            globalMessageManager.getMessage(
                "shop.volume.info",
                "volumeInfo" to "<color:#4169E1>$volumeInfo",
                color = "<color:#87CEFA>"
            ),
            Component.empty(),
            globalMessageManager.getMessage("shop.actions.left_click", color = "<color:#87CEFA>"),
            globalMessageManager.getMessage("shop.actions.right_click", color = "<color:#87CEFA>"),
            globalMessageManager.getMessage(
                "shop.actions.shift_left_click",
                "max" to "<color:#87CEEB>$maxBuy",
                "price" to "<color:#87CEEB>" + bulkBuyPrice.formatPrice(),
                color = "<color:#87CEFA>"
            ),
            globalMessageManager.getMessage(
                "shop.actions.shift_right_click",
                "max" to "<color:#87CEEB>$maxSell",
                "price" to "<color:#87CEEB>" + bulkSellPrice.formatPrice(),
                color = "<color:#87CEFA>"
            )
        )

        val slotId = llamaInv.slotIds[item.id] ?: return
        val stack = llamaInv.inv.getItem(slotId)
        val meta = stack!!.itemMeta
        meta.lore(item.lore)
        stack.itemMeta = meta
    }

    private fun calcBetterShift(volume: Double, nowVolume: Double): Int {
        // so if nowVolume > 600 return 64, if nowVolume > 300 return 32, etc.
        return when (nowVolume) {
            in 0.0..volume / 128 -> 1
            in volume / 128..volume / 64 -> 2
            in volume / 64..volume / 32 -> 4
            in volume / 32..volume / 16 -> 8
            in volume / 16..volume / 8 -> 16
            in volume / 8..volume / 4 -> 32
            in volume / 4..volume / 2 -> 64
            else -> 64
        }
    }

    private fun startTasks() {
        refreshIntervalTicks = refreshInterval * 20L
        passiveStabilizeDelayTicks = passiveStabilizeDelay * 20L

        val lastRefreshInterval = (databaseManager.getConfig("shop.refresh-interval") as? Long)?.toLong() ?: 0
        val lastPassiveStabilizeDelay =
            (databaseManager.getConfig("shop.passive-stabilize-delay") as? Long)?.toLong() ?: 0

        val lastItems: List<ShopItem>? = (databaseManager.getConfig("shop.items") as? String)?.let { value ->
            val items = value.split(",").mapNotNull { itemIdStr ->
                val id = itemIdStr.toIntOrNull() ?: return@mapNotNull null
                val item = items.values.find { it.id == id } ?: return@mapNotNull null
                item
            }
            if (items.size == 15) {
                items
            } else {
                plugin.server.logger.warning("Failed to load items from the database")
                null
            }
        }

        if (lastItems != null) {
            refreshShopItems(lastItems)
        }

        refreshTask = object : BukkitRunnable() {
            override fun run() {
                refreshShopItems()
                refreshIntervalTicks = refreshInterval * 20L
            }
        }.runTaskTimerAsynchronously(plugin, lastRefreshInterval, refreshIntervalTicks)

        if (lastRefreshInterval != 0L && lastRefreshInterval != refreshIntervalTicks) {
            refreshIntervalTicks = lastRefreshInterval
        }

        if (lastPassiveStabilizeDelay != 0L && lastPassiveStabilizeDelay != passiveStabilizeDelayTicks) {
            passiveStabilizeDelayTicks = lastPassiveStabilizeDelay
        }

        object : BukkitRunnable() {
            override fun run() {
                refreshIntervalTicks -= 20L
                passiveStabilizeDelayTicks -= 20L
                if (refreshIntervalTicks <= 0L) {
                    refreshIntervalTicks = refreshInterval * 20L
                }
                if (passiveStabilizeDelayTicks <= 0L) {
                    passiveStabilizeDelayTicks = passiveStabilizeDelay * 20L
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20L)
    }

    fun onDisable() {
        refreshTask.cancel()
        databaseManager.setConfig("shop.refresh-interval", refreshIntervalTicks)
        databaseManager.setConfig("shop.passive-stabilize-delay", passiveStabilizeDelayTicks)
        databaseManager.setConfig("shop.items", nowItems.joinToString(",") { it.id.toString() })
    }

    private fun refreshShopItems() {
        nowItems = items.values.shuffled().take(15)
        refreshItemUI()
    }

    private fun refreshShopItems(items: List<ShopItem>) {
        nowItems = items
        refreshItemUI()
    }

    private fun buyItem(player: Player, amount: Int, item: ShopItem): Boolean {
        val price = if (amount == 1) {
            item.buyPrice
        } else {
            item.calculateBullkBuyPrice(amount.toDouble())
        }
        if (item.nowVolume < amount) {
            player.sendMessage(
                "shop.errors.insufficient_stock_buy",
                "material" to "<color:#FFB347>" + item.material.name,
                "amount" to "<color:#87CEEB>$amount",
                "totalPrice" to "<color:#87CEEB>" + price.formatPrice(),
                "nowVolume" to "<color:#87CEEB>" + item.nowVolume.toInt().toString(),
                color = "<color:#87CEEB>"
            )
            return false
        }
        val balance = databaseManager.getBalance(player.uniqueId.toString())
        if (balance < price) {
            player.sendMessage(
                "shop.errors.insufficient_balance_buy",
                "material" to "<color:#FFB347>" + item.material.name,
                "amount" to "<color:#87CEEB>$amount",
                "totalPrice" to "<color:#87CEEB>" + price.formatPrice(),
                "balance" to "<color:#87CEEB>" + balance.formatPrice(),
                color = "<color:#87CEEB>"
            )
            return false
        }
        databaseManager.changePlayerBalance(player.uniqueId.toString(), price, "remove")
        item.buy(amount.toDouble())
        databaseManager.changeItemVolume(item.id, item.nowVolume, "set")
        player.sendMessage(
            "shop.success.buy",
            "material" to "<color:#FFB347>" + item.material.name,
            "amount" to "<color:#87CEEB>$amount",
            "totalPrice" to "<color:#FFB347>" + price.formatPrice(),
            color = "<color:#FFB347>"
        )
        val itemStack = item.toItemStack()
        itemStack.amount = amount
        player.inventory.addItem(itemStack)
        return true
    }

    private fun sellItem(player: Player, amount: Int, item: ShopItem): Boolean {
        val price = if (amount == 1) {
            item.sellPrice
        } else {
            item.calculateBulkSellPrice(amount.toDouble())
        }
        if (item.nowVolume + amount > item.volume * 2) {
            player.sendMessage(
                "shop.errors.insufficient_stock_sell",
                "material" to "<color:#FFB347>" + item.material.name,
                "amount" to "<color:#87CEEB>$amount",
                "totalPrice" to "<color:#87CEEB>" + price.formatPrice(),
                "nowVolume" to "<color:#87CEEB>" + item.nowVolume.toInt().toString(),
                color = "<color:#87CEEB>"
            )
            return false
        }
        val itemStack = player.inventory.all(item.material).values.sumOf { it.amount }
        if (itemStack < amount) {
            player.sendMessage(
                "shop.errors.insufficient_item_inventory",
                "material" to "<color:#FFB347>" + item.material.name,
                "amount" to "<color:#87CEEB>$amount",
                "totalPrice" to "<color:#87CEEB>" + price.formatPrice(),
                "itemStack" to "<color:#87CEEB>$itemStack",
                color = "<color:#87CEEB>"
            )
            return false
        }
        databaseManager.changePlayerBalance(player.uniqueId.toString(), price, "add")
        item.sell(amount.toDouble())
        databaseManager.changeItemVolume(item.id, item.nowVolume, "set")

        player.sendMessage(
            "shop.success.sell",
            "material" to "<color:#FFB347>" + item.material.name,
            "amount" to "<color:#87CEEB>$amount",
            "totalPrice" to "<color:#FFB347>" + price.formatPrice(),
            color = "<color:#FFB347>"
        )

        var amountS = amount
        // remove x amount of item from player's inventory
        for (itemS in player.inventory.all(item.material).values) {
            if (amountS == 0) {
                break
            }
            if (itemS.amount >= amountS) {
                itemS.amount -= amountS
                break
            } else {
                amountS -= itemS.amount
                itemS.amount = 0
            }
        }

        return true
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        if (inventories.containsKey(player.uniqueId)) {
            val invLama = inventories[player.uniqueId] ?: return
            if (event.clickedInventory != invLama.inv) {
                return
            }
            event.isCancelled = true
            
            val itemClick = event.currentItem ?: return
            if (itemClick.type == Material.KNOWLEDGE_BOOK) {
                allItemsManager.openInventory(player, 1)
            }

            if (event.slot < 2) {
                return
            }
            val item = items[itemClick.type] ?: return

            if (event.isLeftClick) {
                val amount = if (event.isShiftClick) {
                    calcBetterShift(item.volume, item.nowVolume)
                } else {
                    1
                }

                plugin.server.logger.info("BUY | Amount: $amount")

                // Handle buying
                val success = buyItem(player, amount, item)
                if (success) {
                    // play sound
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                    refreshItemUI(item)
                } else {
                    // play sound
                    player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                }
                event.isCancelled = true
            } else if (event.isRightClick) {
                val amount = if (event.isShiftClick) {
                    calcBetterShift(item.volume, item.nowVolume)
                } else {
                    1
                }

                // Handle selling
                val success = sellItem(player, amount, item)
                if (success) {
                    // play sound
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                    refreshItemUI(item)
                } else {
                    // play sound
                    player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                }
                event.isCancelled = true
            }
        } else {
            allItemsManager.handleClickEvent(event)
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val uuid = event.player.uniqueId
        val invLama = inventories[uuid] ?: return
        inventories.remove(uuid)
        invLama.bookTask.cancel()
        invLama.book.remove()
        object : BukkitRunnable() {
            override fun run() {
                shopTeam.removeEntity(invLama.lama)
                invLama.lama.remove()
            }
        }.runTaskLater(plugin, 1)
    }

    @EventHandler
    fun onEntityMountEvent(event: EntityMountEvent) {
        if (event.mount is Llama && (event.mount as Llama).inventory.decor?.type == Material.KNOWLEDGE_BOOK) {
            event.isCancelled = true
        }
    }

    private fun openInventory(player: Player) {
        val llama = player.world.spawn(player.location, Llama::class.java) {
            it.setAI(false)
            it.isTamed = true
            it.isInvulnerable = true
            it.isInvisible = true
            it.isCollidable = false
            val title = globalMessageManager.getMessage(
                "shop.title",
                color = "<color:#FFB347>"
            )
            it.customName(
                title
            )
            it.isCustomNameVisible = true
            it.isPersistent = true
            it.removeWhenFarAway = false
            it.isSilent = true
            it.strength = 5
            it.passengers.add(player)
            it.isCarryingChest = true
            it.setGravity(false)
            it.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 0.01
            shopTeam.addEntity(it)
        }

        val book = player.world.spawn(
            player.location.add(0.0, 2.0, 0.0), Item::class.java
        ) {
            it.isInvulnerable = true
            it.isInvisible = true
            it.isPersistent = true
            it.isSilent = true
            it.setGravity(false)
            it.setCanMobPickup(false)
            it.setCanPlayerPickup(false)

            it.itemStack = ItemStack(Material.KNOWLEDGE_BOOK)
        }

        val inv = llama.inventory
        inv.clear()

        inv.decor = ItemStack(Material.KNOWLEDGE_BOOK)

        val bookTask = object : BukkitRunnable() {
            override fun run() {
                val target = player.location.add(0.0, 2.0, 0.0)
                val distance = book.location.distance(target)

                if (distance < 0.1) {
                    book.velocity = Vector(0.0, 0.0, 0.0)
                    return
                }

                // Apply a gradual slowdown as the book gets closer to the target
                val speedFactor = (distance / 2).coerceAtLeast(0.1) // Ensure speed doesn't go too low
                val direction = target.subtract(book.location).toVector().normalize().multiply(speedFactor)
                book.velocity = direction
            }
        }.runTaskTimer(plugin, 0, 1L)

        val llamaInv = InvLama(llama, inv, book, bookTask)

        for ((index, item) in nowItems.withIndex()) {
            llamaInv.slotIds[item.id] = index + 2
            val itemStack = item.toItemStack()
            inv.setItem(index + 2, itemStack)
        }

        refreshItemUI(llamaInv)

        inventories[player.uniqueId] = llamaInv
        player.playSound(player.location, Sound.ENTITY_ARMADILLO_BRUSH, 1.0f, 1.0f)
        player.openInventory(inv)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractAtEntityEvent) {
        val p = event.player
        val e = event.rightClicked

        if (e is Villager && e.profession == Villager.Profession.NITWIT) {
            event.isCancelled = true
            openInventory(p)
        }
    }
}
