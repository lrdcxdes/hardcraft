package dev.lrdcxdes.hardcraft.customtables

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class CustomTableListen : Listener {
    private val tables = mapOf(
        Material.FLETCHING_TABLE to FletchingTable(),
        Material.LOOM to Loom(),
        Material.CAULDRON to CauldronTable(),
    )

    private val cooldowns = mutableMapOf<String, Long>()

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        val action = event.action
        if (action != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        val table = tables[block.type]
        if (table == null) {
            // check if Beehive
            if (block.type == Material.BEEHIVE) {
                val beehiveTable = BeehiveTable(event.player)
                beehiveTable.open()
            }
            return
        }
        // if shift then dont cancel event
        if (event.player.isSneaking) return

        if (cooldowns.containsKey(event.player.name)) {
            val lastUse = cooldowns[event.player.name] ?: return
            if (System.currentTimeMillis() - lastUse < 100) {
                return
            }
        }
        cooldowns[event.player.name] = System.currentTimeMillis()

        event.isCancelled = true
        table.openInventory(event.player)
    }
}