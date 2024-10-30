package dev.lrdcxdes.hardcraft.event

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

// materiallike can be a string or material
typealias MaterialLike = Any

class PhysicsPlaceListen : Listener {
    private val ignoreList: List<MaterialLike> = listOf(
        Material.LANTERN,
        Material.CHAIN,
        Material.POINTED_DRIPSTONE,
        "FENCE",
        Material.IRON_BARS,
        Material.SCAFFOLDING,
        Material.END_ROD,
        Material.HONEY_BLOCK,
        Material.SLIME_BLOCK,
        Material.LADDER,
        Material.BELL,
        "WALL",
        "STAIRS",
        Material.END_PORTAL_FRAME,
        Material.BEDROCK,
        Material.LIGHTNING_ROD,
        Material.SHULKER_BOX,
        Material.CONDUIT,
        "BED",
        Material.BARRIER,
        "DOOR",
        "COMMAND_BLOCK",
        Material.HOPPER,
        "PISTON",
        "FROGLIGHT",
    )

    // Check all adjacent blocks for honey/slime
    private val relativePositions = listOf(
        BlockFace.NORTH,
        BlockFace.SOUTH,
        BlockFace.EAST,
        BlockFace.WEST,
        BlockFace.UP
    )

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlace(event: BlockPlaceEvent) {
        if (event.isCancelled) {
            return
        }
        if (event.player.gameMode.isInvulnerable) {
            return
        }
        // replace to falling block
        val block = event.block
        // only if block is solid
        if (!block.type.isSolid) {
            return
        }
        // check if block is in ignore list
        for (ignore in ignoreList) {
            if (ignore is Material && block.type == ignore) {
                return
            } else if (ignore is String && block.type.name.contains(ignore)) {
                return
            }
        }
        val downBlock = block.getRelative(0, -1, 0)
        if (!downBlock.isEmpty) return

        for (face in relativePositions) {
            val relativeBlock = block.getRelative(face)
            val type = relativeBlock.type
            if (type == Material.HONEY_BLOCK || type == Material.SLIME_BLOCK) {
                return
            }
        }

        val world = block.world
        val fallingBlock = world.spawn(
            block.location.add(0.5, 0.0, 0.5),
            FallingBlock::class.java
        )
        fallingBlock.blockData = block.blockData
        block.type = org.bukkit.Material.AIR
    }
}