package dev.lrdcxdes.hardcraft.seasons

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.ConcurrentHashMap

class Seasons {
    private val world = Hardcraft.instance.server.worlds[0]
    private val dayKey = Hardcraft.instance.key("day")
    private var task: BukkitTask? = null
    private var day: Long = 0
    private var lastTime: Long = 0

    // Use ConcurrentHashMap for thread-safe caching
    private val playerTemperatureCache = ConcurrentHashMap<Player, CacheEntry>()
    private val blockTemperatureCache = ConcurrentHashMap<Block, CacheEntry>()
    private val biomeTemperatureCache = ConcurrentHashMap<Biome, Int>()

    // Cache validity duration in milliseconds
    private val CACHE_DURATION = 5000L

    // Pre-calculated light modifiers for better performance
    private val lightModifiers = Array(16) { i -> (i - 15) / 2 }

    data class CacheEntry(
        val temperature: Int,
        val timestamp: Long
    )

    val season: String
        get() = when (val dayOfYear = (day % 365).toInt()) {
            in 0..89 -> "spring"
            in 90..179 -> "summer"
            in 180..269 -> "autumn"
            else -> "winter"
        }

    var seasonTemperature: Int = 0
        private set

    init {
        day = world.persistentDataContainer.get(dayKey, PersistentDataType.LONG) ?: 0L
        lastTime = world.time
        seasonTemperature = seasonTemperatures[season]!!.random()
        updateBiomeTemperatures()

        // Schedule day tracking task
        task = object : BukkitRunnable() {
            override fun run() {
                if (world.time < lastTime) {
                    day++
                    world.persistentDataContainer.set(dayKey, PersistentDataType.LONG, day)
                    seasonTemperature = seasonTemperatures[season]!!.random()
                    updateBiomeTemperatures()
                }
                lastTime = world.time
            }
        }.runTaskTimerAsynchronously(Hardcraft.instance, 0, 20L * 10)
    }

    private fun updateBiomeTemperatures() {
        biomeTemperatureCache.clear()
        Biome.entries.forEach { biome ->
            biomeTemperatureCache[biome] = seasonTemperature + (biomeMap[biome] ?: 0)
        }
    }

    fun getTemperature(player: Player): Int {
        val currentTime = System.currentTimeMillis()

        // Check cache
        playerTemperatureCache[player]?.let { cached ->
            if (currentTime - cached.timestamp < CACHE_DURATION) {
                return cached.temperature
            }
        }

        // Calculate base temperature
        var temp = getBiomeTemperature(player.location.block.biome)

        // Apply light modifications
        temp = applyLightModifiers(temp, player.location.block)

        // Calculate nearby heat sources
        temp += calculateHeatSources(player)

        // Apply armor bonuses
        temp += calculateArmorBonus(player)

        // Cache and return
        playerTemperatureCache[player] = CacheEntry(temp, currentTime)
        return temp
    }

    fun getTemperature(block: Block): Int {
        val currentTime = System.currentTimeMillis()

        // Check cache
        blockTemperatureCache[block]?.let { cached ->
            if (currentTime - cached.timestamp < CACHE_DURATION) {
                return cached.temperature
            }
        }

        // Calculate base temperature
        var temp = getBiomeTemperature(block.biome)

        // Apply light modifications
        temp = applyLightModifiers(temp, block)

        // Apply packed_ice or magma if under
        if (block.type.name.contains("PACKED_ICE")) {
            temp = -15
        } else if (block.type.name.contains("MAGMA")) {
            temp = 15
        } else {
            // if under block is packed_ice or magma, apply that temperature
            val underBlock = block.getRelative(0, -1, 0)
            if (underBlock.type.name.contains("PACKED_ICE")) {
                temp += -15
            } else if (underBlock.type.name.contains("MAGMA")) {
                temp += 15
            }
        }

        // Cache and return
        blockTemperatureCache[block] = CacheEntry(temp, currentTime)
        return temp
    }

    private fun getBiomeTemperature(biome: Biome): Int =
        biomeTemperatureCache[biome] ?: seasonTemperature

    private fun applyLightModifiers(baseTemp: Int, block: Block): Int {
        var temp = baseTemp

        // Use pre-calculated light modifiers
        val skyMod = lightModifiers[block.lightFromSky.toInt()]
        if (skyMod < 0) {
            temp = if (temp > 0) {
                maxOf(0, temp + skyMod)
            } else {
                minOf(0, temp - skyMod)
            }
        }

        // Add block light contribution
        temp += block.lightFromBlocks.toInt() / 2
        return temp
    }

    private fun calculateHeatSources(player: Player): Int {
        var heatBonus = 0

        // Optimize block checking by checking only relevant blocks
        val loc = player.location
        val range = 5
        val minX = loc.blockX - range
        val minY = loc.blockY - range
        val minZ = loc.blockZ - range
        val maxX = loc.blockX + range
        val maxY = loc.blockY + range
        val maxZ = loc.blockZ + range

        // Check for heat sources
        blockLoop@ for (x in minX..maxX step 2) {
            for (y in minY..maxY step 2) {
                for (z in minZ..maxZ step 2) {
                    val block = world.getBlockAt(x, y, z)
                    when {
                        block.type.name.contains("CAMPFIRE") && block.lightLevel > 0 -> {
                            heatBonus += 7
                            break@blockLoop
                        }

                        block.type.name.contains("SMOKER") && block.lightLevel > 0 -> {
                            heatBonus += 10
                            break@blockLoop
                        }
                    }
                }
            }
        }

        return heatBonus
    }

