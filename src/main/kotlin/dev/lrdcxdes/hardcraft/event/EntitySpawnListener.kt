package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.nms.mobs.*
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent

class EntitySpawnListener : Listener {
    fun setupGoals(event: EntitySpawnEvent?, entity: Entity, loc: Location) {
        when (entity) {
            is Zombie -> {
                CustomZombie.setGoals(entity)
            }

            is Squid -> {
                CustomSquid.setGoals(entity)
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

            is Cow -> {
                if ((entity as CraftEntity).handle is CustomCow) {
                    return
                }
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val cow = CustomCow((loc.world as CraftWorld).handle, isFriendly)
                cow.spawn(loc)
            }

            is Sheep -> {
                if ((entity as CraftEntity).handle is CustomSheep) {
                    return
                }
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val sheep = CustomSheep((loc.world as CraftWorld).handle, isFriendly)
                sheep.spawn(loc)
                sheep.drops
            }

            is Silverfish -> {
                if ((entity as CraftEntity).handle is CustomSilverfish) {
                    return
                }
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                val silverfish = CustomSilverfish((loc.world as CraftWorld).handle)
                silverfish.spawn(loc)
            }

            is Villager -> {
                if ((entity as CraftEntity).handle is CustomVillager) {
                    return
                }
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val villager = CustomVillager((loc.world as CraftWorld).handle, isFriendly)
                villager.spawn(loc)
                villager.drops
            }

            is Horse -> {
                if ((entity as CraftEntity).handle is CustomHorse) {
                    return
                }
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val horse = CustomHorse((loc.world as CraftWorld).handle, isFriendly)
                horse.spawn(loc)
            }

            is Pig -> {
                if ((entity as CraftEntity).handle is CustomPig) {
                    return
                }
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val pig = CustomPig((loc.world as CraftWorld).handle, isFriendly)
                pig.spawn(loc)
            }

            is Chicken -> {
                if ((entity as CraftEntity).handle is CustomChicken) {
                    return
                }
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }
                val chicken = CustomChicken((loc.world as CraftWorld).handle)
                chicken.spawn(loc)
            }

            else -> {
                // not custom
                return
            }
        }
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val entity = event.entity
        val loc = entity.location

        setupGoals(event, entity, loc)
    }
}