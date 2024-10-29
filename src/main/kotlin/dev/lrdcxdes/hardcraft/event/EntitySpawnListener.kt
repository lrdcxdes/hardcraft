package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.nms.mobs.*
import dev.lrdcxdes.hardcraft.nms.mobs.CustomSlime
import dev.lrdcxdes.hardcraft.nms.mobs.CustomHorse
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerType
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType

class EntitySpawnListener : Listener {
    private val key = Hardcraft.instance.key("customEntity")
    private val rnd = (Math.random() * 1000000).toInt().toString()

    fun setupGoals(event: CreatureSpawnEvent?, entity: Entity, loc: Location) {
        if (event?.spawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return
        }

        if (entity.persistentDataContainer.has(key, PersistentDataType.STRING)
            && entity.persistentDataContainer[key, PersistentDataType.STRING] == rnd
        ) {
            return
        }

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
                val baby = !entity.isAdult
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val cow = CustomCow((loc.world as CraftWorld).handle, isFriendly)
                cow.isBaby = baby
                cow.spawn(loc)
            }

            is Sheep -> {
                if ((entity as CraftEntity).handle is CustomSheep) {
                    return
                }
                val baby = !entity.isAdult
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val sheep = CustomSheep((loc.world as CraftWorld).handle, isFriendly)
                sheep.isBaby = baby
                sheep.spawn(loc)
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

                val baby = !entity.isAdult
                val level = entity.villagerLevel
                val profession = entity.profession
                val type = entity.villagerType

                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val villager = CustomVillager((loc.world as CraftWorld).handle, isFriendly)
                villager.isBaby = baby
                villager.apply {
                    var data = villager.villagerData
                    if (event != null && Math.random() < 0.15) {
                        println("proknulo nitwin")
                        data = data.setProfession(VillagerProfession.NITWIT)
                    } else {
                        println("isEvent: ${event != null}, ne proknulo")
                        data = data.setProfession(
                            when (profession) {
                                Villager.Profession.NONE -> VillagerProfession.NONE
                                Villager.Profession.NITWIT -> VillagerProfession.NITWIT
                                Villager.Profession.CLERIC -> VillagerProfession.CLERIC
                                Villager.Profession.FARMER -> VillagerProfession.FARMER
                                Villager.Profession.MASON -> VillagerProfession.MASON
                                Villager.Profession.ARMORER -> VillagerProfession.ARMORER
                                Villager.Profession.BUTCHER -> VillagerProfession.BUTCHER
                                Villager.Profession.CARTOGRAPHER -> VillagerProfession.CARTOGRAPHER
                                Villager.Profession.FISHERMAN -> VillagerProfession.FISHERMAN
                                Villager.Profession.FLETCHER -> VillagerProfession.FLETCHER
                                Villager.Profession.LEATHERWORKER -> VillagerProfession.LEATHERWORKER
                                Villager.Profession.LIBRARIAN -> VillagerProfession.LIBRARIAN
                                Villager.Profession.SHEPHERD -> VillagerProfession.SHEPHERD
                                Villager.Profession.TOOLSMITH -> VillagerProfession.TOOLSMITH
                                else -> VillagerProfession.WEAPONSMITH
                            }
                        )
                    }
                    data = data.setLevel(level)
                    data = data.setType(
                        when (type) {
                            Villager.Type.SNOW -> VillagerType.SNOW
                            Villager.Type.DESERT -> VillagerType.DESERT
                            Villager.Type.JUNGLE -> VillagerType.JUNGLE
                            Villager.Type.PLAINS -> VillagerType.PLAINS
                            Villager.Type.SWAMP -> VillagerType.SWAMP
                            Villager.Type.SAVANNA -> VillagerType.SAVANNA
                            else -> VillagerType.TAIGA
                        }
                    )
                    villager.villagerData = data
                }
                villager.spawn(loc)
            }

            is Horse -> {
                if ((entity as CraftEntity).handle is CustomHorse) {
                    return
                }
                val baby = !entity.isAdult
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val horse = CustomHorse((loc.world as CraftWorld).handle, isFriendly)
                horse.isBaby = baby
                horse.spawn(loc)
            }

            is Pig -> {
                if ((entity as CraftEntity).handle is CustomPig) {
                    return
                }
                val baby = !entity.isAdult
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }

                // random friendly
                val isFriendly = Math.random() < 0.3

                val pig = CustomPig((loc.world as CraftWorld).handle, isFriendly)
                pig.isBaby = baby
                pig.spawn(loc)
            }

            is Chicken -> {
                if ((entity as CraftEntity).handle is CustomChicken) {
                    return
                }
                val baby = !entity.isAdult
                if (event != null) {
                    event.isCancelled = true
                } else {
                    entity.remove()
                }
                val chicken = CustomChicken((loc.world as CraftWorld).handle)
                chicken.isBaby = baby
                chicken.spawn(loc)
            }

            else -> {
                // not custom
                return
            }
        }

        entity.persistentDataContainer.set(key, PersistentDataType.STRING, rnd)
    }

    @EventHandler
    fun onEntitySpawn(event: CreatureSpawnEvent) {
        val entity = event.entity
        val loc = entity.location

        setupGoals(event, entity, loc)
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        for (entity in event.chunk.entities) {
            setupGoals(null, entity, entity.location)
        }
    }

}