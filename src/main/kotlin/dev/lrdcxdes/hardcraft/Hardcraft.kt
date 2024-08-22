package dev.lrdcxdes.hardcraft

import dev.lrdcxdes.hardcraft.event.*
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin

class Hardcraft : JavaPlugin() {
    private val entitySpawnListener = EntitySpawnListener()

    override fun onEnable() {
        // Plugin startup logic
        instance = this

        val manager = this.lifecycleManager
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
        }

        for (entity in server.worlds.flatMap(World::getEntities)) {
            entitySpawnListener.setupGoals(null, entity, entity.location)
        }

        // gardens
        Gardens.init()
        GardenListener().let {
            server.pluginManager.registerEvents(it, this)
        }

        server.pluginManager.registerEvents(EntityDamageEntityListener(), this)
        server.pluginManager.registerEvents(MedusaListener(), this)
        server.pluginManager.registerEvents(SkeletonListener(), this)
        server.pluginManager.registerEvents(entitySpawnListener, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        lateinit var instance: Hardcraft
    }
}
