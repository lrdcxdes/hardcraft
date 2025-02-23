package dev.lrdcxdes.hardcraft.races

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ElfListener: Listener {
    @EventHandler
    fun onExpReceive(event: PlayerPickupExperienceEvent) {
        // Receive +50% XP Points
        val player = event.player
        if (player.getRace() != Race.ELF) return
        event.experienceOrb.experience += (event.experienceOrb.experience * 0.5).toInt()
    }
}