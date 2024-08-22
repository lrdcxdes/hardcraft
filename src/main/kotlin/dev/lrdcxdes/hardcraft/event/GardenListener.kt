package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.event.Gardens.Companion.gardens
import dev.lrdcxdes.hardcraft.nms.mobs.CustomSilverfish
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.scheduler.BukkitRunnable

class GardenListener : Listener {
    @EventHandler
    fun onGardenGrow(event: BlockGrowEvent) {
        val newState = event.newState
        if (newState.blockData is Ageable) {
            val ageable = newState.blockData as Ageable
            if (ageable.age == ageable.maximumAge) {
                Gardens.addBlock(event.block)
            }
        }
    }

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val block = event.block
        if (block.blockData is Ageable) {
            Gardens.removeBlock(block)
            // set underblock to Material.ROOTED_DIRT
            block.getRelative(0, -1, 0).type = Material.ROOTED_DIRT
        }
    }

    @EventHandler
    fun chunkLoadEvent(event: ChunkLoadEvent) {
        val chunk = event.chunk
        val oldGardensSize = gardens.size
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..255) {
                    val block = chunk.getBlock(x, y, z)
                    if (block.blockData is Ageable) {
                        val ageable = block.blockData as Ageable
                        if (ageable.age == ageable.maximumAge) {
                            Gardens.addBlock(block)
                        }
                    }
                }
            }
        }
        println("Chunk ${chunk.x},${chunk.z} loaded with ${gardens.size - oldGardensSize} gardens")
    }
}

class Gardens {
    companion object {
        val gardens: MutableList<Block> = mutableListOf()
        val world: World = Hardcraft.instance.server.getWorld("world")!!
        var spawnSilverfishChange: Double = 0.1
        private var lastDayState: Boolean = world.isDayTime

        fun init() {
            object : BukkitRunnable() {
                override fun run() {
                    val spawnSilverfish = world.isDayTime && !lastDayState
                    lastDayState = world.isDayTime
                    for (garden in gardens.toList()) {
                        val ageable = garden.blockData as? Ageable
                        if (ageable == null || ageable.age < ageable.maximumAge) {
                            removeBlock(garden)
                            return
                        }
                        if (spawnSilverfish && Math.random() < spawnSilverfishChange) {
                            garden.type = Material.AIR
                            // Set the block below to rooted dirt
                            garden.getRelative(0, -1, 0).type = Material.ROOTED_DIRT
                            val silverfish = CustomSilverfish((world as CraftWorld).handle)
                            silverfish.spawn(garden.location.add(0.5, 0.0, 0.5))
                        }
                    }
                }
            }.runTaskTimer(Hardcraft.instance, 0, 20)
        }

        fun addBlock(block: Block) {
            gardens.add(block)
        }

        fun removeBlock(block: Block) {
            gardens.remove(block)
        }
    }
}
