package dev.lrdcxdes.hardcraft.races

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class Goblin(private val plugin: Hardcraft) : Listener {
    // Regeneration: Self-heal 2 HP every 60 seconds
    //Group Synergy: +5% movement speed per nearby Goblin (max bonus up to +50%, i.e. multiplier up to 0.15000000223)
    private val goblinsMaxHP = RaceManager.getAttributes(Race.GOBLIN)!!.baseAttributes[Attribute.MAX_HEALTH]!!
    private val goblinsSpeed = RaceManager.getDefaultAttributes().baseAttributes[Attribute.MOVEMENT_SPEED]!!

    private val taskRegeneration = object : BukkitRunnable() {
        override fun run() {
            for (player in plugin.server.onlinePlayers) {
                if (player.getRace() != Race.GOBLIN) return
                player.heal(4.0)
            }
        }
    }

    private val taskGroupSynergy = object : BukkitRunnable() {
        override fun run() {
            plugin.server.onlinePlayers.forEach { player ->
                val race = player.getRace()
                if (race == Race.GOBLIN) {
                    updateSpeedBonus(player)
                }
            }
        }
    }

    // Counts nearby goblin players (within a given radius) and returns bonus up to 50%.
    private fun calculateGoblinBonus(player: Player): Double {
        val radius = 10.0  // Example radius; adjust as needed.
        val nearbyGoblinCount = player.location.getNearbyEntities(radius, radius, radius)
            .filterIsInstance<Player>()
            .count { it != player && it.getRace() == Race.GOBLIN }
        val bonus = 0.05 * nearbyGoblinCount
        return bonus.coerceAtMost(0.5)
    }

    private fun updateSpeedBonus(player: Player) {
        val bonusPercentage = calculateGoblinBonus(player)
        val attribute = player.getAttribute(Attribute.MOVEMENT_SPEED) ?: return

        // Calculate bonus as a fraction of the base speed.
        val baseSpeed = goblinsSpeed
        val newSpeed = baseSpeed + baseSpeed * bonusPercentage
        attribute.baseValue = newSpeed
    }

    // Когда гоблин подбирает/получает золото(слиток/кусочек/блок/руда) любым образом (подбор,инвентари) - он получает + к своему exp в зависимости от того какое золото и сколько его он подобрал

    private val goldExp: Map<Material, Int> = mapOf(
        Material.GOLD_NUGGET to 1,
        Material.GOLD_INGOT to 6,
        Material.GOLD_BLOCK to 6 * 9,
        Material.GOLD_ORE to 3,
        Material.DEEPSLATE_GOLD_ORE to 3,
        Material.RAW_GOLD to 3,
        Material.NETHER_GOLD_ORE to 1,
    )

    // Events
    @EventHandler
    fun onGoldPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        if (player.getRace() != Race.GOBLIN) return

        val item = event.item.itemStack
        val exp = goldExp[item.type] ?: return
        player.giveExp(exp * item.amount)

        event.item.itemStack = ItemStack(Material.AIR)
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        val player = event.whoClicked as? Player ?: return
        if (player.getRace() != Race.GOBLIN) return

        val exp = goldExp[item.type] ?: return
        player.giveExp(exp * item.amount)

        event.currentItem = ItemStack(Material.AIR)
    }

    init {
        taskRegeneration.runTaskTimer(plugin, 0, 20L * 60L)
        taskGroupSynergy.runTaskTimer(plugin, 0, 20L * 5L)
    }
}