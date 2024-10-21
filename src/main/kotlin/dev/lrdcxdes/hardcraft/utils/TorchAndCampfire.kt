package dev.lrdcxdes.hardcraft.utils

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.block.data.type.Campfire
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class TorchAndCampfire {
    private val TORCH_TIME = (15 * 60..20 * 60)  // 15-20 min
    private val CAMPFIRE_TIME = (15 * 60..20 * 60)  // 15-20 min

    private val task: BukkitTask = object : BukkitRunnable() {
        override fun run() {
            // check torches near all players
            for (player in Hardcraft.instance.server.onlinePlayers) {
                val location = player.location
                val range = (location.world.viewDistance * 16) / 4
                for (x in -range..range) {
                    for (y in -range..range) {
                        for (z in -range..range) {
                            val block = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block
                            if (block.type == org.bukkit.Material.TORCH) {
                                if (!block.hasMetadata("torchTime")) {
                                    block.setMetadata(
                                        "torchTime",
                                        FixedMetadataValue(
                                            Hardcraft.instance,
                                            System.currentTimeMillis() + TORCH_TIME.random() * 1000
                                        )
                                    )
                                    continue
                                }
                                val time =
                                    block.getMetadata("torchTime").firstOrNull()?.asLong()!!
                                if (time <= System.currentTimeMillis()) {
                                    // save facing of current torch and replace default torch by the redstone torch islit false
                                    block.removeMetadata("torchTime", Hardcraft.instance)
                                    block.type = Material.AIR
                                    // /playsound minecraft:block.cherry_wood_trapdoor.close master @s ~ ~ ~ 0.5 2.0
                                    block.location.world.playSound(
                                        block.location,
                                        "minecraft:block.cherry_wood_trapdoor.close",
                                        0.5f,
                                        2.0f
                                    )
                                }
                            } else if (block.type == org.bukkit.Material.CAMPFIRE) {
                                if (!block.hasMetadata("campfireTime")) {
                                    block.setMetadata(
                                        "campfireTime",
                                        FixedMetadataValue(
                                            Hardcraft.instance,
                                            System.currentTimeMillis() + CAMPFIRE_TIME.random() * 1000
                                        )
                                    )
                                    continue
                                }
                                val time =
                                    block.getMetadata("campfireTime").firstOrNull()?.asLong()!!
                                if (time <= System.currentTimeMillis()) {
                                    val data = block.blockData as Campfire
                                    data.isLit = false
                                    block.blockData = data

                                    block.location.world.playSound(
                                        block.location,
                                        "minecraft:block.cherry_wood_trapdoor.close",
                                        0.5f,
                                        2.0f
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }.runTaskTimer(Hardcraft.instance, 0, 20L * 15)
}