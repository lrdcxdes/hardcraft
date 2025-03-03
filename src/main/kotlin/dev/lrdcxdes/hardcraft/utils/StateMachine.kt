import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * The main psychological and physical conditions that can affect players
 */
enum class ConditionType {
    DARKNESS_FEAR,    // Fear of darkness - slowness, paranoia, nyctophobia, starvation
    LIGHT_SENSITIVITY, // Sensitivity to light - weakness, blindness, wither
    MUSCLE_STRAIN,     // Physical fatigue from breaking blocks
    COLD_EXPOSURE,     // Freezing conditions - slowness, damage
    HEAT_EXPOSURE,     // Hot conditions - nausea, wither
    SPELUNCAPHOBIA,    // Fear of depths / caves - mining fatigue
}

/**
 * Holds the condition level data with display name and effects
 */
data class ConditionLevel(
    val displayName: String,
    val color: TextColor
)

/**
 * Advanced state machine for player conditions in Hardcraft
 */
class ConditionSystem(private val plugin: Hardcraft) {
    private val playerConditions = ConcurrentHashMap<UUID, EnumMap<ConditionType, Int>>()

    // Define condition level names and colors
    private val conditionLevels = mapOf(
        ConditionType.DARKNESS_FEAR to listOf(
            ConditionLevel("Unease", TextColor.color(0xAAAAAA)),
            ConditionLevel("Paranoia", TextColor.color(0x6A0DAD)),
            ConditionLevel("Nyctophobia", TextColor.color(0x4B0082)),
            ConditionLevel("Void Terror", TextColor.color(0x2E0854))
        ),
        ConditionType.LIGHT_SENSITIVITY to listOf(
            ConditionLevel("Squinting", TextColor.color(0xFFD700)),
            ConditionLevel("Eye Strain", TextColor.color(0xFFA500)),
            ConditionLevel("Solar Blindness", TextColor.color(0xFF4500)),
            ConditionLevel("Radiation Burn", TextColor.color(0xFF0000))
        ),
        ConditionType.MUSCLE_STRAIN to listOf(
            ConditionLevel("Overexertion", TextColor.color(0x8B4513))
        ),
        ConditionType.COLD_EXPOSURE to listOf(
            ConditionLevel("Chilled", TextColor.color(0x87CEEB)),
            ConditionLevel("Frostbitten", TextColor.color(0x1E90FF)),
            ConditionLevel("Hypothermia", TextColor.color(0x0000CD)),
            ConditionLevel("Frozen Core", TextColor.color(0x00008B))
        ),
        ConditionType.HEAT_EXPOSURE to listOf(
            ConditionLevel("Sweating", TextColor.color(0xFFA07A)),
            ConditionLevel("Heat Exhaustion", TextColor.color(0xFF7F50)),
            ConditionLevel("Heatstroke", TextColor.color(0xFF4500))
        ),
        ConditionType.SPELUNCAPHOBIA to listOf(
            ConditionLevel("Cave Fear", TextColor.color(0x8B4513))
        )
    )

    init {
        // Task to update action bars and apply effects every second
        object : BukkitRunnable() {
            override fun run() {
                updateAllPlayers()
            }
        }.runTaskTimer(plugin, 0L, 20L) // Every second
    }

    /**
     * Add a condition with specified level to a player
     * @param player The player to affect
     * @param condition The type of condition
     * @param level The level to set (1-based)
     */
    fun addState(player: Player, condition: ConditionType, level: Int = 1) {
        val maxLevel = conditionLevels[condition]?.size ?: 1
        val validLevel = level.coerceIn(1, maxLevel)

        playerConditions.computeIfAbsent(player.uniqueId) {
            EnumMap(ConditionType::class.java)
        }[condition] = validLevel

        updateActionBar(player)
    }

    /**
     * Remove a condition from a player completely
     * @param player The player to affect
     * @param condition The condition to remove
     */
    fun removeState(player: Player, condition: ConditionType) {
        playerConditions[player.uniqueId]?.remove(condition)
        if (playerConditions[player.uniqueId]?.isEmpty() == true) {
            playerConditions.remove(player.uniqueId)
        }
        updateActionBar(player)
    }

    /**
     * Get the current level of a condition for a player
     * @return The level (0 if not present)
     */
    fun getStateLevel(player: Player, condition: ConditionType): Int {
        return playerConditions[player.uniqueId]?.get(condition) ?: 0
    }

    /**
     * Process all players - update action bars and apply effects
     */
    private fun updateAllPlayers() {
        playerConditions.forEach { (uuid, conditions) ->
            plugin.server.getPlayer(uuid)?.let { player ->
                updateActionBar(player)
            }
        }
    }

    /**
     * Update the action bar for a specific player
     */
    private fun updateActionBar(player: Player) {
        val conditions = playerConditions[player.uniqueId] ?: return
        if (conditions.isEmpty()) return

        val components = mutableListOf<TextComponent>()

        // Take top 3 conditions sorted by level
        val sortedConditions = conditions.entries.sortedByDescending { it.value }.take(3)

        for ((condition, level) in sortedConditions) {
            val levelIndex = level - 1
            val conditionLevel = conditionLevels[condition]?.getOrNull(levelIndex)
                ?: continue

            val component = Component.text()
                .append(Component.text(conditionLevel.displayName).color(conditionLevel.color))
                .build()

            components.add(component)
        }

        // Build the final right-aligned component
        if (components.isNotEmpty()) {
            var finalComponent = components.first()
            if (components.size > 1) {
                finalComponent = finalComponent.append(dividerComponent)
            }
            for (i in 1 until components.size) {
                finalComponent = finalComponent.append(components[i])
                if (i < components.size - 1) {
                    finalComponent = finalComponent.append(dividerComponent)
                }
            }

            player.sendActionBar(finalComponent)
        }
    }

    /**
     * Handle player logout
     */
    fun playerQuit(player: Player) {
        playerConditions.remove(player.uniqueId)
    }

    companion object {
        private val dividerComponent = Component.text(" ◆ ").color(TextColor.color(0xAAAAAA))
    }
}

private val lazyConditionSystem: ConditionSystem by lazy {
    ConditionSystem(Hardcraft.instance)
}

val conditionSystem: ConditionSystem
    get() = lazyConditionSystem