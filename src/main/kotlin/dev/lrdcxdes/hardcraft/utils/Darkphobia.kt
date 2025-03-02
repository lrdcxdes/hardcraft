package dev.lrdcxdes.hardcraft.utils

import conditionSystem
import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.races.Race
import dev.lrdcxdes.hardcraft.races.getRace
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class Darkphobia : Listener {
    private val players = mutableMapOf<String, DarkphobiaState>()

    data class DarkphobiaState(
        var fearLevel: Double = 0.0,
        var sunLevel: Double = 0.0,
        var soundTimer: Int = 0
    )

    init {
        // Register events
        Hardcraft.instance.server.pluginManager.registerEvents(this, Hardcraft.instance)
        startMainLoop()
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // Reset player's darkphobia state on death
        players.remove(event.entity.name)
        conditionSystem.removeState(event.player, ConditionType.DARKNESS_FEAR)
    }

    private fun updatePlayerState(player: Player) {
        if (player.gameMode.isInvulnerable) {
            return
        }

        val state = players.getOrPut(player.name) { DarkphobiaState() }

        val race = player.getRace()

        // Update fear level based on
        if (race != Race.VAMPIRE && race != Race.SKELETON) {
            val lightLevel = player.eyeLocation.block.lightLevel
            state.fearLevel += if (lightLevel < 6) 5.0 else -20.0
            state.fearLevel = state.fearLevel.coerceIn(0.0, 500.0)
        } else {
            val sunLevel = player.eyeLocation.block.lightFromSky
            // println("Sun level: $sunLevel, Helmet: ${player.inventory.helmet?.type}")
            if (!player.world.isDayTime || sunLevel < 13 || (player.inventory.helmet != null && player.inventory.helmet!!.type != Material.AIR)) {
                state.sunLevel -= 20.0
            } else {
                state.sunLevel += 5.0
            }
            state.sunLevel = state.sunLevel.coerceIn(0.0, 500.0)
        }

        // println("Fear level: ${state.fearLevel}, Sun level: ${state.sunLevel}")

        // Apply effects based on fear level
        object : BukkitRunnable() {
            override fun run() {
                applyEffects(player, state)
            }
        }.runTask(Hardcraft.instance)
    }

    private fun applyEffects(player: Player, state: DarkphobiaState) {
        with(state.fearLevel) {
            when {
                this >= 480.0 -> {
                    player.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, 140, 0, false, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 140, 0, false, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 140, 0, false, false))
                    playSounds(player, state)
                    conditionSystem.addState(player, ConditionType.DARKNESS_FEAR, 4)
                }

                this >= 300.0 -> {
                    player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 140, 0, false, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 140, 0, false, false))
                    playSounds(player, state)
                    conditionSystem.addState(player, ConditionType.DARKNESS_FEAR, 3)
                }

                this >= 180.0 -> {
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 140, 0, false, false))
                    playSounds(player, state)
                    conditionSystem.addState(player, ConditionType.DARKNESS_FEAR, 2)
                }

                this >= 120.0 -> {
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 140, 0, false, false))
                    conditionSystem.addState(player, ConditionType.DARKNESS_FEAR, 1)
                }

                this == 0.0 -> {
                    conditionSystem.removeState(player, ConditionType.DARKNESS_FEAR)
                }

                this < 180.0 -> {
                    state.soundTimer = 0
                }

                else -> {}
            }
        }

        with(state.sunLevel) {
            when {
                this >= 480.0 -> {
                    player.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 140, 0, true, true))
                    player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 140, 0, false, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 140, 0, false, false))
                    conditionSystem.addState(player, ConditionType.LIGHT_SENSITIVITY, 4)
                }

                this >= 300.0 -> {
                    player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 140, 0, false, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 140, 0, false, false))
                    conditionSystem.addState(player, ConditionType.LIGHT_SENSITIVITY, 3)
                }

                this >= 180.0 -> {
                    player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 140, 0, false, false))
                    conditionSystem.addState(player, ConditionType.LIGHT_SENSITIVITY, 2)
                }

                this >= 120.0 -> {
                    player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 140, 0, false, false))
                    conditionSystem.addState(player, ConditionType.LIGHT_SENSITIVITY, 1)
                }

                this == 0.0 -> {
                    conditionSystem.removeState(player, ConditionType.LIGHT_SENSITIVITY)
                }

                else -> {}
            }
        }
    }

    private fun playSounds(player: Player, state: DarkphobiaState) {
        if (state.soundTimer % 30 != 0) {
            state.soundTimer++
            return
        }

        val sounds = listOf(
            Sound.AMBIENT_CAVE to SoundCategory.AMBIENT,
            Sound.BLOCK_STONE_PLACE to SoundCategory.BLOCKS,
            Sound.BLOCK_STONE_BREAK to SoundCategory.BLOCKS,
            Sound.BLOCK_STONE_STEP to SoundCategory.BLOCKS,
            Sound.ENTITY_PLAYER_ATTACK_NODAMAGE to SoundCategory.PLAYERS,
            Sound.ENTITY_CREEPER_PRIMED to SoundCategory.HOSTILE,
            Sound.AMBIENT_CRIMSON_FOREST_MOOD to SoundCategory.AMBIENT,
            Sound.AMBIENT_BASALT_DELTAS_MOOD to SoundCategory.AMBIENT,
            Sound.AMBIENT_WARPED_FOREST_MOOD to SoundCategory.AMBIENT,
        )

        val (sound, category) = sounds.random()

        when (category) {
            SoundCategory.BLOCKS, SoundCategory.PLAYERS -> {
                val randomLoc = player.location.clone().add(
                    Random.nextDouble(-5.0, 5.0),
                    Random.nextDouble(-5.0, 5.0),
                    Random.nextDouble(-5.0, 5.0)
                )
                repeat(Random.nextInt(1, 4)) {
                    player.playSound(randomLoc, sound, 1.0f, 1.0f)
                }
            }

            SoundCategory.AMBIENT -> {
                player.playSound(player.location, sound, 1.0f, 0.5f)
            }

            else -> {
                val behindPlayer = player.location.clone().add(player.location.direction.multiply(-1))
                player.playSound(behindPlayer, sound, 1.0f, 1.0f)
            }
        }

        state.soundTimer++
    }

    private fun startMainLoop() {
        object : BukkitRunnable() {
            override fun run() {
                Hardcraft.instance.server.onlinePlayers.forEach { player ->
                    updatePlayerState(player)
                }
            }
        }.runTaskTimerAsynchronously(Hardcraft.instance, 0, 20L * 5)
    }

    enum class SoundCategory {
        AMBIENT, BLOCKS, PLAYERS, HOSTILE
    }
}