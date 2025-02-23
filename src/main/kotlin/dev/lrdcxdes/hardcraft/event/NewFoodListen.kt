package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.races.Race
import dev.lrdcxdes.hardcraft.races.getRace
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class NewFoodListen : Listener {
    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        if (event.entity !is Player) return
        val item = event.item.itemStack
        setItemComponents(item, event.entity as Player)
    }

    private fun setItemComponents(item: ItemStack, player: Player) {
        if (item.type.name.contains("_SEEDS")) {
            item.setData(
                DataComponentTypes.CONSUMABLE, Consumable.consumable().consumeSeconds(1F).animation(
                    ItemUseAnimation.EAT
                ).build()
            )
            item.setData(DataComponentTypes.FOOD, FoodProperties.food().nutrition(1).build())
        } else if (item.type.name.contains("COAL")) {
            val race = player.getRace()
            if (race == Race.DWARF) {
                item.setData(
                    DataComponentTypes.CONSUMABLE, Consumable.consumable().consumeSeconds(1.5F).animation(
                        ItemUseAnimation.EAT
                    ).build()
                )
                item.setData(DataComponentTypes.FOOD, FoodProperties.food().nutrition(4).build())
            } else {
                item.unsetData(
                    DataComponentTypes.CONSUMABLE
                )
                item.unsetData(DataComponentTypes.FOOD)
            }
        } else if (item.type.name == "COCOA_BEANS") {
            item.setData(
                DataComponentTypes.CONSUMABLE, Consumable.consumable().consumeSeconds(1F).animation(
                    ItemUseAnimation.EAT
                ).build()
            )
            item.setData(DataComponentTypes.FOOD, FoodProperties.food().nutrition(1).build())
        } else if (item.type.name == "FLOWER_BANNER_PATTERN" && item.itemMeta?.customModelData == 3) {
            item.setData(
                DataComponentTypes.CONSUMABLE, Consumable.consumable().consumeSeconds(5F).animation(
                    ItemUseAnimation.SPYGLASS
                ).hasConsumeParticles(false).sound(
                    NamespacedKey.minecraft("entity.horse.breathe")
                ).build()
            )
        } else if (item.type.name == "BONE") {
            val race = player.getRace()
            if (race == Race.SKELETON) {
                item.setData(
                    DataComponentTypes.CONSUMABLE, Consumable.consumable().consumeSeconds(1F).animation(
                        ItemUseAnimation.EAT
                    ).sound(
                        NamespacedKey.minecraft("entity.skeleton.step")
                    )
                        .build()
                )
            } else {
                item.unsetData(
                    DataComponentTypes.CONSUMABLE
                )
            }
        }
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        if (event.whoClicked !is Player) return
        setItemComponents(item, event.whoClicked as Player)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (!event.action.name.contains("RIGHT")) return
        val item = event.item ?: return
        setItemComponents(item, event.player)
    }
}