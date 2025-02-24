package dev.lrdcxdes.hardcraft.races

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class Cible : Listener {
    @EventHandler
    fun onToggleSneak(event: PlayerToggleSneakEvent) {
        // holding shift - 1 sec levitation
        val player = event.player
        val race = player.getRace()
        if (race != Race.CIBLE) return
        if (!event.isSneaking) return

        player.addPotionEffect(
            org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.LEVITATION,
                40,
                1
            )
        )
    }

    private val lastFireball = mutableMapOf<String, Long>()

    @EventHandler
    fun onUse(event: PlayerInteractEvent) {
        // use 15 exp points to shoot 1 fireball with cd 5 sec
        val player = event.player
        val race = player.getRace() ?: return
        if (race != Race.CIBLE) return
        if (player.isSneaking && event.action.isRightClick && event.item?.type?.isBlock != true) {
            if (lastFireball[player.name] != null && System.currentTimeMillis() - lastFireball[player.name]!! < 5000) {
                return
            }

            if (player.totalExperience >= 15) {
                player.totalExperience -= 15
                player.launchProjectile(
                    org.bukkit.entity.SmallFireball::class.java
                )
                lastFireball[player.name] = System.currentTimeMillis()
            }
        }
    }
}