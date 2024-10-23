package dev.lrdcxdes.hardcraft

import com.mojang.brigadier.Command
import dev.lrdcxdes.hardcraft.customtables.CustomTableListen
import dev.lrdcxdes.hardcraft.event.*
import dev.lrdcxdes.hardcraft.plants.FernManager
import dev.lrdcxdes.hardcraft.plants.GardenManager
import dev.lrdcxdes.hardcraft.plants.PlantsEventListener
import dev.lrdcxdes.hardcraft.raids.Guardian
import dev.lrdcxdes.hardcraft.raids.PrivateListener
import dev.lrdcxdes.hardcraft.seasons.Seasons
import dev.lrdcxdes.hardcraft.seasons.getTemperatureAsync
import dev.lrdcxdes.hardcraft.utils.Chuma
import dev.lrdcxdes.hardcraft.utils.CustomCrafts
import dev.lrdcxdes.hardcraft.utils.Darkphobia
import dev.lrdcxdes.hardcraft.utils.TorchAndCampfire
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class Hardcraft : JavaPlugin() {
    val random: java.util.Random = java.util.Random()
    private lateinit var entitySpawnListener: EntitySpawnListener
    private lateinit var privateListener: PrivateListener
    lateinit var fernListener: FernListener
    lateinit var foodListener: FoodListener
    lateinit var seasons: Seasons

    override fun onEnable() {
        // Plugin startup logic
        instance = this

        // CustomEntities
        entitySpawnListener = EntitySpawnListener()

        for (entity in server.worlds.flatMap(World::getEntities)) {
            entitySpawnListener.setupGoals(null, entity, entity.location)
        }

        // CustomCrafts
        val cc = CustomCrafts()
        cc.loadAll()

        val manager = this.lifecycleManager
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register(
                Commands.literal("freeze")
                    .executes { context ->
                        val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                        player.freezeTicks = 200

                        Command.SINGLE_SUCCESS
                    }
                    .build()
            )
            commands.register(
                Commands.literal("temperature")
                    .executes { context ->
                        val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                        player.getTemperatureAsync { temp ->
                            player.sendMessage(
                                minimessage.deserialize(
                                    """
                                    <green>Temperature: $temp</green>
                                """.trimIndent()
                                )
                            )
                        }

                        Command.SINGLE_SUCCESS
                    }
                    .build()
            )

            commands.register(
                Commands.literal("cc")
                    .executes { context ->
                        val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                        // reload
                        cc.loadAll()

                        player.sendMessage(
                            minimessage.deserialize(
                                """
                                <green>CustomCrafts reloaded</green>
                            """.trimIndent()
                            )
                        )

                        Command.SINGLE_SUCCESS
                    }
                    .build()
            )
        }

        // gardens
//        Gardens.init()
//        GardenListener().let {
//            server.pluginManager.registerEvents(it, this)
//        }

        // FernListener
//        fernListener = FernListener()
//        fernListener.init()
//        server.pluginManager.registerEvents(fernListener, this)

        // Plants
        val gardenManager = GardenManager()
        gardenManager.init()
        val fernManager = FernManager()
        fernManager.init()
        val plantsEventListener = PlantsEventListener(fernManager, gardenManager)
        server.pluginManager.registerEvents(plantsEventListener, this)

        // food freshness
        foodListener = FoodListener()
        server.pluginManager.registerEvents(foodListener, this)
        // food grow
        server.pluginManager.registerEvents(FoodGrowListener(), this)

        // Seasons
        seasons = Seasons()

        // Player freezes by temperature
        server.pluginManager.registerEvents(PlayerTemperatureListener(), this)

        val temperatureEffectsHandler = TemperatureEffectsHandler()
        temperatureEffectsHandler.startEffectsMonitoring()

        // TorchAndCampfire
        TorchAndCampfire()

        // Darkphobia
        Darkphobia()

        // BookEnchantListen
        server.pluginManager.registerEvents(BookEnchantListen(), this)

        // SlowPlayerListener
        server.pluginManager.registerEvents(SlowPlayerListener(), this)

        // EatFoodListener
        server.pluginManager.registerEvents(EatFoodListener(), this)

        // HungerPlaceListen
        server.pluginManager.registerEvents(HungerPlaceListen(), this)

        // PhysicsPlaceListen
        server.pluginManager.registerEvents(PhysicsPlaceListen(), this)

        // NewFoodListen
        server.pluginManager.registerEvents(NewFoodListen(), this)

        // MilkCowEvent
        server.pluginManager.registerEvents(MilkCowEvent(), this)

        // CraftEvent
        server.pluginManager.registerEvents(CraftEvent(), this)

        // StripListener
        server.pluginManager.registerEvents(StripListener(), this)

        // CustomItemsSpawn
        server.pluginManager.registerEvents(CustomItemsSpawn(), this)

        // Chuma
        Chuma()

        // CustomTableListen
        server.pluginManager.registerEvents(CustomTableListen(), this)

        // JoinListener
        server.pluginManager.registerEvents(JoinListener(), this)

        // Raids
//        raids()

        // remove vanilla wind_charge recipe
        server.removeRecipe(NamespacedKey.minecraft("wind_charge"))

        server.pluginManager.registerEvents(EntityDamageEntityListener(), this)
        server.pluginManager.registerEvents(EntityDropListener(), this)
        server.pluginManager.registerEvents(MedusaListener(), this)
        server.pluginManager.registerEvents(SkeletonListener(), this)
        server.pluginManager.registerEvents(CreeperListener(), this)
        server.pluginManager.registerEvents(entitySpawnListener, this)
    }

    private fun raids() {
        // privates
        privateListener = PrivateListener()
        server.pluginManager.registerEvents(privateListener, this)

        val manager = this.lifecycleManager
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register(
                Commands.literal("guardian")
                    .executes { context ->
                        val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                        val guardian = Guardian(
                            (server.getWorld("world")!! as CraftWorld).handle,
                            20f,
                            player.location,
                            player.uniqueId,
                            1
                        )
                        guardian.spawn()
                        privateListener.addRegion(
                            guardian,
                            player.location.clone().subtract(10.0, 10.0, 10.0),
                            player.location.clone().add(10.0, 10.0, 10.0)
                        )

                        Command.SINGLE_SUCCESS
                    }
                    .build()
            )
        }

        // wind bomb
        val windCompKey = NamespacedKey(this, "wind_component")

        // test
        val windCompRecipe = FurnaceRecipe(
            windCompKey,
            ItemStack(Material.WHITE_DYE, 1).apply {
                itemMeta = itemMeta.apply {
                    displayName(Component.text("Wind Component"))
                    persistentDataContainer.set(windCompKey, PersistentDataType.BYTE, 1)
                }
            },
            RecipeChoice.MaterialChoice(Material.GLOWSTONE_DUST, Material.BLAZE_POWDER),
            0F,
            5 * 60 * 20
        )
        server.addRecipe(windCompRecipe)

        val windComp2Key = NamespacedKey(this, "wind_component2")

        val windComp2Recipe =
            ShapedRecipe(windComp2Key, ItemStack(Material.WHITE_DYE, 1).apply {
                itemMeta = itemMeta.apply {
                    displayName(Component.text("Wind Component 2"))
                    persistentDataContainer.set(windComp2Key, PersistentDataType.BYTE, 2)
                }
            }).apply {
                // make it from blaze_powder and gunpowder
                shape("G", "P", "G")
                setIngredient('G', RecipeChoice.MaterialChoice(Material.GLOWSTONE_DUST, Material.BLAZE_POWDER))
                setIngredient('P', RecipeChoice.MaterialChoice(Material.GUNPOWDER))
            }

        server.addRecipe(windComp2Recipe)

        val windBombKey = NamespacedKey(this, "wind_bomb")
        val windBombRecipe = ShapedRecipe(windBombKey, ItemStack(Material.WIND_CHARGE, 1).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Wind Bomb"))
                persistentDataContainer.set(windBombKey, PersistentDataType.BYTE, 1)
            }
        }
        ).apply {
            shape(" A ", "ABA", " A ")
            setIngredient('A', RecipeChoice.ExactChoice(windComp2Recipe.result))
            setIngredient('B', RecipeChoice.ExactChoice(windCompRecipe.result))
        }
        server.addRecipe(windBombRecipe)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun key(s: String): NamespacedKey {
        return NamespacedKey(this, s)
    }

    companion object {
        val minimessage: MiniMessage = MiniMessage.miniMessage()
        lateinit var instance: Hardcraft

        fun removeRegion(guardian: Guardian) {
            instance.privateListener.removeRegion(guardian)
        }
    }
}
