package dev.lrdcxdes.hardcraft.customtables

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import net.kyori.adventure.sound.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

data class CustomTableItemMatch(
    val type: List<Material>,
    val amount: Int,
    val itemName: Component? = null,
    val customModelData: Int? = null,
    val effectType: PotionEffectType? = null,
) {
    constructor(type: Material, amount: Int) : this(listOf(type), amount)
    constructor(type: Material, amount: Int, itemName: Component, customModelData: Int) : this(
        listOf(type),
        amount,
        itemName,
        customModelData
    )

    constructor(type: Material, amount: Int, effectType: PotionEffectType) : this(
        listOf(type),
        amount,
        null,
        null,
        effectType
    )
}

data class CustomTableItem(
    val stack: ItemStack,
    val craftMatch: List<CustomTableItemMatch>,
    val forceUpdate: Boolean = false
)

open class CustomTable(
    title: Component, private val items: List<CustomTableItem>,
    private val successSound: Sound,
    private val failSound: Sound
) : Listener {
    private val inventory: Inventory = Bukkit.createInventory(
        null,
        InventoryType.DISPENSER,
        title
    )

    init {
        for (i in 0 until 9) {
            if (i < items.size) {
                inventory.setItem(i, items[i].stack)
            }
        }

        Hardcraft.instance.server.pluginManager.registerEvents(this, Hardcraft.instance)
    }

    fun openInventory(player: org.bukkit.entity.Player) {
        for (i in 0 until 9) {
            if (i < items.size) {
                if (items[i].forceUpdate) {
                    inventory.setItem(i, items[i].stack)
                }
            }
        }

        player.openInventory(inventory)
    }

    @EventHandler
    fun onItemClick(event: InventoryClickEvent) {
        if (event.clickedInventory == inventory) {
            event.isCancelled = true

            val player = event.whoClicked
            val clickedItem = event.currentItem

            if (clickedItem != null) {
                for (item in inventory.contents) {
                    if (item != null && item.isSimilar(clickedItem)) {
                        val match = items.find { it.stack.isSimilar(item) }?.craftMatch
                        if (match != null) {
                            val canCraft = match.all { matchItem ->
                                val playerItem =
                                    player.inventory.contents.find {
                                        it != null && matchItem.type.find { matchType ->
                                            matchType == it.type && (
                                                    matchItem.itemName == null ||
                                                            matchItem.itemName == it.itemMeta.itemName()
                                                            && (
                                                            matchItem.customModelData == null ||
                                                                    matchItem.customModelData == it.itemMeta.customModelData
                                                            )
                                                            && (
                                                            matchItem.effectType == null ||
                                                                    checkPotMeta(it, matchItem.effectType)
                                                            )
                                                    )
                                        } != null
                                    }
                                playerItem != null && playerItem.amount >= matchItem.amount
                            }

                            if (canCraft) {
                                player.playSound(successSound)

                                match.forEach { matchItem ->
                                    val playerItem = player.inventory.contents.find {
                                        it != null && matchItem.type.find { matchType ->
                                            matchType == it.type && (
                                                    matchItem.itemName == null ||
                                                            matchItem.itemName == it.itemMeta.itemName()
                                                            && (
                                                            matchItem.customModelData == null ||
                                                                    matchItem.customModelData == it.itemMeta.customModelData
                                                            )
                                                    )
                                        } != null
                                    }
                                    playerItem?.amount = playerItem?.amount?.minus(matchItem.amount) ?: 0
                                }

                                player.inventory.addItem(item.clone().apply {
                                    itemMeta = itemMeta.apply {
                                        lore(null)
                                    }
                                })
                            } else {
                                player.playSound(failSound)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkPotMeta(it: ItemStack, effectType: PotionEffectType): Boolean {
        if (it.itemMeta is PotionMeta) {
            val meta = it.itemMeta as PotionMeta
            return meta.customEffects.any { it.type == effectType } || meta.basePotionType == PotionType.valueOf(
                effectType.toString()
            )
        }
        return false
    }
}