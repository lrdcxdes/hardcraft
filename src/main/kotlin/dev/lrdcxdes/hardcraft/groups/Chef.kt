package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SuspiciousStewMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class Chef : Listener {
    // Карта для отслеживания открытых brewing stand’ов по UUID игрока
    private val views: MutableMap<UUID, InventoryView> = mutableMapOf()

    // Создаём меню варки (брюинг-стенд)
    @Suppress("UnstableApiUsage")
    private fun createBrewerMenu(player: Player): InventoryView {
        val view = MenuType.BREWING_STAND.builder().location(
            player.location.clone().apply { y = -61.0 }
        ).title(
            Component.text("㈈").color(
                TextColor.color(255, 255, 255)
            )
        ).build(player)
        views[player.uniqueId] = view
        return view
    }

    @EventHandler
    fun onBrewBowl(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        val item = event.item ?: return

        if (item.type != Material.BOWL) return
        val group = event.player.getGroup()
        if (group != Group.CHEF) return

        event.player.openInventory(createBrewerMenu(event.player))
    }

    // Проверка, является ли предмет herb
    private fun isHerb(item: ItemStack): Boolean {
        return item.hasData(DataComponentTypes.FOOD) && item.hasData(DataComponentTypes.CONSUMABLE)
    }

    // Обработка клика в brewing stand
    @EventHandler
    fun onBrew(event: InventoryClickEvent) {
        if (event.view.type != InventoryType.BREWING ||
            !views.containsKey(event.whoClicked.uniqueId) ||
            views[event.whoClicked.uniqueId] != event.view
        ) {
            return
        }

        val player = event.whoClicked as Player

        if (player.inventory.itemInMainHand.itemMeta.persistentDataContainer.has(
                Hardcraft.instance.key("rmBowl"),
                PersistentDataType.BYTE
            )
        ) {
            return
        }

        val rawSlot = event.rawSlot

        // Слоты 0-2 – входные для ингредиентов
        if (rawSlot in 0..2) {
            val cursorItem = event.cursor
            // Если игрок пытается положить пустую руку или не herb – отменяем клик
            if (cursorItem.type == Material.AIR) {
                event.isCancelled = true
                // player.sendMessage("Можно класть только еду!")

                // Забрать предмет
                val x = event.view.getItem(rawSlot)
                if (x != null && x.type != Material.AIR) {
                    event.whoClicked.setItemOnCursor(x.clone())
                    event.view.setItem(rawSlot, null)

                    // Планируем проверку: если в слотах появилось 1-3 herb, запускаем варку
                    object : BukkitRunnable() {
                        override fun run() {
                            startBrewing(event.view)
                        }
                    }.runTaskLater(Hardcraft.instance, 1L)
                }
                (event.whoClicked as Player).updateInventory()
                return
            } else if (!isHerb(cursorItem)) {
                event.isCancelled = true
                player.sendMessage(Hardcraft.minimessage.deserialize("<red><lang:btn.chef_only_herbs>"))
                return
            }

            event.isCancelled = true

            // Разрешаем установку предмета в инвентарь
            val x = event.view.getItem(rawSlot)
            if (x != null && x.type != Material.AIR) {
                val new = event.whoClicked.itemOnCursor.clone()
                event.whoClicked.setItemOnCursor(x.clone())
                event.view.setItem(rawSlot, new)
            } else {
                event.view.setItem(rawSlot, event.whoClicked.itemOnCursor.clone())
                event.whoClicked.setItemOnCursor(null)
            }

            (event.whoClicked as Player).updateInventory()

            // Планируем проверку: если в слотах появилось 1-3 herb, запускаем варку
            object : BukkitRunnable() {
                override fun run() {
                    startBrewing(event.view)
                }
            }.runTaskLater(Hardcraft.instance, 1L)
            // player.sendMessage("Вы положили траву в брюинг-стенд!")
        }
        // Слот 3 – результат варки
        else if (rawSlot == 3) {
            if (event.currentItem == null) return
            // Игрок кликает по готовому зелью – можно обработать выдачу зелья и очистку слотов
//            player.inventory.addItem(event.currentItem!!.clone())
//            event.currentItem = null
//            event.view.topInventory.setItem(3, null)
            // player.sendMessage("Вы получили мутированное зелье!")
            player.world.playSound(
                player.location,
                "minecraft:block.brewing_stand.brew", 1.0f, 1.0f
            )
            (0..2).forEach {
                event.view.topInventory.setItem(
                    it,
                    event.view.topInventory.getItem(it)?.clone()?.apply { amount -= 1 })
            }
        }
    }

    // Запускаем процесс варки: собираем ингредиенты, вычисляем итоговые эффекты и цвет
    private fun startBrewing(view: InventoryView) {
        val topInv = view.topInventory
        // Слоты 0,1,2 содержат ингредиенты
        val ingredients = (0..2).mapNotNull { slot ->
            topInv.getItem(slot)?.takeIf { isHerb(it) }
        }
        if (ingredients.isEmpty()) {
            topInv.setItem(3, null)
            return
        }

        // Вычисляем итоговый soup
        val soupItem = createSoup(ingredients)
        // Помещаем готовое soup в слот результата (3)
        topInv.setItem(3, soupItem)
        // Очистим входные слоты после начала варки
        // (0..2).forEach { topInv.setItem(it, null) }
    }

    // Создаём soup с combined consumable and food
    private fun createSoup(ingredients: List<ItemStack>): ItemStack {
        val soup = ItemStack(Material.RABBIT_STEW)
        val meta = soup.itemMeta
        meta.persistentDataContainer.set(Hardcraft.instance.key("rmBowl"), PersistentDataType.BYTE, 1)

        var saturation = 0.0F
        var nutrition = 0
        val effects = mutableListOf<ConsumeEffect>()

        var mostConsumeSeconds = 1.0F

        for (ingredient in ingredients) {
            val food: FoodProperties = ingredient.getData(DataComponentTypes.FOOD) ?: continue
            val consumable = ingredient.getData(DataComponentTypes.CONSUMABLE) ?: continue

            saturation += food.saturation()
            nutrition += food.nutrition()

            if (consumable.consumeSeconds() > mostConsumeSeconds) {
                mostConsumeSeconds = consumable.consumeSeconds()
            }

            effects.addAll(consumable.consumeEffects())
        }

        val myFood = FoodProperties.food().saturation(
            saturation
        ).nutrition(
            nutrition
        ).build()  // .canAlwaysEat(true)

        val myConsumable = Consumable.consumable()
            .consumeSeconds(
                mostConsumeSeconds
            )
            .addEffects(
                effects
            )
            .build()

        meta.itemName(
            Hardcraft.minimessage.deserialize(
                "<lang:btn.chef_soup>"
            )
        )

        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)

        soup.itemMeta = meta

        // soup.resetData(DataComponentTypes.FOOD)
        // soup.resetData(DataComponentTypes.CONSUMABLE)

        soup.setData(DataComponentTypes.FOOD, myFood)
        soup.setData(DataComponentTypes.CONSUMABLE, myConsumable)

        return soup
    }

    @EventHandler
    fun onConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type == Material.RABBIT_STEW) {
            val meta = event.item.itemMeta
            val hand = event.hand

            if (meta.persistentDataContainer.has(Hardcraft.instance.key("rmBowl"), PersistentDataType.BYTE)) {
                // add 30% heal to player from soup.nutrition
                val nutrition = event.item.getData(DataComponentTypes.FOOD)?.nutrition() ?: 0
                event.player.heal(nutrition * 0.3)

                object : BukkitRunnable() {
                    override fun run() {
                        event.player.inventory.setItem(hand, ItemStack(Material.AIR))
                    }
                }.runTaskLater(Hardcraft.instance, 1L)
            }
        }
    }
}
