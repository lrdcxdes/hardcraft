package dev.lrdcxdes.hardcraft.races

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class Goblin(private val plugin: Hardcraft) : Listener {
    // every 1level gain -2% regen cd (max100%) (so from 1200 tick to 1 tick)
    // every 2levels gain 1% dmg - max 65% (from 0.85 to 1.5)
    // every 3levels you got 1 max heart - max 10hearts (20.0)
    private val goblinsMaxHP = RaceManager.getAttributes(Race.GOBLIN)?.baseAttributes?.get(Attribute.MAX_HEALTH)!!
    private val goblinsSpeed = RaceManager.getDefaultAttributes().baseAttributes[Attribute.MOVEMENT_SPEED]!!
    private val goblinsMaxDmg = RaceManager.getAttributes(Race.GOBLIN)?.baseAttributes?.get(Attribute.ATTACK_DAMAGE)!!

    private fun calculateRegenInterval(level: Int): Long {
        val baseInterval = 1200L // начальный интервал в тиках (60 секунд)
        val reduction = (level * 2).coerceAtMost(100) // максимум 100%
        val adjustedInterval = baseInterval * (100 - reduction) / 100
        return adjustedInterval.coerceAtLeast(1L) // минимум 1 тик
    }

    data class Task(val task: BukkitTask, val lastLevel: Int)

    private var regenTasks: MutableMap<UUID, Task> = mutableMapOf()

    private fun startRegenerationTask(player: Player) {
        if (regenTasks.containsKey(player.uniqueId)) {
            val lastLevel = regenTasks[player.uniqueId]?.lastLevel ?: 0
            if (lastLevel == player.level) return
        }
        regenTasks[player.uniqueId]?.task?.cancel() // Останавливаем текущую задачу, если она существует
        val interval = calculateRegenInterval(player.level)

        regenTasks[player.uniqueId] = Task(object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) {
                    cancel()
                    return
                }
                if (player.isDead) return
                if (player.getRace() != Race.GOBLIN) return
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.REGENERATION,
                        20 * 3,
                        0,
                    )
                )
            }
        }.runTaskTimer(plugin, 0, interval), player.level)
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

    // Гоблины не могут получать exp
    @EventHandler
    fun onExpChange(event: PlayerExpChangeEvent) {
        if (event.amount > 0) {
            event.amount = 0
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (player.getRace() != Race.GOBLIN) return
        startRegenerationTask(player)
    }

    @EventHandler
    fun onConsumeGold(event: PlayerItemConsumeEvent) {
        val player = event.player
        if (player.getRace() != Race.GOBLIN) return
        if (event.item.type != Material.GOLD_INGOT) return

        val xp = when (player.level) {
            in 0..10 -> 9 // 1 слиток = 9 опыта до 11 уровня
            in 11..20 -> 6 // 1 слиток = 6 опыта до 21 уровня
            in 21..30 -> 8 // 1 слиток = 8 опыта до 31 уровня
            else -> 9 // 1 слиток = 9 опыта после 30 уровня
        }
        player.giveExp(xp, true)

        applyAttributes(player)
        startRegenerationTask(player)
    }

    private val limitMaxHP = goblinsMaxHP + 20.0
    private val maxDmg = goblinsMaxHP + (goblinsMaxHP * 0.65)

    private fun applyAttributes(player: Player) {
        val level = player.level
        var maxHP = goblinsMaxHP + level / 1.5
        if (maxHP > limitMaxHP) {
            maxHP = limitMaxHP
        }
        var damage = goblinsMaxDmg + level / 200.0
        if (damage > maxDmg) {
            damage = maxDmg
        }
        player.getAttribute(Attribute.MAX_HEALTH)?.baseValue = maxHP
        player.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue = damage
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player
        if (player.getRace() != Race.GOBLIN) return

        // nug = level x 7
        // drop_ingot = nug / 9
        // drop_nug = nug - ingot * 9 (or nug % 9)
        val nug = player.level * 7
        val ingot = nug / 9
        val dropNug = nug % 9

        event.setShouldDropExperience(false)
        val toDrop = mutableListOf<ItemStack>()
        if (dropNug > 0) {
            toDrop.add(ItemStack(Material.GOLD_NUGGET, dropNug))
        }
        if (ingot > 0) {
            toDrop.add(ItemStack(Material.GOLD_INGOT, ingot))
        }
        event.drops.addAll(
            toDrop
        )
    }

    init {
        taskGroupSynergy.runTaskTimer(plugin, 0, 20L * 5L)
    }
}