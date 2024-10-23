package dev.lrdcxdes.hardcraft.plants

import dev.lrdcxdes.hardcraft.Hardcraft
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
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

// Common constants
object Constants {
    val SOIL_TYPES = setOf(
        Material.DIRT,
        Material.GRASS_BLOCK,
        Material.PODZOL,
        Material.COARSE_DIRT,
        Material.FARMLAND
    )

    val ALLOWED_GARDENS = setOf(
        Material.WHEAT,
        Material.POTATOES,
        Material.CARROTS,
        Material.BEETROOTS,
        Material.MELON_STEM,
        Material.PUMPKIN_STEM,
        Material.SWEET_BERRY_BUSH
    )
}

class FernManager {
    private val ferns = CopyOnWriteArraySet<Block>()
    private val plugin = Hardcraft.instance

    fun addFern(block: Block) {
        if (block.type != Material.FERN) return

        if (!block.hasMetadata("nowTime")) {
            block.setMetadata("nowTime", FixedMetadataValue(plugin, 0L))
            block.setMetadata(
                "endTime",
                FixedMetadataValue(plugin, 60L * (5 + plugin.random.nextInt(3)))
            )
        }
        ferns.add(block)
    }

    fun removeFern(block: Block, removeMetadata: Boolean = true) {
        ferns.remove(block)
        if (removeMetadata) {
            block.removeMetadata("nowTime", plugin)
            block.removeMetadata("endTime", plugin)
        }
    }

    private fun growFern(fern: Block) {
        if (fern.type != Material.FERN) {
            removeFern(fern)
            return
        }

        val up = fern.getRelative(0, 1, 0)
        if (up.isSolid || up.type == Material.BEDROCK || fern.lightLevel < 11) return

        val nowTime = fern.getMetadata("nowTime").firstOrNull()?.asLong() ?: return
        val endTime = fern.getMetadata("endTime").firstOrNull()?.asLong() ?: return

        if (nowTime + 60 >= endTime) {
            removeFern(fern)
            object : BukkitRunnable() {
                override fun run() {
                    growLargeFern(fern)
                }
            }.runTask(plugin)
        } else {
            fern.setMetadata("nowTime", FixedMetadataValue(plugin, nowTime + 60))
        }
    }

    private fun growLargeFern(fern: Block) {
        fern.type = Material.LARGE_FERN

//        fern.getRelative(0, 1, 0).apply {
//            type = Material.LARGE_FERN
//            val x = blockData as Bisected
//            x.half = Bisected.Half.TOP
//            blockData = x
//        }
    }

    fun init() {
        object : BukkitRunnable() {
            override fun run() {
                ferns.forEach { growFern(it) }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20L * 60)
    }

    fun getFerns() = ferns
}

class GardenManager {
    private val gardens = CopyOnWriteArraySet<Block>()
    private val loadedChunks = ConcurrentHashMap.newKeySet<Long>()
    private val world: World = Hardcraft.instance.server.getWorld("world")!!
    private var lastDayState: Boolean = world.isDayTime
    private val spawnSilverfishChance = 0.1

    fun addGarden(block: Block) {
        gardens.add(block)
    }

    fun removeGarden(block: Block) {
        gardens.remove(block)
    }

    fun processChunk(chunk: Long) {
        if (!loadedChunks.add(chunk)) return
    }

    fun processRemoveChunk(chunk: Long) {
        loadedChunks.remove(chunk)
    }

    fun init() {
        object : BukkitRunnable() {
            override fun run() {
                val spawnSilverfish = world.isDayTime && !lastDayState
                lastDayState = world.isDayTime

                gardens.forEach { garden ->
                    val ageable = garden.blockData as? Ageable
                    if (ageable == null || ageable.age < ageable.maximumAge) {
                        removeGarden(garden)
                        return@forEach
                    }

                    if (spawnSilverfish && Math.random() < spawnSilverfishChance) {
                        spawnSilverfishAtGarden(garden)
                    }
                }
            }
        }.runTaskTimerAsynchronously(Hardcraft.instance, 0, 20L * 60)
    }

