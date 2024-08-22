package dev.lrdcxdes.hardcraft

import dev.lrdcxdes.hardcraft.event.EntityDamageEntityListener
import dev.lrdcxdes.hardcraft.event.EntitySpawnListener
import dev.lrdcxdes.hardcraft.nms.mobs.CustomChicken
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Hardcraft : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        instance = this

        val loc = server.worlds[0].spawnLocation

        val chicken = CustomChicken(loc)
        chicken.spawn(loc)

        val manager = this.lifecycleManager
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
        }

        server.pluginManager.registerEvents(EntityDamageEntityListener(), this)
        server.pluginManager.registerEvents(EntitySpawnListener(), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        lateinit var instance: Hardcraft
    }
}
