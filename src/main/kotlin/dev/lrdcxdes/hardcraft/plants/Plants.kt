package dev.lrdcxdes.hardcraft.plants

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.nms.mobs.CustomSilverfish
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Bisected
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet


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
    private val ferns = ConcurrentHashMap<Long, CopyOnWriteArraySet<Block>>()
    private val plugin = Hardcraft.instance

    fun addFern(block: Block) {
        if (block.type != Material.FERN) return

        val chunkKey = (block.chunk.x.toLong() shl 32) or block.chunk.z.toLong()
        if (!block.hasMetadata("nowTime")) {
            block.setMetadata("nowTime", FixedMetadataValue(plugin, 0L))
            block.setMetadata(
                "endTime",
                FixedMetadataValue(plugin, 60L * (5 + plugin.random.nextInt(3)))
            )
        }
        ferns.computeIfAbsent(chunkKey) { CopyOnWriteArraySet() }.add(block)
    }

    fun removeFern(block: Block, removeMetadata: Boolean = true) {
        val chunkKey = (block.chunk.x.toLong() shl 32) or block.chunk.z.toLong()
        ferns[chunkKey]?.remove(block)
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
        if (up.isSolid || up.type == Material.BEDROCK || fern.lightLevel < 11) {
            return
        }

        val nowTime = fern.getMetadata("nowTime").firstOrNull()?.asLong() ?: return
        val endTime = fern.getMetadata("endTime").firstOrNull()?.asLong() ?: return

        if (nowTime + 60 >= endTime) {
            removeFern(fern)
            object : BukkitRunnable() {
                override fun run() {
                    fern.type = Material.LARGE_FERN
                    val data: Bisected = fern.blockData as Bisected
                    data.half = Bisected.Half.BOTTOM
                    fern.blockData = data
                    // set top block also
//                    val topFern = fern.getRelative(0, 1, 0)
//                    topFern.type = Material.LARGE_FERN
//                    val topData: Bisected = topFern.blockData as Bisected
//                    topData.half = Bisected.Half.TOP
//                    topFern.blockData = topData
                }
            }.runTask(plugin)
        } else {
            fern.setMetadata("nowTime", FixedMetadataValue(plugin, nowTime + 60))
        }
    }

    fun init() {
        object : BukkitRunnable() {
            override fun run() {
                ferns.values.forEach { blockSet ->
                    blockSet.forEach { growFern(it) }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20L * 60)
    }

    fun getFerns(chunkKey: Long) = ferns[chunkKey] ?: emptySet()
    fun removeChunk(chunkKey: Long) = ferns.remove(chunkKey)
}

class GardenManager {
    private val gardens = ConcurrentHashMap<Long, CopyOnWriteArraySet<Block>>()
    private val world: World = Hardcraft.instance.server.getWorld("world")!!
    private var lastDayState: Boolean = world.isDayTime
    private val spawnSilverfishChance = 0.1

    fun addGarden(block: Block) {
        val chunkKey = (block.chunk.x.toLong() shl 32) or block.chunk.z.toLong()
        gardens.computeIfAbsent(chunkKey) { CopyOnWriteArraySet() }.add(block)
    }

    fun removeGarden(block: Block) {
        val chunkKey = (block.chunk.x.toLong() shl 32) or block.chunk.z.toLong()
        gardens[chunkKey]?.remove(block)
    }

    fun init() {
        object : BukkitRunnable() {
            override fun run() {
                val spawnSilverfish = world.isDayTime && !lastDayState
                lastDayState = world.isDayTime

                val iterator = gardens.values.iterator()
                while (iterator.hasNext()) {
                    val blocks = iterator.next()
                    blocks.removeIf { garden ->
                        val ageable = garden.blockData as? Ageable
                        if (ageable == null || ageable.age < ageable.maximumAge) {
                            true
                        } else {
                            if (spawnSilverfish && Math.random() < spawnSilverfishChance) {
                                object : BukkitRunnable() {
                                    override fun run() {
                                        spawnSilverfishAtGarden(garden)
                                    }
                                }.runTask(Hardcraft.instance)
                            }
                            false
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(Hardcraft.instance, 0, 20L * 60)
    }

    private fun spawnSilverfishAtGarden(garden: Block) {
        garden.type = Material.AIR
        convertToRootedDirt(garden.getRelative(0, -1, 0))

        CustomSilverfish((world as CraftWorld).handle).spawn(
            garden.location.add(0.5, 0.0, 0.5)
        )
    }

    fun convertToRootedDirt(block: Block) {
        if (block.type in Constants.SOIL_TYPES) {
            block.type = Material.ROOTED_DIRT
        }
    }

    fun getGardens(chunkKey: Long) = gardens[chunkKey] ?: emptySet()
    fun removeChunk(chunkKey: Long) = gardens.remove(chunkKey)
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
            else -> handleGardenPlace(event)
        }
    }

    @EventHandler
    fun onRightClickFern(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type == Material.FERN) {
            val nowTime = block.getMetadata("nowTime").firstOrNull()?.asLong() ?: return
            val endTime = block.getMetadata("endTime").firstOrNull()?.asLong() ?: return

            event.player.sendMessage("прошло: $nowTime, конец: $endTime, осталось: ${endTime - nowTime}")
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

        val world = block.world
        val location = block.location

        object : BukkitRunnable() {
            override fun run() {
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
        }.runTask(Hardcraft.instance)
    }

    private fun handleGardenBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type !in Constants.ALLOWED_GARDENS) return

        val ageable = block.blockData as? Ageable ?: return
        if (ageable.age < 1) return

        gardenManager.removeGarden(block)
        gardenManager.convertToRootedDirt(block.getRelative(0, -1, 0))
    }

    private fun handleGardenPlace(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        if (block.type !in Constants.ALLOWED_GARDENS) return

        val ageable = block.blockData as? Ageable ?: return
        if (ageable.age < ageable.maximumAge) return

        gardenManager.addGarden(block)
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
        val chunk = event.chunk

        object : BukkitRunnable() {
            override fun run() {
                processChunkBlocks(chunk)
            }
        }.runTaskAsynchronously(Hardcraft.instance)
    }

    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {
        val chunk = event.chunk
        val chunkKey = (chunk.x.toLong() shl 32) or chunk.z.toLong()

        if (!Hardcraft.instance.server.isStopping) {
            fernManager.removeChunk(chunkKey)
        }
        gardenManager.removeChunk(chunkKey)
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

                        else -> {}
                    }
                }
            }
        }
    }
}