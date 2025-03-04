package dev.lrdcxdes.hardcraft.weather

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.*

class TornadoCommand(private val naturalDisasters: NaturalDisasters) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // spawn tornado command
        if (sender !is Player) {
            sender.sendMessage(Hardcraft.minimessage.deserialize(
                "<red>Команда доступна только игрокам"
            ))
            return true
        }

        // Find a suitable location for tornado
        val player = sender
        val tornadoLocation: Location = player.location.clone()

        // Adjust Y to ground level
        tornadoLocation.y = (player.world.getHighestBlockYAt(tornadoLocation) + 1).toDouble()

        // Create tornado with random parameters
        val lifetime = ThreadLocalRandom.current().nextInt(NaturalDisasters.MIN_TORNADO_LIFETIME, NaturalDisasters.MAX_TORNADO_LIFETIME)
        val radius = ThreadLocalRandom.current().nextDouble(5.0, 15.0)
        val height = ThreadLocalRandom.current().nextDouble(50.0, 150.0)

        val tornado = naturalDisasters.Tornado(tornadoLocation, radius, height, lifetime)
        naturalDisasters.activeTornadoes.add(tornado)

        sender.sendMessage(
            Hardcraft.minimessage.deserialize(
                "<red>Торнадо сформовано на координатах <white>${tornadoLocation.x}, ${tornadoLocation.y}, ${tornadoLocation.z}"
            )
        )
        return true
    }
}

class NaturalDisasters(private val plugin: Hardcraft) {
    val activeTornadoes: MutableList<Tornado> = ArrayList()

    private val logger = plugin.logger

    fun onEnable() {
        // Start tornado simulation
        startTornadoSimulation()

        // Register events
        plugin.server.pluginManager.registerEvents(WeatherListener(plugin), plugin)

        // Commands
        plugin.getCommand("tornado")?.setExecutor(TornadoCommand(this))

        logger.info("RealisticWeather has been enabled!")
    }

    fun onDisable() {
        // Clean up
        activeTornadoes.clear()
        logger.info("RealisticWeather has been disabled!")
    }

    private fun startTornadoSimulation() {
        // Tornado update task - handles movement and entity interactions
        object : BukkitRunnable() {
            override fun run() {
                updateTornadoes()
            }
        }.runTaskTimer(plugin, 20, TORNADO_UPDATE_INTERVAL.toLong())


        // Tornado spawn task - handles creation of new tornadoes
        object : BukkitRunnable() {
            override fun run() {
                considerSpawningTornadoes()
            }
        }.runTaskTimer(plugin, 600, WEATHER_UPDATE_INTERVAL.toLong())
    }

    private fun considerSpawningTornadoes() {
        if (activeTornadoes.size >= MAX_ACTIVE_TORNADOES) return

        val random = ThreadLocalRandom.current()

        for (world in Bukkit.getWorlds()) {
            // Only consider overworld and only during storms
            if (world.environment != World.Environment.NORMAL || !world.hasStorm()) continue

            // Higher chance during thunderstorms
            val spawnChance = if (world.isThundering) TORNADO_SPAWN_CHANCE * 3 else TORNADO_SPAWN_CHANCE

            if (random.nextDouble() < spawnChance) {
                // Find a suitable location for tornado
                val randomPlayer = getRandomPlayerInWorld(world)
                val tornadoLocation: Location

                if (randomPlayer != null) {
                    // Spawn somewhat near a player but not too close
                    tornadoLocation = randomPlayer.location.clone()
                    tornadoLocation.add(
                        random.nextDouble(-1000.0, 1000.0),
                        0.0,
                        random.nextDouble(-1000.0, 1000.0)
                    )
                } else {
                    // No players in world, pick random location
                    tornadoLocation = Location(
                        world,
                        random.nextDouble(-10000.0, 10000.0),
                        world.getHighestBlockYAt(
                            random.nextDouble(-10000.0, 10000.0).toInt(),
                            random.nextDouble(-10000.0, 10000.0).toInt()
                        ).toDouble(),
                        random.nextDouble(-10000.0, 10000.0)
                    )
                }


                // Adjust Y to ground level
                tornadoLocation.y = (world.getHighestBlockYAt(tornadoLocation) + 1).toDouble()


                // Create tornado with random parameters
                val lifetime = random.nextInt(MIN_TORNADO_LIFETIME, MAX_TORNADO_LIFETIME)
                val radius = random.nextDouble(5.0, 15.0)
                val height = random.nextDouble(50.0, 150.0)

                val tornado = Tornado(tornadoLocation, radius, height, lifetime)
                activeTornadoes.add(tornado)

                logger.info(
                    "Tornado spawned at " + tornadoLocation.x + ", " +
                            tornadoLocation.y + ", " + tornadoLocation.z
                )


                // Announce to nearby players
                for (player in world.players) {
                    if (player.location.distance(tornadoLocation) < 500) {
                        // player.sendMessage("§c§lA tornado has formed nearby! Seek shelter immediately!")
                        player.sendMessage(
                            Hardcraft.minimessage.deserialize(
                                "<red><lang:btn.tornado>"
                            )
                        )
                    }
                }


                // Only spawn one tornado per world per check
                break
            }
        }
    }

