package dev.lrdcxdes.hardcraft.customtables

import org.bukkit.Location
import org.bukkit.entity.Player


class Saw(private val player: Player) {
    private val location: Location

    init {
        val loc = player.location.clone()
        loc.y = -76.0
        location = loc
    }

    fun open(player: Player) {
        player.openStonecutter(location, true)
    }

    companion object {
        fun open(player: Player) {
            Saw(player).open(player)
        }
    }
}