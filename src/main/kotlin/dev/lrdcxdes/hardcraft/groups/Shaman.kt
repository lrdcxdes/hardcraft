package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Shaman : Listener {
    // Shaman
    //Уникальные способности
    //Начинает с предметом: палка.
    //Может потратить 5 уровней для призыва цыплёнка.

    enum class Cast {
        BABY_CHICKEN,
        POISON_DART  // 2fp = 5s poison dart
    }

    private val lastCast = mutableMapOf<String, Long>()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val group = event.player.getGroup()
        if (group != Group.SHAMAN) return

        val item = event.item ?: return
        if (item.type == Material.STICK) {
            if (lastCast[event.player.name] != null && System.currentTimeMillis() - lastCast[event.player.name]!! < 5000) {
                event.player.sendMessage(
                    Hardcraft.minimessage.deserialize("<red><lang:btn.cooldown>: ${(5000 - (System.currentTimeMillis() - lastCast[event.player.name]!!)) / 1000} s.")
                )
                return
            }

            // rmb = cast
            // lmb = change cast (cycle)
            if (event.action.isLeftClick) {
                val currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("castShaman"),
                    PersistentDataType.INTEGER
                ) ?: Cast.BABY_CHICKEN.ordinal
                val current = Cast.entries[currentOrdinal]
                val next = when (current) {
                    Cast.BABY_CHICKEN -> Cast.POISON_DART
                    Cast.POISON_DART -> Cast.BABY_CHICKEN
                }

                event.player.persistentDataContainer.set(
                    Hardcraft.instance.key("castShaman"),
                    PersistentDataType.INTEGER,
                    next.ordinal
                )

                event.player.sendMessage(
                    Hardcraft.minimessage.deserialize("<lang:btn.current_cast>: <green>${next.name}")
                )
            } else {
                val currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("castShaman"),
                    PersistentDataType.INTEGER
                ) ?: Cast.BABY_CHICKEN.ordinal
                val cast = Cast.entries[currentOrdinal]

                if (cast == Cast.BABY_CHICKEN) {
                    // summon chicken
                    if (event.player.totalExperience >= 20) {
                        event.player.totalExperience -= 20
                        event.player.world.spawnEntity(event.player.location, EntityType.CHICKEN).apply {
                            if (this is org.bukkit.entity.Breedable) {
                                age = -24000
                            }
                        }
                    } else {
                        event.player.playSound(
                            event.player.location,
                            "minecraft:entity.villager.no",
                            1F,
                            2F
                        )
                    }
                } else if (cast == Cast.POISON_DART) {
                    // poison dart
                    if (event.player.foodLevel >= 2) {
                        event.player.foodLevel -= 2
                        val arrow = event.player.launchProjectile(Arrow::class.java)
                        arrow.shooter = event.player
                        arrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY
                        arrow.addCustomEffect(PotionEffect(PotionEffectType.POISON, 3 * 20, 0), true)
                        event.player.world.playSound(
                            event.player.location,
                            "minecraft:entity.horse.breathe",
                            1f,
                            2f
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
}