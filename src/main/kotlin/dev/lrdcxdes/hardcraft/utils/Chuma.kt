package dev.lrdcxdes.hardcraft.utils

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.scheduler.BukkitRunnable

class Chuma {
    private val chumaMobs = listOf(
        EntityType.COW,
        EntityType.PIG,
        EntityType.SHEEP,
        EntityType.CHICKEN,
        EntityType.RABBIT,
        EntityType.HORSE,
        EntityType.DONKEY,
        EntityType.MULE,
        EntityType.LLAMA,
        EntityType.PANDA,
    )

    init {
        object : BukkitRunnable() {
            override fun run() {
                val overworlds = Hardcraft.instance.server.worlds.filter { it.environment == World.Environment.NORMAL }
                for (world in overworlds) {
                    for (entity in world.entities) {
                        if (entity.isDead) continue
                        if (entity.type in chumaMobs && Hardcraft.instance.random.nextInt(100) < 5) {
                            (entity as Mob).damage(1.0)
                        }
                    }
                }
            }
        }.runTaskTimer(Hardcraft.instance, 20L * 60 * 30, 20L * 60 * 60)
    }
}