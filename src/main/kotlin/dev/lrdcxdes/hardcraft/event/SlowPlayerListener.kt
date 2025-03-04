package dev.lrdcxdes.hardcraft.event

import ConditionType
import conditionSystem
import dev.lrdcxdes.hardcraft.races.Race
import dev.lrdcxdes.hardcraft.races.getRace
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SlowPlayerListener : Listener {
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val hasPumpkin = player.inventory.helmet?.type == Material.CARVED_PUMPKIN

        if (player.location.y < 0) {
            if (player.getRace() == Race.DWARF) return
            if (hasPumpkin) {
                player.removePotionEffect(PotionEffectType.MINING_FATIGUE)
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        -1,
                        1,
                        false,
                        false,
                        false
                    )
                )
                conditionSystem.removeState(player, ConditionType.SPELUNCAPHOBIA)
            } else {
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        -1,
                        2,
                        false,
                        false,
                        false
                    )
                )
                conditionSystem.addState(player, ConditionType.SPELUNCAPHOBIA, 1)
            }
        } else {
            val nowEffect = player.getPotionEffect(PotionEffectType.MINING_FATIGUE)
            if (nowEffect != null && nowEffect.amplifier == 2) {
                player.removePotionEffect(PotionEffectType.MINING_FATIGUE)
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        -1,
                        1,
                        false,
                        false,
                        false
                    )
                )
                conditionSystem.removeState(player, ConditionType.SPELUNCAPHOBIA)
            }
            else if (!player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        -1,
                        1,
                        false,
                        false,
                        false
                    )
                )
                conditionSystem.removeState(player, ConditionType.SPELUNCAPHOBIA)
            }
        }
    }
}