package dev.lrdcxdes.hardcraft.event

import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
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

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
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
        val world = block.world
        val fallingBlock = world.spawn(
            block.location.add(0.5, 0.0, 0.5),
            FallingBlock::class.java
        )
        fallingBlock.blockData = block.blockData
        block.type = org.bukkit.Material.AIR
    }
}