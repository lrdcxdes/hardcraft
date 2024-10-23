package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

private val DEFUSE_LOOT_RANGE: IntRange = 1..2

class CreeperListener : Listener {
    private val DEFUSE_CHANCE: Double = 0.7

    @EventHandler
    fun onCreeperDefuse(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity.type == EntityType.CREEPER) {
            if (!InteractEntityUtil.canInteract(player.uniqueId, 1)) {
                return
            }
            InteractEntityUtil.setLastClickTime(player.uniqueId)
            if (player.inventory.itemInMainHand.type != Material.SHEARS) {
                return
            }
            val creeper = entity as Creeper
            if (Math.random() > DEFUSE_CHANCE) {
                // boom creeper
                creeper.boom(player)
            } else {
                creeper.defuse(player)
            }
        }
    }

    companion object {
        val DEFUSED = NamespacedKey(Hardcraft.instance, "defused")
    }
}

fun Creeper.defuse(player: Player) {
    if (this.persistentDataContainer.has(CreeperListener.DEFUSED, PersistentDataType.BOOLEAN)) {
        return
    }

    val redstoneAmount = DEFUSE_LOOT_RANGE.random()
    player.sendMessage("You defused the creeper! You got $redstoneAmount redstone.")

    val item = ItemStack(Material.REDSTONE, redstoneAmount)
    val meta = item.itemMeta
    meta.persistentDataContainer.set(Hardcraft.instance.key("itemChecked"), PersistentDataType.BOOLEAN, true)
    item.itemMeta = meta

    player.inventory.addItem(item)

    this.persistentDataContainer.set(CreeperListener.DEFUSED, PersistentDataType.BOOLEAN, true)

    // /summon minecraft:creeper ~ ~ ~ {Fuse:32768}
    this.isIgnited = false
    this.maxFuseTicks = 32768
    this.fuseTicks = 0
}

fun Creeper.boom(player: Player) {
    if (this.persistentDataContainer.has(CreeperListener.DEFUSED, PersistentDataType.BOOLEAN)) {
        return
    }

    player.sendMessage("You failed to defuse the creeper! It exploded.")
    this.explode()
}
