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
import java.util.UUID

class SkeletonListener : Listener {
    private val STEAL_GEAR_CHANCE: Double = 0.1
    private val STEAL_GEAR_ARROW_RANGE: IntRange = 1..8
    private val STEAL_GEAR_COOLDOWN: Long = 1000
    private val STEAL_GEAR_SUCCESS_SOUND: (Player) -> Unit =
        { _: Player -> }
    private val STEAL_GEAR_FAILED_SOUND: (Player) -> Unit =
        { player: Player -> player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f) }
    private val STEAL_GEAR_COOLDOWN_SOUND: (Player) -> Unit =
        { player: Player -> player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f) }

    private val lastStealTime = mutableMapOf<UUID, Long>()

    @EventHandler
    fun onSkeletonInteract(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity.type == EntityType.SKELETON) {
            if (lastStealTime.containsKey(player.uniqueId) &&
                System.currentTimeMillis() - lastStealTime[player.uniqueId]!! < STEAL_GEAR_COOLDOWN
            ) {
                STEAL_GEAR_COOLDOWN_SOUND(player)
                return
            }
            lastStealTime[player.uniqueId] = System.currentTimeMillis()
            if (Math.random() < STEAL_GEAR_CHANCE) {
                val skeleton = entity as Skeleton
                val hand = skeleton.equipment.itemInMainHand
                if (hand.type == Material.AIR || hand.amount == 0) {
                    STEAL_GEAR_FAILED_SOUND(player)
                    return
                }
                val arrows = ItemStack(Material.ARROW, STEAL_GEAR_ARROW_RANGE.random())
                skeleton.giveGear(player, arrows)
                STEAL_GEAR_SUCCESS_SOUND(player)
            } else {
                STEAL_GEAR_FAILED_SOUND(player)
            }
        }
    }
}

fun Skeleton.giveGear(player: Player, arrows: ItemStack) {
    val bow = this.equipment.itemInMainHand.clone()
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