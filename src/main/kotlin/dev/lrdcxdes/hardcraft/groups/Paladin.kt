package dev.lrdcxdes.hardcraft.groups

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Paladin: Listener {
    //Paladin
    //Уникальные способности
    //После блокирования атаки щитом получает бафф на 5 секунд, повышающий регенерацию и сопротивление.

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.entity is Player) {
            val player = event.entity as Player
            // Проверка на блок щитом
            if (player.isBlocking && player.getGroup() == Group.PALADIN) {
                // Бафф на 5 секунд
                // Повышение регенерации и сопротивления

                // /playsound minecraft:entity.warden.heartbeat
                player.playSound(
                    player.location,
                    org.bukkit.Sound.ENTITY_WARDEN_STEP,
                    1f,
                    1f
                )
                player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 0))
                player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 5 * 20, 0))
            }
        }
    }
}