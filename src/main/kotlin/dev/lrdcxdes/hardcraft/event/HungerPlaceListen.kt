package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class HungerPlaceListen : Listener {
    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (Hardcraft.instance.random.nextInt(100) < 90) {
            return
        }
        if (event.player.saturation > 0) {
            event.player.saturation -= 1
        } else {
            event.player.foodLevel -= 1
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (Hardcraft.instance.random.nextInt(100) < 90) {
            return
        }
        if (event.player.saturation > 0) {
            event.player.saturation -= 1
        } else {
            event.player.foodLevel -= 1
        }
    }
}