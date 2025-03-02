package dev.lrdcxdes.hardcraft.event

import conditionSystem
import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*


class DamageWithoutInstrumentListen : Listener {
    private val plugin = Hardcraft.instance

    // Store active damage tasks for players
    private val activeDamageTasks: MutableMap<UUID, Int> = HashMap()

    @EventHandler
    fun onBlockDamage(event: BlockDamageEvent) {
        val player = event.player
        val block = event.block
        val itemInHand = player.inventory.itemInMainHand
        val blockType = block.type


        // Check if block hardness is greater than threshold
        if (block.type.hardness > HARDNESS_THRESHOLD) {
            // Check if the tool is appropriate for the block
            if (!isAppropriateToolForBlock(itemInHand.type, blockType)) {
                // Start damaging the player periodically
                startDamaging(player)

                // Send initial message to player
                // player.sendMessage("§cYou need a proper tool to break this block!")
            }
        }
    }

    @EventHandler
    fun onBlockDamageAbort(event: BlockDamageAbortEvent) {
        // Stop damaging when player stops breaking
        stopDamaging(event.player)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        // Stop damaging when block is broken
        stopDamaging(event.player)
    }

    private fun startDamaging(player: Player) {
        val playerId = player.uniqueId

        // Don't start a new task if one is already running
        if (activeDamageTasks.containsKey(playerId)) {
            return
        }

        conditionSystem.addState(player, ConditionType.MUSCLE_STRAIN)

        // Create repeating task to damage player
        val taskId = object : BukkitRunnable() {
            override fun run() {
                // Check if player is still online
                if (player.isDead || !player.isOnline) {
                    stopDamaging(player)
                    return
                }

                // Apply damage
                player.damage(DAMAGE_AMOUNT)
            }
        }.runTaskTimer(plugin, DAMAGE_INTERVAL_TICKS / 2, DAMAGE_INTERVAL_TICKS).taskId

        // Store task ID
        activeDamageTasks[playerId] = taskId
    }

    private fun stopDamaging(player: Player) {
        val playerId = player.uniqueId
        val taskId = activeDamageTasks.remove(playerId)

        if (taskId != null) {
            plugin.server.scheduler.cancelTask(taskId)
        }

        conditionSystem.removeState(player, ConditionType.MUSCLE_STRAIN)
    }

    private fun isAppropriateToolForBlock(tool: Material, block: Material): Boolean {
        // Check if tool is any tool at all
        if (!isToolMaterial(tool)) {
            return false
        }

        // Wood/Stone/Iron/Diamond/Netherite tools checking
        if (Tag.MINEABLE_PICKAXE.isTagged(block)) {
            return isPickaxe(tool)
        }
        if (Tag.MINEABLE_AXE.isTagged(block)) {
            return isAxe(tool)
        }
        if (Tag.MINEABLE_SHOVEL.isTagged(block)) {
            return isShovel(tool)
        }
        if (Tag.MINEABLE_HOE.isTagged(block)) {
            return isHoe(tool)
        }

        // If block doesn't require specific tool
        return true
    }

    private fun isToolMaterial(material: Material): Boolean {
        return isPickaxe(material) || isAxe(material) || isShovel(material) || isHoe(material)
    }

    private fun isPickaxe(material: Material): Boolean {
        return material == Material.WOODEN_PICKAXE || material == Material.STONE_PICKAXE || material == Material.IRON_PICKAXE || material == Material.GOLDEN_PICKAXE || material == Material.DIAMOND_PICKAXE || material == Material.NETHERITE_PICKAXE
    }

    private fun isAxe(material: Material): Boolean {
        return material == Material.WOODEN_AXE || material == Material.STONE_AXE || material == Material.IRON_AXE || material == Material.GOLDEN_AXE || material == Material.DIAMOND_AXE || material == Material.NETHERITE_AXE
    }

    private fun isShovel(material: Material): Boolean {
        return material == Material.WOODEN_SHOVEL || material == Material.STONE_SHOVEL || material == Material.IRON_SHOVEL || material == Material.GOLDEN_SHOVEL || material == Material.DIAMOND_SHOVEL || material == Material.NETHERITE_SHOVEL
    }

    private fun isHoe(material: Material): Boolean {
        return material == Material.WOODEN_HOE || material == Material.STONE_HOE || material == Material.IRON_HOE || material == Material.GOLDEN_HOE || material == Material.DIAMOND_HOE || material == Material.NETHERITE_HOE
    }

    companion object {
        private const val HARDNESS_THRESHOLD = 0.7
        private const val DAMAGE_AMOUNT = 0.5 // 0.25 heart of damage
        private const val DAMAGE_INTERVAL_TICKS = 4 * 20L // 4 second (80 ticks)
    }
}