    private fun updateTornadoes() {
        val iterator = activeTornadoes.iterator()
        while (iterator.hasNext()) {
            val tornado = iterator.next()

            // Update lifetime
            tornado.decrementLifetime(TORNADO_UPDATE_INTERVAL)

            if (tornado.lifetime <= 0) {
                // Tornado has dissipated
                iterator.remove()
                continue
            }


            // Update tornado position based on movement pattern
            tornado.move()


            // Render tornado particles
            tornado.render()


            // Handle entity interactions
            handleTornadoEntityInteractions(tornado)
        }
    }

    private fun handleTornadoEntityInteractions(tornado: Tornado) {
        val location = tornado.location
        val world = location.world
        val radius = tornado.radius


        // Get entities within tornado's influence radius (slightly larger than visible radius)
        val influenceRadius = radius * 1.5
        val nearbyEntities = world.getNearbyEntities(
            location, influenceRadius,
            tornado.height, influenceRadius
        )

        for (entity in nearbyEntities) {
            // Skip players in creative/spectator mode
            if (entity is Player) {
                if (entity.isFlying || entity.gameMode == GameMode.CREATIVE ||
                    entity.gameMode == GameMode.SPECTATOR
                ) {
                    continue
                }
            }


            // Calculate distance from tornado center (horizontally)
            val entityLoc = entity.location
            val dx = entityLoc.x - location.x
            val dz = entityLoc.z - location.z
            val distance = sqrt(dx * dx + dz * dz)


            // Skip entities outside the tornado's effect radius
            if (distance > influenceRadius) continue


            // Calculate force based on distance (stronger near center)
            val forceFactor = 1.0 - (distance / influenceRadius)


            // Calculate vertical force (lift)
            val verticalForce = 1.0 * forceFactor


            // Calculate rotational force
            val angle = atan2(dz, dx) + Math.PI / 2 // Perpendicular to radius
            val rotationalForce = 1.5 * forceFactor
            val vx = cos(angle) * rotationalForce
            val vz = sin(angle) * rotationalForce


            // Apply forces to entity
            val velocity = entity.velocity
            velocity.add(Vector(vx, verticalForce, vz))
            entity.velocity = velocity


            // Damage entities caught in the tornado core
            if (distance < radius * 0.3) {
                if (entity is Player) {
                    entity.damage(1.0) // 0.5 hearts of damage
                    // entity.sendMessage("§c§lYou're being torn apart by the tornado!")
                    entity.sendMessage(
                        Hardcraft.minimessage.deserialize(
                            "<red><lang:btn.tornado.damage>"
                        )
                    )
                } else {
                    // Non-player entities take more damage
                    entity.fireTicks = 0 // Prevent fire damage in tornado
                }
            }
        }
    }

    private fun getRandomPlayerInWorld(world: World): Player? {
        val players = world.players
        if (players.isEmpty()) return null

        return players[ThreadLocalRandom.current().nextInt(players.size)]
    }

