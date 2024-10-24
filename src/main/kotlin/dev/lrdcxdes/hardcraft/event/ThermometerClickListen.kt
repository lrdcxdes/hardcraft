package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.seasons.getTemperatureAsync
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class ThermometerClickListen : Listener {
    @EventHandler
    fun onThermometerClick(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        val item = event.item ?: return
        if (item.type != Material.RAW_COPPER) return
        val meta = item.itemMeta
        if (!meta.hasCustomModelData()) return
        val modelData = meta.customModelData
        if (modelData in 3..8) {
            // its thermometer
            val block = event.clickedBlock
            if (block != null) {
                val temp = Hardcraft.instance.seasons.getTemperature(block)
                event.player.sendMessage("Temperature of clicked block: $temp")
            } else {
                event.player.getTemperatureAsync {
                    event.player.sendMessage("Temperature of player: $it")
                }
            }
        }
    }
}