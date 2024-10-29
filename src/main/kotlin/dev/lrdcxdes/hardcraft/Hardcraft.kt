package dev.lrdcxdes.hardcraft

import dev.lrdcxdes.hardcraft.customtables.CustomTableListen
import dev.lrdcxdes.hardcraft.economy.EconomyCommands
import dev.lrdcxdes.hardcraft.economy.VaultImpl
import dev.lrdcxdes.hardcraft.economy.shop.Shop
import dev.lrdcxdes.hardcraft.economy.shop.ShopCommand
import dev.lrdcxdes.hardcraft.event.*
import dev.lrdcxdes.hardcraft.friends.FriendsCommand
import dev.lrdcxdes.hardcraft.friends.FriendsListener
import dev.lrdcxdes.hardcraft.friends.FriendsManager
import dev.lrdcxdes.hardcraft.plants.FernManager
import dev.lrdcxdes.hardcraft.plants.GardenManager
import dev.lrdcxdes.hardcraft.plants.PlantsEventListener
import dev.lrdcxdes.hardcraft.seasons.Seasons
import dev.lrdcxdes.hardcraft.sql.DatabaseManager
import dev.lrdcxdes.hardcraft.utils.Chuma
import dev.lrdcxdes.hardcraft.utils.CustomCrafts
import dev.lrdcxdes.hardcraft.utils.Darkphobia
import dev.lrdcxdes.hardcraft.utils.TorchAndCampfire
import net.kyori.adventure.text.minimessage.MiniMessage
import net.milkbowl.vault.economy.Economy
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin


class Hardcraft : JavaPlugin() {
    lateinit var shop: Shop
    val random: java.util.Random = java.util.Random()
    private lateinit var entitySpawnListener: EntitySpawnListener
    lateinit var foodListener: FoodListener
    lateinit var seasons: Seasons
    lateinit var cc: CustomCrafts
    private lateinit var friendsManager: FriendsManager
    private var econ: Economy? = null

    override fun onEnable() {
        // Plugin startup logic
        instance = this

        // CustomEntities
        entitySpawnListener = EntitySpawnListener()

        for (entity in server.worlds.flatMap(World::getEntities)) {
            entitySpawnListener.setupGoals(null, entity, entity.location)
        }

        // CustomCrafts
        cc = CustomCrafts()
        cc.loadAll()

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

        // TNTThrowListen
        server.pluginManager.registerEvents(TNTThrowListen(), this)

        // CraftEvent
        server.pluginManager.registerEvents(CraftEvent(), this)

        // StripListener
        server.pluginManager.registerEvents(StripListener(), this)

        // CustomItemsSpawn
        server.pluginManager.registerEvents(CustomItemsSpawn(), this)

        // ThermometerClickListen
        server.pluginManager.registerEvents(ThermometerClickListen(), this)

        // Chuma
        Chuma()

        // SlimeDeathListen
        server.pluginManager.registerEvents(SlimeListen(), this)

        // CustomTableListen
        server.pluginManager.registerEvents(CustomTableListen(), this)

        // JoinListener
        server.pluginManager.registerEvents(JoinListener(), this)

        // FlintAndSteelListen
        server.pluginManager.registerEvents(FlintAndSteelListen(), this)

        // SieveBowlListen
        server.pluginManager.registerEvents(SieveBowlListen(), this)

        // SawListen
        server.pluginManager.registerEvents(SawListen(), this)

        // Raids
//        raids()

        database = DatabaseManager()
        database.connect()
        database.createTables()

        // OnJoinDatabaseListen
        server.pluginManager.registerEvents(OnJoinDatabaseListen(), this)

        // Friends
        friendsManager = FriendsManager()
        getCommand("friends")?.setExecutor(FriendsCommand(friendsManager))
        server.pluginManager.registerEvents(FriendsListener(friendsManager), this)

        // Economy
        vaultImpl = VaultImpl()

        val economyCommands = EconomyCommands()
        getCommand("economy")?.let {
            it.setExecutor(economyCommands)
            it.tabCompleter = economyCommands
        }
        getCommand("pay")?.let {
            it.setExecutor(economyCommands)
            it.tabCompleter = economyCommands
        }
        getCommand("balance")?.setExecutor(economyCommands)

        // Shop
        shop = Shop()
        getCommand("shop")?.setExecutor(ShopCommand(shop))
        server.pluginManager.registerEvents(shop, this)

        if (server.pluginManager.getPlugin("Vault") == null) {
            logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", name))
            server.pluginManager.disablePlugin(this)
            return
        }

        if (!setupEconomy()) {
            logger.severe("No economy plugin")
        }

        // DamageWithoutInstrumentListen
        server.pluginManager.registerEvents(DamageWithoutInstrumentListen(), this)

        // PoopThrowEvent
        server.pluginManager.registerEvents(PoopThrowEvent(), this)

        // Other listeners
        server.pluginManager.registerEvents(EntityDamageEntityListener(), this)
        server.pluginManager.registerEvents(EntityDropListener(), this)
        server.pluginManager.registerEvents(MedusaListener(), this)
        server.pluginManager.registerEvents(SkeletonListener(), this)
        server.pluginManager.registerEvents(CreeperListener(), this)
        server.pluginManager.registerEvents(entitySpawnListener, this)
    }

    private lateinit var vaultImpl: VaultImpl

    private fun setupEconomy(): Boolean {
        server.servicesManager.register(Economy::class.java, vaultImpl, this, ServicePriority.Highest)
        econ = vaultImpl
        return true
    }

    override fun onDisable() {
        // Plugin shutdown logic
        shop.onDisable()
        database.disconnect()
    }

    fun key(s: String): NamespacedKey {
        return NamespacedKey(this, s)
    }

    companion object {
        val minimessage: MiniMessage = MiniMessage.miniMessage()
        lateinit var instance: Hardcraft
        lateinit var database: DatabaseManager
    }
}