    inner class Tornado(
        val location: Location, var radius: Double, var height: Double, // In ticks
        var lifetime: Int
    ) {
        private val movement: Vector

        init {
            // Initialize random movement vector
            val random = ThreadLocalRandom.current()
            val speed = random.nextDouble(0.1, 0.3)
            val angle = random.nextDouble() * 2 * Math.PI
            this.movement = Vector(
                cos(angle) * speed,
                0.0,
                sin(angle) * speed
            )
        }

        fun decrementLifetime(ticks: Int) {
            lifetime -= ticks


            // Tornado gets smaller as it dissipates
            if (lifetime < 600) { // Last 30 seconds
                radius *= 0.995
                height *= 0.995
            }
        }

        fun move() {
            // Change direction occasionally
            if (ThreadLocalRandom.current().nextDouble() < 0.01) {
                val angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI
                val speed = movement.length()
                movement.setX(cos(angle) * speed)
                movement.setZ(sin(angle) * speed)
            }


            // Move tornado
            location.add(movement)


            // Ensure tornado stays at ground level
            location.y = (location.world.getHighestBlockYAt(location) + 1).toDouble()


            // Avoid going out of world border
            val border: WorldBorder = location.world.worldBorder
            val center: Location = border.center
            val size: Double = border.size / 2

            if (abs(location.x - center.x) > size - radius * 2 ||
                abs(location.z - center.z) > size - radius * 2
            ) {
                // Reverse direction if near border
                movement.setX(-movement.x)
                movement.setZ(-movement.z)
            }
        }

        fun render() {
            val world = location.world


            // Tornado shape parameters
            val baseRadius = radius
            val topRadius = radius * 0.2
            val particleDensity = max(1.0, radius * 0.3) // More particles for larger tornadoes


            // Create the funnel shape
            var y = 0.0
            while (y < height) {
                // Calculate radius at this height (linear interpolation)
                val heightFactor = y / height
                val currentRadius = baseRadius * (1 - heightFactor) + topRadius * heightFactor


                // Number of particles in this ring
                val particleCount = (currentRadius * 8 * particleDensity).toInt()

                for (i in 0..<particleCount) {
                    val angle = (i * 2 * Math.PI / particleCount)


                    // Add some randomness to make it look more natural
                    val randomFactor = 1.0 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1)
                    val x = location.x + cos(angle) * currentRadius * randomFactor
                    val z = location.z + sin(angle) * currentRadius * randomFactor


                    // Spawn particle
                    world.spawnParticle(
                        Particle.CLOUD,
                        x,
                        location.y + y,
                        z,
                        1,  // count
                        0.1, 0.1, 0.1,  // offset
                        0.05 // speed
                    )


                    // Add some dirt particles near the ground
                    if (y < 10) {
                        if (ThreadLocalRandom.current().nextDouble() < 0.2) {
                            // get the block data of the lowest block at this location
                            val b = world.getBlockAt(x.toInt(), location.y.toInt(), z.toInt())
                            var blockData = b.blockData
                            if (b.type == Material.AIR) {
                                blockData = Material.DIRT.createBlockData()
                            }
                            world.spawnParticle(
                                Particle.FALLING_DUST,
                                Location(world, x, location.y + y, z),
                                1,
                                0.1, 0.1, 0.1,
                                0.05,
                                blockData
                            )
                        }
                    }
                }
                y += 2.0
            }


            // Debris and lightning effects for dramatic effect
            if (ThreadLocalRandom.current().nextDouble() < 0.01) {
                val x = location.x + ThreadLocalRandom.current().nextDouble(-radius, radius)
                val z = location.z + ThreadLocalRandom.current().nextDouble(-radius, radius)
                val lightningLoc = Location(world, x, world.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble(), z)
                world.strikeLightningEffect(lightningLoc)
            }
        }
    }

    companion object {
        // Weather update intervals (in ticks)
        private const val WEATHER_UPDATE_INTERVAL = 1200 // 1 minute
        private const val TORNADO_UPDATE_INTERVAL = 2 // Fast updates for tornado movement

        // Tornado spawn chances and parameters
        private const val TORNADO_SPAWN_CHANCE = 0.001 // Chance per weather update during storms
        private const val MAX_ACTIVE_TORNADOES = 3
        const val MIN_TORNADO_LIFETIME = 1200 // 1 minute
        const val MAX_TORNADO_LIFETIME = 6000 // 5 minutes
    }
}
