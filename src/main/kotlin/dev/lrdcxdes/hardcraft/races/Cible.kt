package dev.lrdcxdes.hardcraft.races

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.scheduler.BukkitRunnable

class Cible : Listener {
    private val tasks: MutableMap<String, BukkitRunnable> = mutableMapOf()

    @EventHandler
    fun onToggleSneak(event: PlayerToggleSneakEvent) {
        // holding shift - 1 sec levitation
        val player = event.player
        val race = player.getRace() ?: return
        if (race != Race.CIBLE) return
        if (!player.isSneaking) return

        if (tasks.containsKey(player.name)) {
            return
        }

        val task = object : BukkitRunnable() {
            private var ticks: Int = 0

            override fun run() {
                if (!player.isSneaking) {
                    cancel()
                    tasks.remove(player.name)
                    return
                }
                ticks++
                if (ticks == 20) {
                    player.addPotionEffect(
                        org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.LEVITATION,
                            20,
                            1
                        )
                    )
                } else if (ticks >= 40) {
                    cancel()
                    tasks.remove(player.name)
                }
            }
        }
        task.runTaskTimer(Hardcraft.instance, 0, 1L)

        tasks[player.name] = task
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