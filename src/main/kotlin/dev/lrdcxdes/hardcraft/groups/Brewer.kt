package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
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
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class Brewer : Listener {
    // Каждый herb имеет свой цвет и список эффектов
    data class HerbProperties(val color: Color, val effects: List<PotionEffect>)

    // Карта всех допустимых herb (семена, травы, цветы, растительность)
    private val herbs = mutableMapOf(
        // Культуры и семена
        Material.WHEAT to HerbProperties(
            Color.fromRGB(245, 222, 179),
            listOf(PotionEffect(PotionEffectType.SPEED, 200, 0))
        ),
        Material.WHEAT_SEEDS to HerbProperties(
            Color.fromRGB(245, 222, 179),
            listOf(PotionEffect(PotionEffectType.JUMP_BOOST, 200, 0))
        ),
        Material.CARROT to HerbProperties(
            Color.fromRGB(255, 140, 0),
            listOf(PotionEffect(PotionEffectType.HEALTH_BOOST, 200, 0))
        ),
        Material.POTATO to HerbProperties(
            Color.fromRGB(222, 184, 135),
            listOf(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0))
        ),
        Material.BEETROOT to HerbProperties(
            Color.fromRGB(139, 0, 0),
            listOf(PotionEffect(PotionEffectType.REGENERATION, 200, 0))
        ),
        Material.NETHER_WART to HerbProperties(
            Color.fromRGB(178, 34, 34),
            listOf(PotionEffect(PotionEffectType.STRENGTH, 200, 0))
        ),

        // Цветы и дикорастущая растительность
        Material.DANDELION to HerbProperties(
            Color.fromRGB(255, 255, 0),
            listOf(PotionEffect(PotionEffectType.REGENERATION, 100, 0))
        ),
        Material.POPPY to HerbProperties(
            Color.fromRGB(255, 0, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 100, 0))
        ),
        Material.BLUE_ORCHID to HerbProperties(
            Color.fromRGB(65, 105, 225),
            listOf(PotionEffect(PotionEffectType.WATER_BREATHING, 100, 0))
        ),
        Material.ALLIUM to HerbProperties(
            Color.fromRGB(238, 130, 238),
            listOf(PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0))
        ),
        Material.AZURE_BLUET to HerbProperties(
            Color.fromRGB(240, 248, 255),
            listOf(PotionEffect(PotionEffectType.JUMP_BOOST, 100, 0))
        ),
        Material.RED_TULIP to HerbProperties(
            Color.fromRGB(255, 69, 0),
            listOf(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0))
        ),
        Material.ORANGE_TULIP to HerbProperties(
            Color.fromRGB(255, 165, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 100, 0))
        ),
        Material.WHITE_TULIP to HerbProperties(
            Color.fromRGB(255, 250, 250),
            listOf(PotionEffect(PotionEffectType.INVISIBILITY, 100, 0))
        ),
        Material.PINK_TULIP to HerbProperties(
            Color.fromRGB(255, 192, 203),
            listOf(PotionEffect(PotionEffectType.INSTANT_HEALTH, 100, 0))
        ),
        Material.OXEYE_DAISY to HerbProperties(
            Color.fromRGB(255, 248, 220),
            listOf(PotionEffect(PotionEffectType.RESISTANCE, 100, 0))
        ),
        Material.LILY_OF_THE_VALLEY to HerbProperties(
            Color.fromRGB(240, 255, 240),
            listOf(PotionEffect(PotionEffectType.REGENERATION, 150, 0))
        ),
        Material.CORNFLOWER to HerbProperties(
            Color.fromRGB(100, 149, 237),
            listOf(PotionEffect(PotionEffectType.ABSORPTION, 100, 0))
        ),

        // «Дикая» растительность (если предмет существует в Material)
        Material.SHORT_GRASS to HerbProperties(
            Color.fromRGB(124, 252, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.TALL_GRASS to HerbProperties(
            Color.fromRGB(124, 252, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.SEAGRASS to HerbProperties(
            Color.fromRGB(0, 255, 127),
            listOf(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 100, 0))
        ),
        Material.MELON_SEEDS to HerbProperties(
            Color.fromRGB(15, 15, 15),
            listOf(PotionEffect(PotionEffectType.ABSORPTION, 80, 0))
        ),
        Material.PUMPKIN_SEEDS to HerbProperties(
            Color.fromRGB(255, 239, 223),
            listOf(PotionEffect(PotionEffectType.HASTE, 80, 0))
        ),
        Material.BAMBOO to HerbProperties(
            Color.fromRGB(108, 214, 84),
            listOf(PotionEffect(PotionEffectType.STRENGTH, 80, 0))
        ),
        Material.SWEET_BERRIES to HerbProperties(
            Color.fromRGB(255, 96, 87),
            listOf(PotionEffect(PotionEffectType.REGENERATION, 80, 0))
        ),
        Material.COCOA_BEANS to HerbProperties(
            Color.fromRGB(102, 68, 54),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.BEETROOT_SEEDS to HerbProperties(
            Color.fromRGB(116, 27, 71),
            listOf(PotionEffect(PotionEffectType.REGENERATION, 80, 0))
        ),
        Material.KELP to HerbProperties(
            Color.fromRGB(84, 148, 34),
            listOf(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 80, 0))
        ),
        Material.SEA_PICKLE to HerbProperties(
            Color.fromRGB(87, 112, 71),
            listOf(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 80, 0))
        ),
        Material.SUGAR_CANE to HerbProperties(
            Color.fromRGB(144, 238, 144),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.SUGAR to HerbProperties(
            Color.fromRGB(255, 255, 255),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.CACTUS to HerbProperties(
            Color.fromRGB(96, 164, 79),
            listOf(
                PotionEffect(PotionEffectType.SPEED, 80, 0),
                PotionEffect(PotionEffectType.POISON, 80, 0)
            )
        ),
        Material.MELON to HerbProperties(
            Color.fromRGB(255, 140, 0),
            listOf(PotionEffect(PotionEffectType.HEALTH_BOOST, 80, 0))
        ),
        Material.PUMPKIN to HerbProperties(
            Color.fromRGB(231, 106, 0),
            listOf(PotionEffect(PotionEffectType.HASTE, 80, 0))
        ),
        Material.ROSE_BUSH to HerbProperties(
            Color.fromRGB(255, 0, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.LILAC to HerbProperties(
            Color.fromRGB(200, 162, 200),
            listOf(PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0))
        ),
        Material.PEONY to HerbProperties(
            Color.fromRGB(255, 192, 203),
            listOf(PotionEffect(PotionEffectType.INSTANT_HEALTH, 80, 0))
        ),
        Material.SUNFLOWER to HerbProperties(
            Color.fromRGB(255, 215, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.LILY_PAD to HerbProperties(
            Color.fromRGB(0, 255, 127),
            listOf(
                PotionEffect(PotionEffectType.JUMP_BOOST, 80, 0),
                PotionEffect(PotionEffectType.NAUSEA, 80, 0)
            )
        ),
        Material.VINE to HerbProperties(
            Color.fromRGB(0, 255, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.TWISTING_VINES to HerbProperties(
            Color.fromRGB(0, 255, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.WEEPING_VINES to HerbProperties(
            Color.fromRGB(0, 255, 0),
            listOf(PotionEffect(PotionEffectType.SPEED, 80, 0))
        ),
        Material.WITHER_ROSE to HerbProperties(
            Color.fromRGB(0, 0, 0),
            listOf(PotionEffect(PotionEffectType.WITHER, 100, 0))
        ),
    )

    // Карта для отслеживания открытых brewing stand’ов по UUID игрока
    private val views: MutableMap<UUID, InventoryView> = mutableMapOf()

    // Создаём меню варки (брюинг-стенд)
    @Suppress("UnstableApiUsage")
    private fun createBrewerMenu(player: Player): InventoryView {
        val view = MenuType.BREWING_STAND.builder().location(
            player.location.clone().apply { y = -61.0 }
        ).title(
            Component.text("㈇").color(TextColor.color(255, 255, 255))
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
        if (group != Group.BREWER) return

        event.player.openInventory(createBrewerMenu(event.player))
    }

    // Проверка, является ли предмет herb
    private fun isHerb(item: ItemStack): Boolean {
        return herbs.containsKey(item.type)
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
        val rawSlot = event.rawSlot

        // Слоты 0-2 – входные для ингредиентов
        if (rawSlot in 0..2) {
            val cursorItem = event.cursor
            // Если игрок пытается положить пустую руку или не herb – отменяем клик
            if (cursorItem.type == Material.AIR) {
                event.isCancelled = true
                // player.sendMessage("Можно класть только травы или семена!")

                // Забрать предмет
                val x = event.view.getItem(rawSlot)
                if (x != null && x.type != Material.AIR) {
                    event.whoClicked.itemOnCursor = x.clone()
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
                player.sendMessage(Hardcraft.minimessage.deserialize(
                    "<red><lang:btn.brewer_only_herbs>"
                ))
                return
            }

            event.isCancelled = true

            // Разрешаем установку предмета в инвентарь
            val x = event.view.getItem(rawSlot)
            if (x != null && x.type != Material.AIR) {
                val new = event.whoClicked.itemOnCursor.clone()
                event.whoClicked.itemOnCursor = x.clone()
                event.view.setItem(rawSlot, new)
            } else {
                event.view.setItem(rawSlot, event.whoClicked.itemOnCursor.clone())
                event.whoClicked.itemOnCursor.amount = 0
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

        // Вычисляем итоговый список эффектов
        val combinedEffects = combineHerbEffects(ingredients)
        // Вычисляем итоговый цвет зелья как среднее RGB всех трав
        val combinedColor = averageHerbColors(ingredients)
        // Создаём ItemStack зелья с нужными характеристиками
        val potionItem = createPotion(combinedColor, combinedEffects)
        // Помещаем готовое зелье в слот результата (3)
        topInv.setItem(3, potionItem)
        // Очистим входные слоты после начала варки
        // (0..2).forEach { topInv.setItem(it, null) }
    }

    // Объединяем эффекты всех ингредиентов
    private fun combineHerbEffects(ingredients: List<ItemStack>): List<PotionEffect> {
        // Используем мапу для объединения эффектов по типу
        val effectMap = mutableMapOf<PotionEffectType, PotionEffect>()
        for (item in ingredients) {
            val herb = herbs[item.type] ?: continue
            for (effect in herb.effects) {
                var current = effectMap[effect.type]
                if (current == null) {
                    effectMap[effect.type] = effect
                } else {
                    // Если эффект уже присутствует – выбираем тот, что сильнее или длительнее
                    effectMap[effect.type] = PotionEffect(
                        effect.type,
                        effect.duration + current.duration,
                        current.amplifier
                    )
                    current = effectMap[effect.type]

                    if (effect.amplifier > current!!.amplifier) {
                        effectMap[effect.type] = PotionEffect(
                            effect.type,
                            current.duration,
                            effect.amplifier
                        )
                    }
                }
            }
        }
        // Пример «мутировавшего» эффекта: если смешаны 3 разных трав (и эффектов более одного),
        // добавляем дополнительный эффект, например, поглощение
        if (ingredients.size == 3 && effectMap.size > 1) {
            effectMap[PotionEffectType.ABSORPTION] = PotionEffect(PotionEffectType.ABSORPTION, 200, 0)
        }
        return effectMap.values.toList()
    }

    // Вычисляем средний цвет на основе цветов всех ингредиентов
    private fun averageHerbColors(ingredients: List<ItemStack>): Color {
        var totalR = 0
        var totalG = 0
        var totalB = 0
        var count = 0
        for (item in ingredients) {
            val herb = herbs[item.type] ?: continue
            totalR += herb.color.red
            totalG += herb.color.green
            totalB += herb.color.blue
            count++
        }
        if (count == 0) return Color.WHITE
        return Color.fromRGB(totalR / count, totalG / count, totalB / count)
    }

    // Создаём предмет-зелье с заданным цветом и эффектами
    private fun createPotion(color: Color, effects: List<PotionEffect>): ItemStack {
        val potion = ItemStack(Material.POTION)
        val meta = potion.itemMeta as PotionMeta
        meta.color = color
        for (effect in effects) {
            meta.addCustomEffect(effect, true)
        }
        meta.persistentDataContainer.set(Hardcraft.instance.key("rmBottle"), PersistentDataType.BYTE, 1)
        // hide potion effects from lore
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        potion.itemMeta = meta
        return potion
    }

    @EventHandler
    fun onConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type == Material.POTION) {
            val meta = event.item.itemMeta
            val hand = event.hand

            if (meta.persistentDataContainer.has(Hardcraft.instance.key("rmBottle"), PersistentDataType.BYTE)) {
                object : BukkitRunnable() {
                    override fun run() {
                        event.player.inventory.setItem(hand, ItemStack(Material.AIR))
                    }
                }.runTaskLater(Hardcraft.instance, 1L)
            }
        }
    }
}
