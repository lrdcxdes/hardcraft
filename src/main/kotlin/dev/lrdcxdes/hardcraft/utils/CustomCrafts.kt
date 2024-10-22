package dev.lrdcxdes.hardcraft.utils

import com.google.common.base.Preconditions
import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.*


class CustomCrafts {
    private val configFile: File = Hardcraft.instance.dataFolder.resolve("recipes.yml")

    fun loadRecipesFromConfig() {
        // reset crafts
        Hardcraft.instance.server.resetRecipes()

        try {
            if (!configFile.exists()) {
                Hardcraft.instance.saveResource("recipes.yml", false)
            }
            val config: YamlConfiguration = YamlConfiguration.loadConfiguration(configFile)

            // Удаляем ванильные рецепты
            val recipesToRemove: List<String> = config.getStringList("remove")
            for (recipeName in recipesToRemove) {
                if (recipeName == "all_planks") {
                    val keys = arrayOf(
                        "oak_planks",
                        "spruce_planks",
                        "birch_planks",
                        "jungle_planks",
                        "acacia_planks",
                        "dark_oak_planks",
                        "crimson_planks",
                        "warped_planks",
                        "mangrove_planks",
                        "cherry_planks"
                    )
                    for (key in keys) {
                        Hardcraft.instance.server.removeRecipe(NamespacedKey("minecraft", key))
                    }
                    continue
                }
                val key = NamespacedKey("minecraft", recipeName)
                Hardcraft.instance.server.removeRecipe(key)
            }

            // Добавляем кастомные рецепты
            val addSection: ConfigurationSection = config.getConfigurationSection("add") ?: return
            for (recipeName in addSection.getKeys(false)) {
                println("Loading recipe $recipeName")
                val resultSection = addSection.getConfigurationSection("$recipeName.result")
                if (resultSection == null) {
                    println("Result section is null for $recipeName")
                    continue
                }
                val shape = addSection.getStringList("$recipeName.shape")
                val ingredients: MutableMap<Char, Any> = HashMap()

                val ingredientsSection = addSection.getConfigurationSection("$recipeName.ingredients") ?: continue
                for (key in ingredientsSection.getKeys(false)) {
                    val d = ingredientsSection.getConfigurationSection(key) ?: continue
                    var k: String
                    val kTemp = d.getStringList("type")
                    if (kTemp.size > 1) {
                        val choice = RecipeChoice.MaterialChoice(kTemp.mapNotNull { Material.matchMaterial(it) })
                        ingredients[key[0]] = choice
                        continue
                    } else {
                        k = d.getString("type") ?: continue
                    }
                    if (k.startsWith("ANY_")) {
                        val matName = k.substring(3)
                        val choice =
                            RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
                        ingredients[key[0]] = choice
                        continue
                    }
                    val mat = Material.matchMaterial(k) ?: continue
                    val amount = d.getInt("amount", 1)
                    val customModelData = d.getInt("customModelData", -1)
                    val displayName = d.getString("displayName")
                    val color = d.getString("color")
                    if (amount > 1 || customModelData != -1 || displayName != null || color != null) {
                        ingredients[key[0]] = RecipeChoice.ExactChoice(ItemStack(mat, amount).apply {
                            val meta = itemMeta
                            if (customModelData != -1) {
                                meta.setCustomModelData(customModelData)
                            }
                            if (displayName != null) {
                                meta.itemName(Hardcraft.minimessage.deserialize(displayName))
                            }
                            if (color != null) {
                                (meta as PotionMeta)
                                meta.color = Color.fromRGB(color.substring(1).toInt(16))
                            }
                            itemMeta = meta
                        })
                    } else {
                        ingredients[key[0]] = mat
                    }
                }

                // Создаем рецепт
                val mat = Material.matchMaterial(resultSection.getString("type") ?: continue) ?: continue
                val amount = resultSection.getInt("amount", 1)
                val customModelData = resultSection.getInt("customModelData", -1)
                val result = ItemStack(mat, amount)
                if (customModelData != -1) {
                    val meta = result.itemMeta
                    meta.setCustomModelData(customModelData)
                    result.itemMeta = meta
                }
                val displayName = resultSection.getString("displayName")
                if (displayName != null) {
                    val meta = result.itemMeta
                    meta.itemName(Hardcraft.minimessage.deserialize(displayName))
                    result.itemMeta = meta
                }

                if (resultSection.getInt("damage", -1) != -1) {
                    val meta = result.itemMeta as Damageable
                    meta.damage = resultSection.getInt("damage")
                    result.itemMeta = meta
                }

                // check if potion effects
                val potionEffects = resultSection.getConfigurationSection("potionEffects")
                if (potionEffects != null) {
                    for (key in potionEffects.getKeys(false)) {
                        PotionEffectType.BAD_OMEN
                        val effect = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(key))
                        if (effect == null) {
                            println("Invalid potion effect: $key")
                            continue
                        }
                        val duration = potionEffects.getInt("$key.duration", 1)
                        val amplifier = potionEffects.getInt("$key.amplifier", 1)
                        val ambient = potionEffects.getBoolean("$key.ambient", false)
                        val particles = potionEffects.getBoolean("$key.particles", true)
                        val icon = potionEffects.getBoolean("$key.icon", true)
                        val effectInstance = PotionEffect(effect, duration, amplifier, ambient, particles, icon)

                        val meta = result.itemMeta as PotionMeta
                        meta.addCustomEffect(effectInstance, true)

                        result.itemMeta = meta
                    }
                }

                val color = resultSection.getString("color")
                if (color != null) {
                    val meta = result.itemMeta as PotionMeta
                    meta.color = Color.fromRGB(color.substring(1).toInt(16))
                    result.itemMeta = meta
                }

                println("Adding recipe $recipeName")
                println("Result: $result")
                println("Ingredients: $ingredients")
                println("Shape: $shape")

                val recipe = ShapedRecipe(NamespacedKey(Hardcraft.instance, recipeName), result)
                recipe.shape(*shape.toTypedArray())

                // Добавляем ингредиенты
                for ((key, value) in ingredients) {
                    when (value) {
                        is Material -> {
                            recipe.setIngredient(key, value)
                        }

                        is ItemStack -> {
                            recipe.setIngredient(key, value.type)
                        }

                        is RecipeChoice.MaterialChoice -> {
                            recipe.setIngredient(key, value)
                        }

                        is RecipeChoice.ExactChoice -> {
                            recipe.setIngredient(key, value)
                        }
                    }
                }

                // Добавляем рецепт на сервер
                Hardcraft.instance.server.addRecipe(recipe)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error while loading recipes from config")
            e.printStackTrace()
        }
    }

