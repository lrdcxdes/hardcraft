package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

data class Food(val material: Material, val spoilTime: Int, val bestTemperature: Int) {
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
        val spoilTime = foodsConfig.getInt("$key.spoilTime")
        val bestTemperature = foodsConfig.getInt("$key.bestTemperature")
        Food(material, spoilTime, bestTemperature)
    }

    private fun checkItem(item: ItemStack, time: Long) {
        val food = foods.find { it.material == item.type } ?: return
        val spoilTime = food.spoilTime

        val meta = item.itemMeta

        val lastCheck = meta.persistentDataContainer.get(Food.SPOIL_TIME, PersistentDataType.LONG)
        val lastSpoilness =
            meta.persistentDataContainer.get(Food.FRESHNESS, PersistentDataType.INTEGER)

        var spoilness: Int = lastSpoilness ?: 100

        if (lastCheck == null || lastSpoilness == null) {
            meta.persistentDataContainer.set(Food.SPOIL_TIME, PersistentDataType.LONG, time)
            meta.persistentDataContainer.set(Food.FRESHNESS, PersistentDataType.INTEGER, 100)
        } else {
            val timeDiff = time - lastCheck
            // TODO: implement temperature effect
            spoilness = lastSpoilness - (timeDiff / spoilTime).toInt()
            if (spoilness <= 0) {
                // Spoiled
                item.amount = 0
                return
            } else {
                // Just update the freshness
                meta.persistentDataContainer.set(
                    Food.FRESHNESS,
                    PersistentDataType.INTEGER,
                    spoilness
                )
            }
        }

        println("Material: ${food.material}")
        println("Spoilness: $spoilness")
        println("Last Spoilness: $lastSpoilness")
        println("Best Temperature: ${food.bestTemperature}")
        println("Time: $time")
        println("Last Check: $lastCheck")

        // update lore with freshness, bestTemperture
        val lore: List<Component> =
            listOf(
                Hardcraft.minimessage.deserialize(
                    "<gradient:#ff0000:#00ff00>${
                        spoilness.toString().padStart(3, ' ')
                    }%</gradient>"
                ),
                Hardcraft.minimessage.deserialize(
                    "<gradient:#ff0000:#00ff00>${food.bestTemperature}</gradient>"
                ),
                Hardcraft.minimessage.deserialize(
                    "<gradient:#ff0000:#00ff00>${lastCheck}</gradient>"
                )
            )

        meta.lore(lore)

        item.itemMeta = meta
    }

//    @EventHandler
//    fun onInventoryOpen(event: InventoryOpenEvent) {
//        val inventory = event.inventory
//        val time = System.currentTimeMillis()
//        // val temperature = world.temperature
//        inventory.contents.forEach { item ->
//            if (item != null && item.type.isEdible) {
//                checkItem(item, time)
//            }
//        }
//    }

    @EventHandler
    fun onItemSpawn(event: ItemSpawnEvent) {
        val item = event.entity.itemStack
        val time = System.currentTimeMillis()
        checkItem(item, time)
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val inventory = event.inventory
        val time = System.currentTimeMillis()
        inventory.contents.forEach { item ->
            if (item != null && item.type.isEdible) {
                checkItem(item, time)
            }
        }
    }
}