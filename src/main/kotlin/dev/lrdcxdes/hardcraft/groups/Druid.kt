package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.entity.Breedable
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Druid : Listener {
    // Druid
    //Уникальные способности
    //Может призывать мобов с помощью любой книги, затрачивая на это голод.
    //Tadpole: 5 единиц голода
    //Cod Fish: 4 единицы голода
    //Baby Chicken: 6 единиц голода
    //Baby Pig: 8 единиц голода
    //Baby Wolf: 10 единиц голода
    enum class Cast {
        TADPOLE,
        COD_FISH,
        BABY_CHICKEN,
        BABY_PIG,
        BABY_WOLF,
    }

    data class CastAttributes(
        val type: EntityType,
        val hunger: Int,
    )

    private val casts = mapOf(
        Cast.TADPOLE to CastAttributes(EntityType.SALMON, 5),
        Cast.COD_FISH to CastAttributes(EntityType.COD, 4),
        Cast.BABY_CHICKEN to CastAttributes(EntityType.CHICKEN, 6),
        Cast.BABY_PIG to CastAttributes(EntityType.PIG, 8),
        Cast.BABY_WOLF to CastAttributes(EntityType.WOLF, 10),
    )

    private val lastCast = mutableMapOf<String, Long>()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val group = event.player.getGroup()
        if (group != Group.DRUID) return

        val item = event.item ?: return
        if (item.type == Material.BOOK) {
            if (lastCast[event.player.name] != null && System.currentTimeMillis() - lastCast[event.player.name]!! < 1000) {
                event.player.sendMessage(
                    Hardcraft.minimessage.deserialize("<red><lang:btn.cooldown>: ${(1000 - (System.currentTimeMillis() - lastCast[event.player.name]!!)) / 1000} s.")
                )
                return
            }

            // rmb = cast
            // lmb = change cast (cycle)
            if (event.action.isLeftClick) {
                val currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("cast"),
                    PersistentDataType.INTEGER
                ) ?: Cast.TADPOLE.ordinal
                val current = Cast.entries[currentOrdinal]
                val next = when (current) {
                    Cast.TADPOLE -> Cast.COD_FISH
                    Cast.COD_FISH -> Cast.BABY_CHICKEN
                    Cast.BABY_CHICKEN -> Cast.BABY_PIG
                    Cast.BABY_PIG -> Cast.BABY_WOLF
                    Cast.BABY_WOLF -> Cast.TADPOLE
                }
                event.player.persistentDataContainer.set(
                    Hardcraft.instance.key("cast"),
                    PersistentDataType.INTEGER,
                    next.ordinal
                )

                event.player.sendMessage(
                    Hardcraft.minimessage.deserialize("<lang:btn.current_cast>: <green>${next.name}")
                )
            } else {
                val currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("cast"),
                    PersistentDataType.INTEGER
                ) ?: Cast.TADPOLE.ordinal
                val cast = Cast.entries[currentOrdinal]

                val castAttributes = casts[cast] ?: return
                if (event.player.foodLevel >= castAttributes.hunger) {
                    event.player.foodLevel -= castAttributes.hunger
                    event.player.world.spawnEntity(event.player.location, castAttributes.type).apply {
                        if (this is Breedable) {
                            this.age = -24000
                        }
                    }
                    event.player.world.playSound(
                        event.player.location,
                        "entity.fox.aggro",
                        1F,
                        1F
                    )
                } else {
                    event.player.playSound(
                        event.player.location,
                        "minecraft:entity.villager.no",
                        1F,
                        2F
                    )
                }

                lastCast[event.player.name] = System.currentTimeMillis()
            }
        }
    }
}