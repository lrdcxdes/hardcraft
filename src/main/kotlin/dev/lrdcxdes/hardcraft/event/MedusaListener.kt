package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.entity.Player
import org.bukkit.entity.Squid
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

class MedusaListener : Listener {
    @EventHandler
    fun onPlayerSwim(event: PlayerMoveEvent) {
        val player = event.player
        if (player.isInWater && (!player.world.isDayTime || player.world.hasStorm())) {
            // find nearest squid
            val nearbySquids = player.location.world!!.getNearbyEntities(player.location, 2.0, 2.0, 2.0)
                .filterIsInstance<Squid>().filter { it.hasLineOfSight(player) }
            if (nearbySquids.isNotEmpty()) {
                val squid = nearbySquids.first()
                squid.startStrangling(player)
            }
        }
    }
}

private val squidStranglings = mutableMapOf<UUID, SquidStrangling>()

class SquidStrangling(val squid: Squid, val player: Player) {
    private var task: BukkitTask? = null

    init {
        player.addPotionEffect(PotionEffectType.DARKNESS.createEffect(20 * 10, 1))

        task = object : BukkitRunnable() {
            override fun run() {
                if (player.isDead || squid.isDead) {
                    cancel()
                    return
                }

                squid.damage(1.0)
                player.damage(1.0)
            }

            override fun cancel() {
                super.cancel()
                player.removePassenger(squid)
                player.removePotionEffect(PotionEffectType.DARKNESS)
                squidStranglings.remove(player.uniqueId)
            }
        }.runTaskTimer(Hardcraft.instance, 0, 20L)
    }
}

private fun Squid.startStrangling(player: Player) {
    if (player.uniqueId in squidStranglings) {
        return
    }
    player.addPassenger(this)
    squidStranglings[player.uniqueId] = SquidStrangling(this, player)
}