    private val allWoodAdapter = RecipeChoice.MaterialChoice(
        Material.OAK_PLANKS,
        Material.SPRUCE_PLANKS,
        Material.BIRCH_PLANKS,
        Material.JUNGLE_PLANKS,
        Material.ACACIA_PLANKS,
        Material.DARK_OAK_PLANKS,
        Material.CRIMSON_PLANKS,
        Material.WARPED_PLANKS,
        Material.MANGROVE_PLANKS,
        Material.CHERRY_PLANKS
    )

    fun idk() {
        // remove all beds vanilla recipes
        val bedKeys = arrayOf(
            "white_bed",
            "orange_bed",
            "magenta_bed",
            "light_blue_bed",
            "yellow_bed",
            "lime_bed",
            "pink_bed",
            "gray_bed",
            "light_gray_bed",
            "cyan_bed",
            "purple_bed",
            "blue_bed",
            "brown_bed",
            "green_bed",
            "red_bed",
            "black_bed"
        )
        for (key in bedKeys) {
            Hardcraft.instance.server.removeRecipe(NamespacedKey("minecraft", key))
        }

        // Bed's recipes based on wool color
        val woolColors = arrayOf(
            Material.WHITE_WOOL,
            Material.ORANGE_WOOL,
            Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.PINK_WOOL,
            Material.GRAY_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.CYAN_WOOL,
            Material.PURPLE_WOOL,
            Material.BLUE_WOOL,
            Material.BROWN_WOOL,
            Material.GREEN_WOOL,
            Material.RED_WOOL,
            Material.BLACK_WOOL
        )

        for (i in woolColors.indices) {
            val color = woolColors[i].name.split("_WOOL")[0]
            val resultBed = ItemStack(Material.matchMaterial("${color}_BED")!!, 1)
            val recipe = ShapedRecipe(
                NamespacedKey(Hardcraft.instance, "${color}_bed"),
                resultBed
            )
            recipe.shape("WW", "PP")
            recipe.setIngredient('W', woolColors[i])
            recipe.setIngredient('P', allWoodAdapter)
            Hardcraft.instance.server.addRecipe(recipe)
        }
    }

}