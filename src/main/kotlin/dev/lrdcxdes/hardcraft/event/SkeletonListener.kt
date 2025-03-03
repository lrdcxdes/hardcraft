package dev.lrdcxdes.hardcraft.event

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Skeleton
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class SkeletonListener : Listener {
    private val STEAL_GEAR_CHANCE: Double = 0.1
    private val STEAL_GEAR_ARROW_RANGE: IntRange = 1..8
    private val STEAL_GEAR_DAMAGE_RANGE: IntRange = 150..380
    private val STEAL_GEAR_COOLDOWN: Int = 1 // seconds
    private val STEAL_GEAR_SUCCESS_SOUND: (Player) -> Unit =
        { _: Player -> }
    private val STEAL_GEAR_FAILED_SOUND: (Player) -> Unit =
        { player: Player -> player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f) }
    private val STEAL_GEAR_COOLDOWN_SOUND: (Player) -> Unit =
        { player: Player -> player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f) }

    @EventHandler
    fun onSkeletonInteract(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity.type == EntityType.SKELETON) {
            if (!InteractEntityUtil.canInteract(player.uniqueId, STEAL_GEAR_COOLDOWN)) {
                STEAL_GEAR_COOLDOWN_SOUND(player)
                return
            }
            InteractEntityUtil.setLastClickTime(player.uniqueId)
            if (Math.random() < STEAL_GEAR_CHANCE) {
                val skeleton = entity as Skeleton
                val hand = skeleton.equipment.itemInMainHand
                if (hand.type == Material.AIR || hand.amount == 0) {
                    STEAL_GEAR_FAILED_SOUND(player)
                    return
                }
                val arrows = ItemStack(Material.ARROW, STEAL_GEAR_ARROW_RANGE.random())
                val damage = STEAL_GEAR_DAMAGE_RANGE.random()
                skeleton.giveGear(player, arrows, damage)
                STEAL_GEAR_SUCCESS_SOUND(player)
            } else {
                STEAL_GEAR_FAILED_SOUND(player)
            }
        }
    }
}

fun Skeleton.giveGear(player: Player, arrows: ItemStack, damage: Int) {
    val bow = this.equipment.itemInMainHand.clone().apply {
        this.itemMeta = (this.itemMeta as Damageable).apply {
            this.damage = damage
        }
    }
    this.equipment.setItemInMainHand(null)

    this.lootTable = null

    // if player iteminmainhand is air, set it to bow
    if (player.inventory.itemInMainHand.type == Material.AIR) {
        player.inventory.setItemInMainHand(bow)
    } else {
        player.inventory.addItem(bow)
    }

    player.inventory.addItem(arrows)
}