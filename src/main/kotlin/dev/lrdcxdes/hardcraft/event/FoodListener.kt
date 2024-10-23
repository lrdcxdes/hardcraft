package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.seasons.getTemperature
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.abs

data class Food(val material: Material, val spoilTime: Long, val bestTemperature: Int) {
    companion object {
        val SPOIL_TIME = Hardcraft.instance.key("spoilTime")
        val FRESHNESS = Hardcraft.instance.key("freshness")
    }
}

class FoodListener : Listener {
    private val foodsConfigFile = Hardcraft.instance.dataFolder.resolve("foods.yml")
    private val foodsConfig: YamlConfiguration

    init {
        if (!foodsConfigFile.exists()) {
            Hardcraft.instance.saveResource("foods.yml", false)
        }
        foodsConfig = YamlConfiguration.loadConfiguration(foodsConfigFile)
    }

    private val foods: List<Food> = foodsConfig.getKeys(false).map { key ->
        val material = Material.matchMaterial(key) ?: throw IllegalArgumentException("Invalid material: $key")
        val spoilTime = foodsConfig.getLong("$key.spoilTime")
        val bestTemperature = foodsConfig.getInt("$key.bestTemperature")
        Food(material, spoilTime, bestTemperature)
    }

    fun checkItem(item: ItemStack, temperature: Int): Double {
        val food = foods.find { it.material == item.type } ?: return 1.0
        val spoilTime = food.spoilTime
        val bestTemperature = food.bestTemperature

        val meta = item.itemMeta

        var lastCheck = meta.persistentDataContainer.get(Food.SPOIL_TIME, PersistentDataType.LONG)
        var freshness =
            meta.persistentDataContainer.get(Food.FRESHNESS, PersistentDataType.DOUBLE)

        val time = System.currentTimeMillis()

        if (lastCheck == null || freshness == null) {
            meta.persistentDataContainer.set(Food.SPOIL_TIME, PersistentDataType.LONG, time)
            meta.persistentDataContainer.set(Food.FRESHNESS, PersistentDataType.DOUBLE, 1.0)

            freshness = 1.0
        } else {
            val timeDiff = time - lastCheck
            val tempDiff = abs(temperature - bestTemperature)

            // Update freshness based on time and temperature, temperature affects freshness more
            // example
            // 10 minutes passed / 10 minutes spoil time, 20 temperature difference = 2.0
            // 10 minutes passed / 10 minutes spoil time, 10 temperature difference = 1.5
            // 10 minutes passed / 10 minutes spoil time, 0 temperature difference = 1.0

            val x = (((timeDiff / 1000).toDouble() / (spoilTime / 1000).toDouble()) * (1 + tempDiff / 20))

            freshness -= x
            lastCheck = time

            if (freshness <= 0f) {
                // Spoiled
                item.amount = 0
                return 0.0
            } else {
                // Just update the freshness
                meta.persistentDataContainer.set(
                    Food.FRESHNESS,
                    PersistentDataType.DOUBLE,
                    freshness
                )
                meta.persistentDataContainer.set(
                    Food.SPOIL_TIME,
                    PersistentDataType.LONG,
                    lastCheck
                )
            }
        }

        // update lore with freshness, bestTemperture
        val lore: List<Component> =
            listOf(
                Hardcraft.minimessage.deserialize(
                    "<!italic><color:#AAAAAA>Fresh: <color:#FFAA00>${(freshness * 100).toInt()}%"
                ),
//                Hardcraft.minimessage.deserialize(
//                    "<gray>${"Best Temperature: %d".format(bestTemperature)}</gray>"
//                )
            )

        meta.lore(lore)

        item.itemMeta = meta

        return freshness
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inventory = event.inventory
        // check ignoreItems
        if (inventory.type == InventoryType.DISPENSER
            &&
            ignoreItems.any { ignoreItem ->
                inventory.contents.any { item ->
                    item?.type == ignoreItem.type &&
                            item.itemMeta?.lore() == ignoreItem.itemMeta?.lore()
                }
            }
        ) {
            return
        }
        val item = event.currentItem
        if (item != null && item.type.isEdible) {
            val isPlayerInv = event.view.topInventory == event.inventory
            val temp =
                if (isPlayerInv) (event.whoClicked as Player).getTemperature() else event.inventory.location?.block?.let {
                    Hardcraft.instance.seasons.getTemperature(it)
                } ?: 0
            checkItem(
                item,
                temp
            )
        }
    }

    @EventHandler
    fun onItemSpawn(event: ItemSpawnEvent) {
        val item = event.entity.itemStack
        checkItem(item, 0)
    }

    private val ignoreItems: List<ItemStack> = listOf(
        ItemStack(Material.MUSHROOM_STEW, 1).apply {
            itemMeta = itemMeta.apply {
                lore(
                    listOf(
                        Hardcraft.minimessage.deserialize("<!italic><gray>(x3) <lang:item.minecraft.brown_mushroom>"),
                    )
                )
            }
        },
    )

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val inventory = event.inventory
        // check ignoreItems
        if (inventory.type == InventoryType.DISPENSER
            &&
            ignoreItems.any { ignoreItem ->
                inventory.contents.any { item ->
                    item?.type == ignoreItem.type &&
                            item.itemMeta?.lore() == ignoreItem.itemMeta?.lore()
                }
            }
        ) {
            return
        }
        val temp = event.inventory.location?.block?.let {
            Hardcraft.instance.seasons.getTemperature(it)
        } ?: 0
        inventory.contents.forEach { item ->
            if (item != null && item.type.isEdible) {
                checkItem(item, temp)
            }
        }
    }

    private val task = object : BukkitRunnable() {
        override fun run() {
            for (player in Hardcraft.instance.server.onlinePlayers) {
                // ignore players in creative mode or in spectator mode
                if (player.gameMode == org.bukkit.GameMode.CREATIVE || player.gameMode == org.bukkit.GameMode.SPECTATOR) {
                    continue
                }

                val temp = player.getTemperature()
                player.inventory.contents.forEach { item ->
                    if (item != null && item.type.isEdible) {
                        checkItem(item, temp)
                    }
                }
            }
        }
    }.runTaskTimer(Hardcraft.instance, 0, 20L * 60)
}