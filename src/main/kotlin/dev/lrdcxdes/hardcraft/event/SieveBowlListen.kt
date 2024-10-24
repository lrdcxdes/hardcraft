package dev.lrdcxdes.hardcraft.event

import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

data class ItemChance(val items: List<ItemStack>, val chance: Double)

class SieveBowlListen : Listener {
    private val sieveItems: Map<Material, List<ItemChance>> = buildMap {
        // Common items for sand-like materials
        val sandItems = listOf(
            ItemChance(listOf(ItemStack(Material.KELP)), 0.5),
            ItemChance(
                listOf(
                    ItemStack(Material.IRON_NUGGET),
                    ItemStack(Material.GOLD_NUGGET),
                    ItemStack(Material.PRISMARINE_SHARD),
                    ItemStack(Material.ROTTEN_FLESH),
                    ItemStack(Material.PRISMARINE_CRYSTALS),
                    ItemStack(Material.STICK),
                    ItemStack(Material.CLAY_BALL),
                    ItemStack(Material.INK_SAC),
                    ItemStack(Material.BRICK)
                ), 0.85
            ),
            ItemChance(
                listOf(
                    ItemStack(Material.TURTLE_HELMET),
                    ItemStack(Material.AMETHYST_SHARD),
                    ItemStack(Material.GOLD_INGOT),
                    ItemStack(Material.IRON_INGOT),
                    ItemStack(Material.COPPER_INGOT),
                    ItemStack(Material.GLOW_INK_SAC),
                    ItemStack(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE),
                    ItemStack(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE),
                    ItemStack(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE),
                    ItemStack(Material.NAME_TAG),
                    ItemStack(Material.MUSIC_DISC_RELIC)
                ), 1.0
            )
        )

        put(
            Material.GRAVEL, listOf(
                ItemChance(listOf(ItemStack(Material.FLINT)), 0.5),
                ItemChance(
                    listOf(
                        ItemStack(Material.RAW_IRON),
                        ItemStack(Material.RAW_COPPER),
                        ItemStack(Material.RAW_GOLD),
                        ItemStack(Material.ROTTEN_FLESH),
                        ItemStack(Material.BONE),
                        ItemStack(Material.STICK),
                        ItemStack(Material.CLAY_BALL)
                    ), 0.85
                ),
                ItemChance(
                    listOf(
                        ItemStack(Material.QUARTZ),
                        ItemStack(Material.IRON_INGOT),
                        ItemStack(Material.GOLD_INGOT),
                        ItemStack(Material.COPPER_INGOT),
                        ItemStack(Material.PHANTOM_MEMBRANE),
                        ItemStack(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE),
                        ItemStack(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE),
                        ItemStack(Material.SADDLE),
                        ItemStack(Material.LEAD),
                        ItemStack(Material.MUSIC_DISC_11)
                    ), 1.0
                )
            )
        )

        put(Material.SAND, sandItems)
        put(Material.RED_SAND, sandItems)

        put(
            Material.SOUL_SAND, listOf(
                ItemChance(listOf(ItemStack(Material.COAL)), 0.5),
                ItemChance(
                    listOf(
                        ItemStack(Material.GLOWSTONE_DUST),
                        ItemStack(Material.GUNPOWDER),
                        ItemStack(Material.REDSTONE)
                    ), 0.85
                ),
                ItemChance(
                    listOf(
                        ItemStack(Material.BLAZE_POWDER),
                        ItemStack(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE),
                        ItemStack(Material.MAGMA_CREAM),
                        ItemStack(Material.MUSIC_DISC_PIGSTEP),
                        ItemStack(Material.NETHER_WART)
                    ), 1.0
                )
            )
        )
    }

    private fun sieveItem(type: Material): ItemStack? {
        val itemChances = sieveItems[type] ?: return null
        val rand = Math.random()

        for (itemChance in itemChances) {
            if (rand < itemChance.chance) {
                return itemChance.items.random().clone()
            }
        }

        return null
    }

    private fun Player.isLookingAtWater(): Boolean {
        return rayTraceBlocks(5.0, FluidCollisionMode.ALWAYS)?.hitBlock?.type == Material.WATER
    }

    private fun processSieving(player: Player, item: ItemStack, material: Material) {
        // Remove one item from the stack
        val itemToRemove = player.inventory.firstOrNull { it?.type == material } ?: return
        itemToRemove.amount -= 1

        // Give items from sieving
        val sievedItem = sieveItem(material)
        if (sievedItem != null) {
            player.inventory.addItem(sievedItem)
        }

        // Damage the brush
        val meta = item.itemMeta as? Damageable ?: return
        meta.damage += 1
        if (meta.damage >= item.type.maxDurability) {
            item.amount -= 1
        } else {
            item.itemMeta = meta
        }
    }

    @EventHandler
    fun onSieve(event: PlayerInteractEvent) {
        // Check if it's a right-click action
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return

        // Check if player is using a brush
        val item = event.item ?: return
        if (item.type != Material.BRUSH) return

        // Check if player is looking at water
        if (!event.player.isLookingAtWater()) return

        // Try to process each material type
        val materialsToCheck = listOf(
            Material.GRAVEL,
            Material.SAND,
            Material.RED_SAND,
            Material.SOUL_SAND
        )

        for (material in materialsToCheck) {
            if (event.player.inventory.any { it?.type == material }) {
                processSieving(event.player, item, material)
                break
            }
        }
    }
}