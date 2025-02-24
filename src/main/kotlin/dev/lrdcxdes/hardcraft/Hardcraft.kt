package dev.lrdcxdes.hardcraft

import dev.lrdcxdes.hardcraft.customtables.CustomTableListen
import dev.lrdcxdes.hardcraft.economy.EconomyCommands
import dev.lrdcxdes.hardcraft.economy.shop.Shop
import dev.lrdcxdes.hardcraft.economy.shop.ShopCommand
import dev.lrdcxdes.hardcraft.event.*
import dev.lrdcxdes.hardcraft.friends.FriendsListener
import dev.lrdcxdes.hardcraft.groups.*
import dev.lrdcxdes.hardcraft.plants.FernManager
import dev.lrdcxdes.hardcraft.plants.GardenManager
import dev.lrdcxdes.hardcraft.plants.PlantsEventListener
import dev.lrdcxdes.hardcraft.races.*
import dev.lrdcxdes.hardcraft.seasons.Seasons
import dev.lrdcxdes.hardcraft.sql.DatabaseManager
import dev.lrdcxdes.hardcraft.utils.Chuma
import dev.lrdcxdes.hardcraft.utils.CustomCrafts
import dev.lrdcxdes.hardcraft.utils.Darkphobia
import dev.lrdcxdes.hardcraft.utils.TorchAndCampfire
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin


class Hardcraft : JavaPlugin() {
    lateinit var shop: Shop
    val random: java.util.Random = java.util.Random()
    private lateinit var entitySpawnListener: EntitySpawnListener
    lateinit var foodListener: FoodListener
    lateinit var seasons: Seasons
    lateinit var cc: CustomCrafts

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

        // RaceHandler
        server.pluginManager.registerEvents(RaceHandler(this, getCommand("race")), this)
        // GroupHandler
        server.pluginManager.registerEvents(GroupHandler(this, getCommand("group")), this)
        // ArmorListener
        server.pluginManager.registerEvents(ArmorListener(), this)
        // Elf
        server.pluginManager.registerEvents(ElfListener(), this)
        // Goblin
        server.pluginManager.registerEvents(GoblinListener(this), this)
        // Amphibian
        server.pluginManager.registerEvents(AmphibiaListener(), this)
        // Cible
        server.pluginManager.registerEvents(Cible(), this)
        // Snolem
        server.pluginManager.registerEvents(Snolem(), this)

        // Groups
        server.pluginManager.registerEvents(Scientists(this), this)
        server.pluginManager.registerEvents(Cleric(this), this)

        server.pluginManager.registerEvents(Warrior(), this)
        server.pluginManager.registerEvents(Paladin(), this)
        server.pluginManager.registerEvents(Ranger(), this)
        server.pluginManager.registerEvents(Rogue(), this)
        server.pluginManager.registerEvents(Shaman(), this)
        server.pluginManager.registerEvents(Druid(), this)
        server.pluginManager.registerEvents(Wizard(), this)
        server.pluginManager.registerEvents(Agar(), this)
        server.pluginManager.registerEvents(Caveman(), this)

        database = DatabaseManager()
        database.connect()
        database.createTables()

        // OnJoinDatabaseListen
        server.pluginManager.registerEvents(OnJoinDatabaseListen(), this)

        // Friends
        server.pluginManager.registerEvents(FriendsListener(), this)

        // Economy
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
