package dev.lrdcxdes.hardcraft.customtables

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class BeehiveTable(private val player: Player) : Listener {
    private val inventory = Hardcraft.instance.server.createInventory(
        null,
        54,
        Hardcraft.minimessage.deserialize("<lang:bts.beehive_table>")
    )

    companion object {
        private val SPOIL_TIME = Hardcraft.instance.key("spoilTime")
        private val FRESHNESS = Hardcraft.instance.key("freshness")
    }

    fun open() {
        player.openInventory(inventory)
    }

    init {
        Hardcraft.instance.server.pluginManager.registerEvents(this, Hardcraft.instance)
    }

    private fun hasHoney(): Boolean {
        return inventory.contents.any { item ->
            item != null && (item.type == Material.HONEY_BOTTLE || item.type == Material.HONEYCOMB)
        }
    }

    private data class FoodData(
        val spoilTime: Long,
        val freshness: Double
    )

    private fun getFoodData(item: ItemStack): FoodData? {
        val container = item.itemMeta?.persistentDataContainer
        val spoilTime = container?.get(SPOIL_TIME, PersistentDataType.LONG) ?: return null
        val freshness = container.get(FRESHNESS, PersistentDataType.DOUBLE) ?: return null
        return FoodData(spoilTime, freshness)
    }

    private fun setFoodData(item: ItemStack, data: FoodData) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.apply {
            set(SPOIL_TIME, PersistentDataType.LONG, data.spoilTime)
            set(FRESHNESS, PersistentDataType.DOUBLE, data.freshness)
        }
        item.itemMeta = meta
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked != player) return
        if (event.inventory != inventory) return

        // Если игрок пытается взять предмет - позволяем
        if (event.clickedInventory == inventory &&
            (event.click == ClickType.LEFT ||
                    event.click == ClickType.RIGHT ||
                    event.click == ClickType.SHIFT_LEFT ||
                    event.click == ClickType.SHIFT_RIGHT)
        ) {
            return
        }

        if (!hasHoney()) return

        val clickedItem = event.currentItem
        // Если кликнули по пустому слоту или предмет не съедобный - пропускаем
        if (clickedItem == null || !clickedItem.type.isEdible) return
        // if (clickedItem.type == Material.HONEY_BOTTLE || clickedItem.type == Material.HONEYCOMB) return

        val clickedData = getFoodData(clickedItem) ?: return

        // Ищем все подходящие стаки еды в инвентаре
        val foodStacks = inventory.contents.filterNotNull()
            .filter { item ->
                item.type == clickedItem.type &&
                        item !== clickedItem &&
                        item.amount < item.type.maxStackSize
            }
            .mapNotNull { item ->
                val data = getFoodData(item)
                if (data != null) Pair(item, data) else null
            }
            .sortedBy { (_, data) -> data.freshness }  // Сортируем по свежести (сначала наименее свежие)

        if (foodStacks.isNotEmpty()) {
            event.isCancelled = true

            // Берем наименее свежий стак
            val (targetItem, targetData) = foodStacks[0]

            // Вычисляем новые параметры
            val newData = FoodData(
                spoilTime = minOf(targetData.spoilTime, clickedData.spoilTime),
                freshness = minOf(targetData.freshness, clickedData.freshness)
            )

            // Вычисляем новое количество
            val newAmount = minOf(
                targetItem.amount + clickedItem.amount,
                targetItem.type.maxStackSize
            )

            // Обновляем целевой стак
            targetItem.amount = newAmount
            setFoodData(targetItem, newData)

            // Если есть излишек, создаем новый стак
            val remaining = (targetItem.amount + clickedItem.amount) - targetItem.type.maxStackSize
            if (remaining > 0) {
                val newStack = ItemStack(clickedItem.type, remaining)
                setFoodData(newStack, newData)

                val emptySlot = inventory.firstEmpty()
                if (emptySlot != -1) {
                    inventory.setItem(emptySlot, newStack)
                } else {
                    player.inventory.addItem(newStack)
                }
            }

            // Удаляем исходный предмет
            event.currentItem = null
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.player != player) return
        if (event.inventory != inventory) return

        // Выдадим все предметы игроку
        for (item in inventory.contents) {
            if (item != null && item.type != Material.AIR) {
                player.inventory.addItem(item)
            }
        }

        HandlerList.unregisterAll(this)
    }
}