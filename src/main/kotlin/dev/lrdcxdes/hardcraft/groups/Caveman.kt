package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Sound
import org.bukkit.entity.Item
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class Caveman : Listener {
    // Cooldown map: player UUID to last throw time (milliseconds)
    private val cooldowns = mutableMapOf<UUID, Long>()

    // Set cooldown duration (in milliseconds)
    private val cooldownTime = 1250L

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        // Check if the player is in the CAVEMAN group
        if (player.getGroup() != Group.CAVEMAN) return
        if (event.action != Action.RIGHT_CLICK_AIR) return

        // Enforce cooldown: if the time since the last throw is less than the cooldown, do nothing
        val now = System.currentTimeMillis()
        val lastThrow = cooldowns[player.uniqueId] ?: 0L
        if (now - lastThrow < cooldownTime) {
            // Optionally, send feedback: player.sendMessage("You must wait before throwing again!")
            player.sendMessage(Hardcraft.minimessage.deserialize(
                "<red><lang:btn.you_must_wait>"
            ))
            return
        }

        // Get the item in the player's main hand
        val itemInHand = player.inventory.itemInMainHand

        // Check if the item is usable or edible (in which case, do nothing)
        if (isUsableOrEdible(itemInHand)) return

        // Cancel any default action
        event.isCancelled = true

        // Clone one item to throw (adjust amount to 1)
        val thrownItemStack = itemInHand.clone().apply { amount = 1 }

        // Remove one item from the player's hand
        itemInHand.amount -= 1

        // Drop the item at the player's location and set its velocity to simulate a throw
        // val dropped: Item = player.world.dropItem(player.eyeLocation, thrownItemStack)
        val dropped: Snowball = player.world.spawn(player.eyeLocation, Snowball::class.java)
        dropped.shooter = player
        dropped.persistentDataContainer.set(
            Hardcraft.instance.key("thrownItem"),
            PersistentDataType.BOOLEAN,
            true
        )
        dropped.item = thrownItemStack
        dropped.velocity = player.eyeLocation.direction.multiply(1.5)

        player.playSound(
            player.location,
            Sound.ENTITY_ALLAY_ITEM_THROWN,
            1f,
            1f
        )

        // Record the throw time for cooldown
        cooldowns[player.uniqueId] = now
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val dropped = event.entity as? Snowball ?: return
        if (dropped.persistentDataContainer.has(
                Hardcraft.instance.key("thrownItem"),
                PersistentDataType.BOOLEAN
            )
        ) {
            dropped.world.dropItem(dropped.location, dropped.item)
            dropped.remove()

            val hitted = event.hitEntity ?: return
            // Knockback a little
            hitted.velocity = dropped.velocity.multiply(1.5)

            // Play sound
            hitted.world.playSound(
                hitted.location,
                Sound.ENTITY_WIND_CHARGE_THROW,
                1f,
                1f
            )
        }
    }

    // Helper function to check if an item is either usable or edible
    private fun isUsableOrEdible(item: ItemStack): Boolean {
        // check item is usable
        @Suppress("DEPRECATION")
        return (item.hasData(DataComponentTypes.CONSUMABLE) || item.type.isEdible || item.type.isEmpty || item.hasData(
            DataComponentTypes.EQUIPPABLE
        ) || item.hasData(DataComponentTypes.INSTRUMENT)
                || item.hasData(DataComponentTypes.DAMAGE) || item.hasData(DataComponentTypes.TOOL)
                || item.hasData(DataComponentTypes.CHARGED_PROJECTILES)
                || item.hasData(DataComponentTypes.FIREWORKS) || item.hasData(DataComponentTypes.INTANGIBLE_PROJECTILE)
                || item.type.isInteractable)
    }
}