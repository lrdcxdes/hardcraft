package dev.lrdcxdes.hardcraft.races

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

class Skeleton : Listener {
    private val excludeEntities = setOf(
        EntityType.SLIME,
        EntityType.MAGMA_CUBE,
        EntityType.SQUID,
        EntityType.GLOW_SQUID,
        EntityType.AXOLOTL,
        EntityType.TADPOLE,
        EntityType.TURTLE,
        EntityType.ALLAY,
        EntityType.IRON_GOLEM,
        EntityType.SPIDER,
        EntityType.CAVE_SPIDER,
        EntityType.BEE,
        EntityType.CREAKING,
        EntityType.BLAZE,
        EntityType.BREEZE,
        EntityType.GUARDIAN,
        EntityType.ELDER_GUARDIAN,
        EntityType.SHULKER,
        EntityType.ENDERMITE,
        EntityType.SILVERFISH,
        EntityType.GHAST,
        EntityType.PHANTOM,
        EntityType.VEX,
        EntityType.SKELETON,
        EntityType.WITHER_SKELETON,
        EntityType.STRAY,
        EntityType.BOGGED,
        EntityType.STRIDER,
        EntityType.SNOW_GOLEM
    )

    @EventHandler
    fun onKill(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (killer.getRace() != Race.SKELETON) return

        if (excludeEntities.contains(event.entityType)) return

        // 20% chance to get 1 bone
        if (Math.random() < 0.2) {
            event.drops.add(ItemStack(Material.BONE))
        }
    }
}