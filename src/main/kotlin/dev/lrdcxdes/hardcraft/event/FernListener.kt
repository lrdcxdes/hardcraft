package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable

class FernListener : Listener {
    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (event.blockPlaced.type.name == "FERN") {
            // set age as metadata
            event.blockPlaced.setMetadata("nowTime", FixedMetadataValue(Hardcraft.instance, 0))
            event.blockPlaced.setMetadata(
                "endTime",
                FixedMetadataValue(
                    Hardcraft.instance,
                    60 * (5 + Hardcraft.instance.random.nextInt(3))
                )
            )
            ferns.add(event.blockPlaced)
        } else if (event.blockPlaced.type.name == "LARGE_FERN") {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.LARGE_FERN) {
            ferns.remove(block)

            block.removeMetadata("nowTime", Hardcraft.instance)
            block.removeMetadata("endTime", Hardcraft.instance)

            event.isDropItems = false

            // event.player.inventory.addItem(ItemStack(Material.LARGE_FERN))
            val seedsCount = 1 + Hardcraft.instance.random.nextInt(3)
            // event.player.inventory.addItem(ItemStack(Material.FERN, seedsCount))

            // drop item
            block.world.dropItemNaturally(block.location, ItemStack(Material.FERN, seedsCount))
            block.world.dropItemNaturally(block.location, ItemStack(Material.LARGE_FERN))
        }
    }

    fun processInChunk(fern: Block) {
        if (fern.type == Material.FERN) {
            if (!fern.hasMetadata("nowTime")) {
                fern.setMetadata("nowTime", FixedMetadataValue(Hardcraft.instance, 0))
                fern.setMetadata(
                    "endTime",
                    FixedMetadataValue(
                        Hardcraft.instance,
                        60 * (5 + Hardcraft.instance.random.nextInt(3))
                    )
                )
            }
            ferns.add(fern)
        }
    }

    val ferns = mutableListOf<Block>()

    fun growFern(fern: Block) {
        println("Checking x, y, z: ${fern.x}, ${fern.y}, ${fern.z}")

        if (fern.type != Material.FERN) {
            println("not fern")
            fern.removeMetadata("nowTime", Hardcraft.instance)
            fern.removeMetadata("endTime", Hardcraft.instance)
            return
        }

        // check block face up relative
        val up = fern.getRelative(0, 1, 0)
        if (up.isSolid || up.type == Material.BEDROCK) {
            println("up is not air, up: ${up.type}")
            return
        }

        val light = fern.lightLevel
        if (light < 11) {
            println("light is not enough, light: $light")
            return
        }

        var nowTime = fern.getMetadata("nowTime").firstOrNull()?.asLong()
        if (nowTime == null) {
            println("nowTime is null")
            return
        }
        val endTime = fern.getMetadata("endTime").firstOrNull()?.asLong()
        if (endTime == null) {
            println("endTime is null")
            return
        }

        nowTime += 1
        println("nowTime: $nowTime, endTime: $endTime")

        val growned = nowTime >= endTime

        if (growned) {
            println("growned")
            fern.removeMetadata("nowTime", Hardcraft.instance)
            fern.removeMetadata("endTime", Hardcraft.instance)
            fern.type = Material.LARGE_FERN
            ferns.remove(fern)
        } else {
            fern.setMetadata("nowTime", FixedMetadataValue(Hardcraft.instance, nowTime))
        }
    }

    fun init() {
        object : BukkitRunnable() {
            override fun run() {
                for (fern in ferns.toList()) {
                    growFern(fern)
                }
            }
        }.runTaskTimer(Hardcraft.instance, 0, 20L)
    }
}