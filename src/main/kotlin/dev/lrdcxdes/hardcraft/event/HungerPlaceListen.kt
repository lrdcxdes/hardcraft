package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.races.Race
import dev.lrdcxdes.hardcraft.races.getRace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class HungerPlaceListen : Listener {
    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (Hardcraft.instance.random.nextInt(100) < 90) {
            return
        }
        // check if easy block to break, then ignore
        if (event.block.type.hardness < 0.201) {
            return
        }
        if (event.player.getRace() == Race.SKELETON) return
        if (event.player.saturation > 0) {
            event.player.saturation -= 1
        } else {
            event.player.foodLevel -= 1
        }
    }
}