    private fun calculateArmorBonus(player: Player): Int {
        var bonus = 0

        player.inventory.armorContents.forEach { item ->
            when {
                item == null -> return@forEach
                item.type.name.contains("LEATHER") -> {
                    bonus += when {
                        item.type.name.contains("HELMET") -> 3
                        item.type.name.contains("CHESTPLATE") -> 6
                        item.type.name.contains("LEGGINGS") -> 5
                        item.type.name.contains("BOOTS") -> 6
                        else -> 0
                    }
                }

                else -> {
                    bonus += when {
                        item.type.name.contains("HELMET") -> 2
                        item.type.name.contains("CHESTPLATE") -> 2
                        item.type.name.contains("LEGGINGS") -> 2
                        item.type.name.contains("BOOTS") -> 1
                        else -> 0
                    }
                }
            }
        }

        return bonus
    }

    companion object {
        private val seasonTemperatures = mapOf(
            "spring" to (-6..11),
            "summer" to (12..25),
            "autumn" to (-11..5),
            "winter" to (-25..-12)
        )

        private val biomeMap = mapOf(
            Biome.THE_VOID to 0,
            Biome.PLAINS to 0,
            Biome.SUNFLOWER_PLAINS to 5,
            Biome.SNOWY_PLAINS to -17,
            Biome.ICE_SPIKES to -14,
            Biome.DESERT to 20,
            Biome.SWAMP to 13,
            Biome.MANGROVE_SWAMP to 13,
            Biome.FOREST to -3,
            Biome.FLOWER_FOREST to 0,
            Biome.BIRCH_FOREST to 3,
            Biome.DARK_FOREST to -7,
            Biome.OLD_GROWTH_BIRCH_FOREST to 5,
            Biome.OLD_GROWTH_PINE_TAIGA to -5,
            Biome.OLD_GROWTH_SPRUCE_TAIGA to -5,
            Biome.TAIGA to -5,
            Biome.SNOWY_TAIGA to -15,
            Biome.SAVANNA to 10,
            Biome.SAVANNA_PLATEAU to 10,
            Biome.WINDSWEPT_HILLS to -5,
            Biome.WINDSWEPT_GRAVELLY_HILLS to -5,
            Biome.WINDSWEPT_FOREST to 0,
            Biome.WINDSWEPT_SAVANNA to 6,
            Biome.JUNGLE to 15,
            Biome.SPARSE_JUNGLE to 13,
            Biome.BAMBOO_JUNGLE to 17,
            Biome.BADLANDS to 22,
            Biome.ERODED_BADLANDS to 20,
            Biome.WOODED_BADLANDS to 17,
            Biome.MEADOW to 5,
            Biome.CHERRY_GROVE to 8,
            Biome.GROVE to -12,
            Biome.SNOWY_SLOPES to -17,
            Biome.FROZEN_PEAKS to -22,
            Biome.JAGGED_PEAKS to -15,
            Biome.STONY_PEAKS to -10,
            Biome.RIVER to 0,
            Biome.FROZEN_RIVER to -7,
            Biome.BEACH to 0,
            Biome.SNOWY_BEACH to -12,
            Biome.STONY_SHORE to 0,
            Biome.WARM_OCEAN to 12,
            Biome.LUKEWARM_OCEAN to 9,
            Biome.DEEP_LUKEWARM_OCEAN to 7,
            Biome.OCEAN to 0,
            Biome.DEEP_OCEAN to -3,
            Biome.COLD_OCEAN to -5,
            Biome.DEEP_COLD_OCEAN to -8,
            Biome.FROZEN_OCEAN to -11,
            Biome.DEEP_FROZEN_OCEAN to -14,
            Biome.MUSHROOM_FIELDS to 10,
            Biome.DRIPSTONE_CAVES to -3,
            Biome.LUSH_CAVES to 4,
            Biome.DEEP_DARK to -5,
            Biome.NETHER_WASTES to 15,
            Biome.WARPED_FOREST to 10,
            Biome.CRIMSON_FOREST to 20,
            Biome.SOUL_SAND_VALLEY to 5,
            Biome.BASALT_DELTAS to 25,
            Biome.THE_END to -7,
            Biome.END_HIGHLANDS to -7,
            Biome.END_MIDLANDS to -7,
            Biome.SMALL_END_ISLANDS to -7,
            Biome.END_BARRENS to -7
        )
    }
}

fun Player.getTemperature(): Int {
    return Hardcraft.instance.seasons.getTemperature(this)
}

fun Player.getTemperatureAsync(callback: (Int) -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            callback(this@getTemperatureAsync.getTemperature())
        }
    }.runTaskAsynchronously(Hardcraft.instance)
}