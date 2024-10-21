package dev.lrdcxdes.hardcraft.seasons

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.Campfire
import org.bukkit.block.Smoker
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class Seasons {
    private val world = Hardcraft.instance.server.worlds[0]
    private val dayKey = Hardcraft.instance.key("day")
    private var task: BukkitTask? = null
    private var day: Long = 0
    private var lastTime: Long = 0

    val season: String
        get() {
            val dayOfYear = (day % 365).toInt()
            return when (dayOfYear) {
                in 0..89 -> "spring"
                in 90..179 -> "summer"
                in 180..269 -> "autumn"
                in 270..364 -> "winter"
                else -> "spring"
            }
        }

    var biomesTemperatures: Map<Biome, Int> = mapOf()

    var seasonTemperature: Int = 0

    init {
        if (world.persistentDataContainer.has(dayKey, PersistentDataType.LONG)) {
            day = world.persistentDataContainer.get(dayKey, PersistentDataType.LONG) ?: 0
        } else {
            world.persistentDataContainer.set(dayKey, PersistentDataType.LONG, 0)
        }

        lastTime = world.time

        seasonTemperature = seasonTemperatures[season]!!.random()
        biomesTemperatures = generateBiomesTemperature()

        task = object : BukkitRunnable() {
            override fun run() {
                if (world.time < lastTime) {
                    day++
                    world.persistentDataContainer.set(dayKey, PersistentDataType.LONG, day)
                    seasonTemperature = seasonTemperatures[season]!!.random()
                    biomesTemperatures = generateBiomesTemperature()
                }
                lastTime = world.time
            }
        }.runTaskTimer(Hardcraft.instance, 0, 20L)
    }

    fun getDay(): Long {
        return day
    }

    fun getTemperature(): Int {
        return seasonTemperature
    }

    fun getTemperature(biome: Biome): Int {
        return biomesTemperatures[biome] ?: seasonTemperature
    }

    fun getTemperature(player: Player): Int {
        var envTemp = getTemperature(player.location.block.biome)
        // using player.location.block.lightFromSky / 2 go near to 0
        val lightTemp = (player.location.block.lightFromSky - 15) / 2
        if (lightTemp < 0) {
            if (envTemp > 0) {
                envTemp += lightTemp
                if (envTemp < 0) {
                    envTemp = 0
                }
            } else {
                envTemp -= lightTemp
                if (envTemp > 0) {
                    envTemp = 0
                }
            }
        }
        val blockTemp = player.location.block.lightFromBlocks / 2
        envTemp += blockTemp

        // calculate smoker and campfire
        // if player is near a campfire or smoker then add 7-10°C to the temperature
        val campfire = player.findNearBlock(ignoreWalls = false, range = 5) { it.type.name.contains("CAMPFIRE") && it.lightLevel > 0 }
        val smoker = player.findNearBlock(ignoreWalls = false, range = 5) { it.type.name.contains("SMOKER") && it.lightLevel > 0 }

        if (campfire != null) {
            envTemp += 7
        }
        if (smoker != null) {
            envTemp += 10
        }

        return envTemp
    }

    fun getTemperature(block: Block): Int {
        var envTemp = getTemperature(block.biome)
        val lightTemp = (block.lightFromSky - 15) / 2
        if (lightTemp < 0) {
            if (envTemp > 0) {
                envTemp += lightTemp
                if (envTemp < 0) {
                    envTemp = 0
                }
            } else {
                envTemp -= lightTemp
                if (envTemp > 0) {
                    envTemp = 0
                }
            }
        }
        val blockTemp = block.lightFromBlocks / 2
        envTemp += blockTemp
        return envTemp
    }

    fun getSkyLightTemp(player: Player): Int {
        var envTemp = getTemperature(player.location.block.biome)
        val lightTemp = (player.location.block.lightFromSky - 15) / 2
        if (lightTemp < 0) {
            if (envTemp > 0) {
                envTemp += lightTemp
                return if (envTemp < 0) {
                    0
                } else {
                    lightTemp
                }
            } else {
                envTemp -= lightTemp
                return if (envTemp > 0) {
                    0
                } else {
                    -lightTemp
                }
            }
        }
        return 0
    }

    fun getBlockTemp(block: Block): Int {
        return block.lightFromBlocks / 2
    }

    fun generateBiomesTemperature(): Map<Biome, Int> {
        return Biome.entries.associateWith { biome ->
            val temperature = seasonTemperature + (biomeMap[biome] ?: 0)
            temperature
        }
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

private fun Player.findNearBlock(
    ignoreWalls: Boolean,
    range: Int,
    blockPredicate: (Block) -> Boolean
): Block? {
    val loc = location
    val x = loc.blockX
    val y = loc.blockY
    val z = loc.blockZ
    for (i in -range..range) {
        for (j in -range..range) {
            for (k in -range..range) {
                val block = world.getBlockAt(x + i, y + j, z + k)
                if (blockPredicate(block)) {
                    println("Found block: $block")
                    println("Block light level: ${block.lightLevel}")
                    if (ignoreWalls) {
                        return block
                    } else {
                        // Perform ray tracing to ensure the block is not behind a wall
                        val playerEyeLoc = eyeLocation
                        val targetBlockLoc = block.location.add(0.5, 0.5, 0.5) // Center of the block

                        val direction = targetBlockLoc.toVector().subtract(playerEyeLoc.toVector()).normalize()

                        // Perform a ray trace from the player's eye location to the block
                        val rayTraceResult = world.rayTraceBlocks(
                            playerEyeLoc,
                            direction,
                            playerEyeLoc.distance(targetBlockLoc)
                        )

                        println("Ray trace hitblock: ${rayTraceResult?.hitBlock}")

                        // If the ray trace hits the same block or doesn't hit anything, apply the effect
                        if (rayTraceResult == null || rayTraceResult.hitBlock == block) {
                            return block
                        }
                    }
                }
            }
        }
    }
    return null
}

fun Player.getTemperature(): Int {
    return Hardcraft.instance.seasons.getTemperature(this)
}
