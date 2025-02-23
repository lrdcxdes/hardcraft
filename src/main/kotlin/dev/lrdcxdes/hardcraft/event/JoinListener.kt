package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.groups.Group
import dev.lrdcxdes.hardcraft.groups.getGroup
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.scheduler.BukkitRunnable

class JoinListener : Listener {
    private val rock: ItemStack = ItemStack(Material.WOODEN_AXE).apply {
        val meta = itemMeta as Damageable
        itemMeta = meta.apply {
            itemName(Hardcraft.minimessage.deserialize("<lang:bts.rock>"))
            meta.damage = 51

            meta.setCustomModelData(4)

            // hide flags
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
            addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val group = event.player.getGroup()
        if (group == Group.CAVEMAN) {
            // check if not have rock
            val haveRock = event.player.inventory.firstOrNull {
                it != null && it.type == Material.WOODEN_AXE && it.itemMeta.customModelData == 4
            } != null

            if (!haveRock) {
                event.player.inventory.addItem(rock)
            }
        }

        // give player all recipes
        event.player.discoverRecipes(Hardcraft.instance.cc.customRecipesKeys)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        object : BukkitRunnable() {
            override fun run() {
                val group = event.player.getGroup()
                if (group != Group.CAVEMAN) return

                val haveRock = player.inventory.firstOrNull {
                    it != null && it.type == Material.WOODEN_AXE && it.itemMeta.customModelData == 4
                } != null

                if (!haveRock) {
                    player.inventory.addItem(rock)
                }
            }
        }.runTaskLater(Hardcraft.instance, 1)
    }
}