    private fun spawnSilverfishAtGarden(garden: Block) {
        object : BukkitRunnable() {
            override fun run() {
                garden.type = Material.AIR
                convertToRootedDirt(garden.getRelative(0, -1, 0))

                CustomSilverfish((world as CraftWorld).handle).spawn(
                    garden.location.add(0.5, 0.0, 0.5)
                )
            }
        }.runTask(Hardcraft.instance)
    }

    fun convertToRootedDirt(block: Block) {
        if (block.type in Constants.SOIL_TYPES) {
            block.type = Material.ROOTED_DIRT
        }
    }

    fun getGardens() = gardens
}

class PlantsEventListener(
    private val fernManager: FernManager,
    private val gardenManager: GardenManager
) : Listener {

    @EventHandler
    fun onFernPlace(event: BlockPlaceEvent) {
        when (event.blockPlaced.type) {
            Material.FERN -> fernManager.addFern(event.blockPlaced)
            Material.LARGE_FERN -> event.isCancelled = true
            else -> return
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        when (block.type) {
            Material.LARGE_FERN -> handleLargeFernBreak(event)
            Material.FERN -> fernManager.removeFern(block)
            else -> handleGardenBreak(event)
        }
    }

    private fun handleLargeFernBreak(event: BlockBreakEvent) {
        val block = event.block
        fernManager.removeFern(block)
        event.isDropItems = false

        // Drop items
        val world = block.world
        val location = block.location

        val zazaCount = 1 + Hardcraft.instance.random.nextInt(3)
        val zaza = ItemStack(Material.GREEN_DYE, zazaCount).apply {
            itemMeta = itemMeta?.apply {
                itemName(Hardcraft.minimessage.deserialize("<color:#00AA00><lang:bts.zaza>"))
                setCustomModelData(3)
            }
        }
        world.dropItemNaturally(location, zaza)

        val fernCount = 1 + Hardcraft.instance.random.nextInt(2)
        world.dropItemNaturally(location, ItemStack(Material.FERN, fernCount))
    }

    private fun handleGardenBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type !in Constants.ALLOWED_GARDENS) return

        val ageable = block.blockData as? Ageable ?: return
        if (ageable.age < 1) return

        gardenManager.removeGarden(block)
        gardenManager.convertToRootedDirt(block.getRelative(0, -1, 0))
    }

    @EventHandler
    fun onGardenGrow(event: BlockGrowEvent) {
        val newState = event.newState
        val ageable = newState.blockData as? Ageable ?: return

        if (newState.type in Constants.ALLOWED_GARDENS &&
            ageable.age == ageable.maximumAge
        ) {
            gardenManager.addGarden(event.block)
        }
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        gardenManager.processChunk(event.chunk.chunkKey)

        object : BukkitRunnable() {
            override fun run() {
                processChunkBlocks(event.chunk)
            }
        }.runTaskAsynchronously(Hardcraft.instance)
    }

    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {
        gardenManager.processRemoveChunk(event.chunk.chunkKey)

        object : BukkitRunnable() {
            override fun run() {
                processRemoveChunkBlocks(event.chunk)
            }
        }.runTaskAsynchronously(Hardcraft.instance)
    }

    private fun processChunkBlocks(chunk: org.bukkit.Chunk) {
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..255) {
                    val block = chunk.getBlock(x, y, z)

                    when (block.type) {
                        Material.FERN -> fernManager.addFern(block)
                        in Constants.ALLOWED_GARDENS -> {
                            val ageable = block.blockData as? Ageable
                            if (ageable?.age == ageable?.maximumAge) {
                                gardenManager.addGarden(block)
                            }
                        }

                        else -> continue
                    }
                }
            }
        }
    }

    private fun processRemoveChunkBlocks(chunk: org.bukkit.Chunk) {
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..255) {
                    val block = chunk.getBlock(x, y, z)

                    when (block.type) {
                        Material.FERN -> fernManager.removeFern(block, false)
                        in Constants.ALLOWED_GARDENS -> gardenManager.removeGarden(block)
                        else -> continue
                    }
                }
            }
        }
    }
}