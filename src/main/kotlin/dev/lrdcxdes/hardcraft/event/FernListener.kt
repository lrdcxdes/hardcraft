package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Bisected
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
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

            val zaza = ItemStack(Material.GREEN_DYE, seedsCount)
            zaza.itemMeta = zaza.itemMeta.apply {
                itemName(Hardcraft.minimessage.deserialize("<color:#00AA00><lang:bts.zaza>"))
                setCustomModelData(3)
            }
            block.world.dropItemNaturally(block.location, zaza)

            val fernCount = 1 + Hardcraft.instance.random.nextInt(2)
            val fern = ItemStack(Material.FERN, fernCount)
            block.world.dropItemNaturally(block.location, fern)
        } else if (block.type == Material.FERN) {
            ferns.remove(block)

            block.removeMetadata("nowTime", Hardcraft.instance)
            block.removeMetadata("endTime", Hardcraft.instance)
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
        if (fern.type != Material.FERN) {
            fern.removeMetadata("nowTime", Hardcraft.instance)
            fern.removeMetadata("endTime", Hardcraft.instance)
            ferns.remove(fern)
            return
        }

        // check block face up relative
        val up = fern.getRelative(0, 1, 0)
        if (up.isSolid || up.type == Material.BEDROCK) {
            return
        }

        val light = fern.lightLevel
        if (light < 11) {
            return
        }

        var nowTime = fern.getMetadata("nowTime").firstOrNull()?.asLong() ?: return
        val endTime = fern.getMetadata("endTime").firstOrNull()?.asLong() ?: return

        nowTime += 60

        val growned = nowTime >= endTime

        if (growned) {
            fern.removeMetadata("nowTime", Hardcraft.instance)
            fern.removeMetadata("endTime", Hardcraft.instance)
            object : BukkitRunnable() {
                override fun run() {
                    fern.type = Material.LARGE_FERN
                    val data = fern.blockData as Bisected
                    data.half = Bisected.Half.BOTTOM
                    fern.blockData = data

                    val fernUp = fern.getRelative(0, 1, 0)
                    fernUp.type = Material.LARGE_FERN
                    val dataUp = fernUp.blockData as Bisected
                    dataUp.half = Bisected.Half.TOP
                    fernUp.blockData = dataUp
                }
            }.runTask(Hardcraft.instance)

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
        }.runTaskTimerAsynchronously(Hardcraft.instance, 0, 20L * 60)
    }
}