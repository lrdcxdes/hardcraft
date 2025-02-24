package dev.lrdcxdes.hardcraft.customtables

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.MenuType


class Saw(player: Player) {
    private val location: Location = player.location.clone().apply { y = -76.0 }
    private val view: InventoryView = MenuType.STONECUTTER.builder().location(
        location
    ).checkReachable(
        false
    ).build(player)

    fun open(player: Player) {
        player.openInventory(view)
    }

    companion object {
        fun open(player: Player) {
            Saw(player).open(player)
        }
    }
}