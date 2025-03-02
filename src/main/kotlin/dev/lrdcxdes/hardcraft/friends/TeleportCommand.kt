package dev.lrdcxdes.hardcraft.friends

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.utils.globalMessageManager
import net.kyori.adventure.title.Title
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.util.*

class TeleportCommand : CommandExecutor, Listener {
    private val tpaRequests: MutableMap<UUID, TpaRequest> = HashMap()

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        val request = tpaRequests[player.uniqueId] ?: return
        if (request.accepted) {
            if (event.to.distanceSquared(event.from) > 0.1) {
                request.cancel()
            }
        }
    }

    inner class TpaRequest(private val sender: Player, val target: Player) {
        var accepted: Boolean = false

        private val task: BukkitTask = object : BukkitRunnable() {
            override fun run() {
                sender.sendMessage(
                    globalMessageManager.getMessage(
                        "btn.tpa.expired",
                        "player" to target.name,
                        color = "<red>"
                    )
                )
                target.sendMessage(
                    globalMessageManager.getMessage(
                        "btn.tpa.expired",
                        "player" to sender.name,
                        color = "<red>"
                    )
                )

                tpaRequests.remove(sender.uniqueId)
            }
        }.runTaskLater(Hardcraft.instance, 1200L)

        private var taskWait: BukkitTask? = null

        private fun waitAndTeleport() {
            sender.showTitle(
                Title.title(
                    Hardcraft.minimessage.deserialize("<yellow><lang:btn.tpa.teleporting>"),
                    Hardcraft.minimessage.deserialize("<white><lang:btn.tpa.teleporting-subtitle>"),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(5), Duration.ZERO)
                )
            )

            taskWait = object : BukkitRunnable() {
                override fun run() {
                    tpaRequests.remove(sender.uniqueId)

                    if (sender.foodLevel < TELEPORT_FOOD_COST) {
                        sender.sendMessage(
                            globalMessageManager.getMessage(
                                "btn.tpa.not-enough-food",
                                "have" to sender.foodLevel.toString(),
                                "need" to TELEPORT_FOOD_COST.toString(),
                                color = "<red>"
                            )
                        )
                        return
                    }

                    sender.foodLevel -= TELEPORT_FOOD_COST
                    sender.teleport(target.location)
                }
            }.runTaskLater(Hardcraft.instance, 5 * 20L)
        }

        fun accept() {
            accepted = true
            task.cancel()

            if (sender.foodLevel < TELEPORT_FOOD_COST) {
                sender.sendMessage(
                    globalMessageManager.getMessage(
                        "btn.tpa.not-enough-food",
                        "have" to sender.foodLevel.toString(),
                        "need" to TELEPORT_FOOD_COST.toString(),
                        color = "<red>"
                    )
                )
                return
            }

            sender.sendMessage(
                globalMessageManager.getMessage(
                    "btn.tpa.accepted",
                    "player" to target.name,
                    color = "<green>"
                )
            )
            target.sendMessage(
                globalMessageManager.getMessage(
                    "btn.tpa.accepted",
                    "player" to sender.name,
                    color = "<green>"
                )
            )

            // Wait 5 sec afk check then teleport
            waitAndTeleport()
        }

        fun cancel() {
            tpaRequests.remove(sender.uniqueId)
            taskWait?.cancel()

            sender.sendMessage(
                globalMessageManager.getMessage(
                    "btn.tpa.cancelled",
                    "player" to target.name,
                    color = "<red>"
                )
            )
        }

        fun deny() {
            task.cancel()
            tpaRequests.remove(sender.uniqueId)

            sender.sendMessage(
                globalMessageManager.getMessage(
                    "btn.tpa.denied",
                    "player" to target.name,
                    color = "<red>"
                )
            )
            target.sendMessage(
                globalMessageManager.getMessage(
                    "btn.tpa.denied",
                    "player" to sender.name,
                    color = "<red>"
                )
            )
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player: Player = sender as? Player ?: return false
        when (command.name) {
            "call" -> {
                if (args.size != 1) {
                    player.sendMessage(
                        Hardcraft.minimessage.deserialize(
                            "<red><lang:btn.tpa.invalid-arguments>"
                        )
                    )
                    return true
                }

                if (player.foodLevel < TELEPORT_FOOD_COST) {
                    player.sendMessage(
                        globalMessageManager.getMessage(
                            "btn.tpa.not-enough-food",
                            "have" to player.foodLevel.toString(),
                            "need" to TELEPORT_FOOD_COST.toString(),
                            color = "<red>"
                        )
                    )
                    return true
                }

                val target = player.server.getPlayer(args[0])
                if (target == null) {
                    player.sendMessage(
                        Hardcraft.minimessage.deserialize(
                            "<red><lang:btn.tpa.invalid-player>"
                        )
                    )
                    return true
                }
                if (target.uniqueId == player.uniqueId) {
                    player.sendMessage(
                        globalMessageManager.getMessage(
                            "btn.tpa.self",
                            color = "<red>"
                        )
                    )
                    return true
                }
                player.sendMessage(
                    globalMessageManager.getMessage(
                        "btn.tpa.sent",
                        "player" to target.name,
                        color = "<green>"
                    ),
                )
                target.sendMessage(
                    globalMessageManager.getMessage(
                        "btn.tpa.received",
                        "player" to player.name,
                        color = "<green>"
                    )
                )
                tpaRequests[player.uniqueId] = TpaRequest(player, target)

                return true
            }

            "tpaccept" -> {
                val request = tpaRequests.filter { it.value.target.uniqueId == player.uniqueId }.values.firstOrNull()
                if (request == null) {
                    player.sendMessage(
                        globalMessageManager.getMessage(
                            "btn.tpa.no-request",
                            color = "<red>"
                        )
                    )
                    return true
                }
                request.accept()
                return true
            }

            "tpdeny" -> {
                val request = tpaRequests.filter { it.value.target.uniqueId == player.uniqueId }.values.firstOrNull()
                if (request == null) {
                    player.sendMessage(
                        globalMessageManager.getMessage(
                            "btn.tpa.no-request",
                            color = "<red>"
                        )
                    )
                    return true
                }
                request.deny()
                return true
            }

            else -> {
                println("Unknown command: ${command.name}")
                return false
            }
        }
    }

    companion object {
        private const val TELEPORT_FOOD_COST = 18
    }
}