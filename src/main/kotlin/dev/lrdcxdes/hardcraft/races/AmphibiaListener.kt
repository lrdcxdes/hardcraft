package dev.lrdcxdes.hardcraft.races

import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class AmphibiaListener : Listener {
    private val defaultSpeed = RaceManager.getDefaultAttributes().baseAttributes[Attribute.MOVEMENT_SPEED]!!

    private val lastDamage = mutableMapOf<String, Long>()

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        val race = player.getRace()
        if (!player.isInWater && !player.world.hasStorm() && race == Race.AMPHIBIAN) {
            player.getAttribute(Attribute.MOVEMENT_SPEED)!!.baseValue = defaultSpeed * 0.65
        } else {
            // CIBLElistener
            if (race == Race.CIBLE && (player.isInWater || player.world.hasStorm())) {
                if (lastDamage[player.name] != null && System.currentTimeMillis() - lastDamage[player.name]!! < 1000) {
                    return
                }
                // damage every 1 second
                player.damage(0.5, DamageSource.builder(DamageType.DROWN).build())
                player.playSound(player.location, Sound.ENTITY_WITHER_HURT, 1f, 1f)
                lastDamage[player.name] = System.currentTimeMillis()
            } else if (race == Race.AMPHIBIAN) {
                player.getAttribute(Attribute.MOVEMENT_SPEED)!!.baseValue = defaultSpeed
            }
        }
    }
}