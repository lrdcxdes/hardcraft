package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Particle
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Slime
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class SlimeListen : Listener {
    private val lastBreedKey = Hardcraft.instance.key("lastBreed")

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        if (event.entity.type.name == "SLIME") {
            // get passenger and kill him
            event.entity.passengers.forEach {
                if (it.type == EntityType.ITEM_DISPLAY && Math.random() < 0.60) {
                    val itemStack = (it as ItemDisplay).itemStack
                    event.drops.add(itemStack)
                    it.remove()
                } else {
                    it.remove()
                }
            }
        }
    }

    @EventHandler
    fun onPlayerIntreact(event: PlayerInteractAtEntityEvent) {
        if (event.rightClicked.type == EntityType.SLIME) {
            val slime = event.rightClicked as Slime
            if (event.player.isSneaking) {
                if (slime.size <= 1) {
                    // if player has a passenger, ignore
                    if (event.player.passengers.isNotEmpty()) {
                        return
                    }
                    // mini slime
                    event.player.addPassenger(slime)
                }
            } else {
                // TODO: Breed

                if (slime.size > 1) {
                    val item = event.player.inventory.itemInMainHand
                    val isFood = item.type.isEdible

                    if (isFood) {
                        val lastBreed = slime.persistentDataContainer.get(lastBreedKey, PersistentDataType.LONG)
                        if (lastBreed != null) {
                            if (System.currentTimeMillis() - lastBreed < 1000 * 60 * 5) {
                                return
                            }
                        }

                        event.player.persistentDataContainer.set(
                            lastBreedKey,
                            PersistentDataType.LONG,
                            System.currentTimeMillis()
                        )

                        item.amount--

                        val newSlime = slime.world.spawn(slime.location, Slime::class.java)
                        newSlime.size = 1

                        slime.world.spawnParticle(Particle.HEART, slime.location, 10, 0.5, 0.5, 0.5, 0.1)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR) {
            if (event.player.passengers.isNotEmpty()) {
                event.player.passengers.forEach {
                    event.player.removePassenger(it)
                }
            }
        }
    }
}