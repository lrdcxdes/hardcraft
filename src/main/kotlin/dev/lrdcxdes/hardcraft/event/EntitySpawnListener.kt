package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.nms.mobs.*
import org.bukkit.NamespacedKey
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.persistence.PersistentDataType

class EntitySpawnListener : Listener {
    val KEY: NamespacedKey = NamespacedKey(Hardcraft.instance, "CustomEntity")

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val entity = event.entity
        val loc = entity.location

        when (entity) {
            is Zombie -> {
                CustomZombie.setGoals(entity)
            }

            is Skeleton -> {
                CustomSkeleton.setGoals(entity)
            }

            is Creeper -> {
                CustomCreeper.setGoals(entity)
            }

            is Spider -> {
                CustomSpider.setGoals(entity)
            }

            is Slime -> {
                CustomSlime.setGoals(entity)
            }

            is Chicken -> {
                if (entity.persistentDataContainer.has(KEY, PersistentDataType.BOOLEAN)) {
                    return
                }
                event.isCancelled = true
                val chicken = CustomChicken(loc)
                chicken.spawn(loc)
            }

            else -> {
                // not custom
                return
            }
        }
    }
}