package dev.lrdcxdes.hardcraft.raids

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector


class PrivateListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (locInAnyRegion(event.block.location)) {
            event.isCancelled = true
        }
    }

    private fun generateHPBar(nowHp: Int, maxHp: Int): String {
        val hpBar = StringBuilder()
        val hpBarLength = 10
        val hpBarFill = "<color:#ff0000>❤</color>"
        val hpBarEmpty = "<color:#333333>❤</color>"
        val hpBarFillCount = (nowHp * hpBarLength) / maxHp
        val hpBarEmptyCount = hpBarLength - hpBarFillCount
        for (i in 1..hpBarFillCount) {
            hpBar.append(hpBarFill)
        }
        for (i in 1..hpBarEmptyCount) {
            hpBar.append(hpBarEmpty)
        }
        hpBar.append(" <gray>($nowHp/$maxHp)</gray>")
        return hpBar.toString()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDamage(event: BlockDamageEvent) {
        val maxHp = MATERIALS_HP[event.block.type] ?: 1
        val nowHp = event.block.getMetadata("hp").firstOrNull()?.asInt() ?: maxHp
        // show actionbar
        val message = MiniMessage.miniMessage().deserialize(generateHPBar(nowHp, maxHp))
        event.player.sendActionBar(message)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onExplode(e: EntityExplodeEvent) {
        if (e.entityType == EntityType.WIND_CHARGE) {
            // Центр взрыва
            val explosionCenter: Vector = e.location.toVector()

            // Список поврежденных блоков с уменьшенным уроном
            val blocks: MutableList<BlockDamage> = mutableListOf()

            for (block in e.blockList()) {
                // Вычисляем расстояние от блока до центра взрыва
                val distance = block.location.toCenterLocation().toVector().distance(explosionCenter)

                // Вычисляем урон на основе расстояния с учетом порога
                val maxDamage = 100
                val threshold = 1.0
                val damage = if (distance <= threshold) {
                    maxDamage
                } else {
                    val adjustedDistance = distance - threshold
                    val decreaseFactor = 1 + adjustedDistance
                    (maxDamage / decreaseFactor).toInt()
                }

                blocks.add(BlockDamage(block, damage) {
                    block.type = Material.AIR
                    block.removeMetadata("hp", Hardcraft.instance)
                })
            }

            reduceHp(blocks)
        } else {
            e.blockList().removeIf { block: Block -> locInAnyRegion(block.location) }
            for (block in e.blockList()) {
                block.removeMetadata("hp", Hardcraft.instance)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (locInAnyRegion(event.block.location)) {
            event.isCancelled = true
        } else {
            event.block.removeMetadata("hp", Hardcraft.instance)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onIgnite(event: BlockIgniteEvent) {
        if (locInAnyRegion(event.block.location)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val clicked: Block? = event.clickedBlock
        if (clicked == null) {
            if (event.item?.type == Material.FISHING_ROD) {
                event.isCancelled = true
            }
            return
        }
        if (locInAnyRegion(clicked.location)) {
            event.isCancelled = true
        }
    }

    class BlockDamage(val block: Block, val damage: Int, val breakNaturally: () -> Unit)
    class Region(val guardian: Guardian, val lowestPos: Location, val highestPos: Location)

    private val MATERIALS_HP: MutableMap<Material, Int> = mutableMapOf()
    private val HP_MULTIPLIER: Int = 10
    private val REGIONS: MutableSet<Region> = mutableSetOf()

    private fun isInRegion(playerLocation: Location, lowestPos: Location, highestPos: Location): Boolean {
        if (playerLocation.world != lowestPos.world) {
            return false
        }
        val x: Double = playerLocation.x
        val y: Double = playerLocation.y
        val z: Double = playerLocation.z

        val lowx: Double = lowestPos.x
        val lowy: Double = lowestPos.y
        val lowz: Double = lowestPos.z

        val highx: Double = highestPos.x
        val highy: Double = highestPos.y
        val highz: Double = highestPos.z

        return (x in lowx..highx) && (y in lowy..highy) && (z in lowz..highz)
    }

    private fun locInAnyRegion(loc: Location): Boolean {
        for (region in REGIONS) {
            if (isInRegion(loc, region.lowestPos, region.highestPos)) {
                return true
            }
        }
        return false
    }

    init {
        for (material in Material.entries) {
            // #Material.hardness - Example: (Material.STONE, 1.5), (Material.OBSIDIAN, 50)
            if (material.isBlock) {
                val hp = (material.hardness * HP_MULTIPLIER).toInt()
                if (hp >= 100) {
                    println("Material: ${material.name} HP: $hp")
                }
                MATERIALS_HP[material] = hp
            }
        }
    }

    private fun reduceHp(blocks: List<BlockDamage>) {
        for (block in blocks) {
            val material = block.block.type
            val nowHp = block.block.getMetadata("hp").firstOrNull()?.asInt() ?: MATERIALS_HP[material] ?: continue
            val amount = block.damage
            val newHp = nowHp - amount
            if (newHp <= 0) {
                block.breakNaturally()
            } else {
                block.block.setMetadata("hp", FixedMetadataValue(Hardcraft.instance, newHp))
            }
        }
    }

    fun addRegion(guardian: Guardian, subtract: Location, add: Location) {
        REGIONS.add(Region(guardian, subtract, add))
    }

    fun removeRegion(guardian: Guardian) {
        REGIONS.removeIf { region -> region.guardian == guardian }
    }
}