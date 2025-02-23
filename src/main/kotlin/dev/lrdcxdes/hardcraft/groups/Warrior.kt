package dev.lrdcxdes.hardcraft.groups

import net.kyori.adventure.sound.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Warrior : Listener {
    // Warrior
    //Уникальные способности
    //После блокирования атаки щитом получает бафф на 4 секунды, повышающий силу и скорость.

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.entity is Player) {
            val player = event.entity as Player
            // Проверка на блок щитом
            if (player.isBlocking && player.getGroup() == Group.WARRIOR) {
                // Бафф на 4 секунды
                // Повышение силы и скорости

                // /playsound minecraft:entity.warden.heartbeat
                player.playSound(
                    player.location,
                    org.bukkit.Sound.ENTITY_WARDEN_STEP,
                    1f,
                    1f
                )
                player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 4 * 20, 0))
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 4 * 20, 0))
            }
        }
    }
}