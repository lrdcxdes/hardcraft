package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.races.Race
import dev.lrdcxdes.hardcraft.races.getRace
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EntityDamageEntityListener : Listener {
    @EventHandler
    fun onPoison(event: EntityPotionEffectEvent) {
        val player = event.entity as? Player ?: return
        if (event.modifiedType == PotionEffectType.POISON && player.getRace() == Race.DRAGONBORN) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamageEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        val damager = event.damager

        if (damager is Player) {
            // if held item
            if (damager.inventory.itemInMainHand.type == Material.AIR) {
                val damage = event.damage
                event.damage = damage / 2
            }

            if (entity is LivingEntity) {
                val race = damager.getRace()
                if (race == Race.DRAGONBORN) {
                    // Poison Attack Bonus: 20% on poison attacks (DRAGONBORN)
                    val rnd = Hardcraft.instance.random.nextInt(100)
                    if (rnd < 20) {
                        entity.addPotionEffect(PotionEffect(PotionEffectType.POISON, 5 * 20, 0))
                    }
                } else if (race == Race.VAMPIRE) {
                    // Life Steal: heal 50% of the damage dealt (VAMPIRE)
                    val damage = event.damage
                    val heal = damage * 0.5
                    damager.heal(heal)
                } else if (race == Race.SNOLEM) {
                    // 35% chance to slow the target for 3 seconds (SNOLEM)
                    val rnd = Hardcraft.instance.random.nextInt(100)
                    if (rnd < 35) {
                        entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 0))
                    }
                } else if (race == Race.CIBLE) {
                    // 35% to burn enemy when hit (CIBLE)
                    val rnd = Hardcraft.instance.random.nextInt(100)
                    if (rnd < 35) {
                        val ticks = 3 * 20
                        if (entity.fireTicks > 0) {
                            entity.fireTicks = (entity.fireTicks + ticks).coerceAtMost(entity.maxFireTicks)
                        } else {
                            entity.fireTicks = ticks
                        }
                    }
                }
            }
        }

        if (entity is Player && entity.getRace() == Race.VAMPIRE) {
            // if held item
            if (entity.inventory.itemInMainHand.type.name.contains("IRON")) {
                val damage = event.damage
                event.damage = damage * 1.3
            }
        }
    }
}