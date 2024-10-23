package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockGrowEvent

class FoodGrowListener : Listener {
    // -45 -- -17 🟥🟥
    //-16 -- -1 🟥
    //-0 -- +7 🟧
    //+8 -- +16 🟩
    //+17 -- +25 🟩🟩
    //+26 -- +32 🟧
    //+33 -- +39 🟥
    //+40 -- +45 🟥🟥

    //🟥🟥 - гниє в мертвий куст
    //🟥 - не росте
    //🟧 - росте погано
    //🟩 - росте добре
    //🟩🟩  - росте швидче


    @EventHandler
    fun onFoodGrow(event: BlockGrowEvent) {
        val block = event.block
        val temperature = Hardcraft.instance.seasons.getTemperature(block)
        when (temperature) {
            in -45..-17 -> {
                event.isCancelled = true
                block.drops.clear()
                block.breakNaturally()
                block.type = org.bukkit.Material.DEAD_BUSH
            }

            in -16..-1 -> {
                event.isCancelled = true
            }

            in 0..7 -> {
                // 50%
                if (Hardcraft.instance.random.nextBoolean()) {
                    event.isCancelled = true
                }
            }

            in 8..16 -> {
                // 100%
            }

            in 17..25 -> {
                event.isCancelled = true
                // 200%
                val ageable = block.blockData as org.bukkit.block.data.Ageable
                ageable.age = (ageable.age + 2).coerceAtMost(ageable.maximumAge)
                block.blockData = ageable
            }

            in 26..32 -> {
                // 50%
                if (Hardcraft.instance.random.nextBoolean()) {
                    event.isCancelled = true
                }
            }

            in 33..39 -> {
                event.isCancelled = true
            }

            in 40..45 -> {
                event.isCancelled = true
                block.drops.clear()
                block.breakNaturally()
                block.type = org.bukkit.Material.DEAD_BUSH
            }
        }
    }
}