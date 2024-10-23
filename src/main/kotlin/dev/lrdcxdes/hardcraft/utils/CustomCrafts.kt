package dev.lrdcxdes.hardcraft.utils

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import java.io.File


class CustomCrafts {
    private val configFile: File = Hardcraft.instance.dataFolder.resolve("recipes.yml")
    private val furnaceFile: File = Hardcraft.instance.dataFolder.resolve("furnace.yml")
    private val smokerFile: File = Hardcraft.instance.dataFolder.resolve("smoker.yml")
    private val blastfurnaceFile: File = Hardcraft.instance.dataFolder.resolve("blastfurnace.yml")
    private val campfireFile: File = Hardcraft.instance.dataFolder.resolve("campfire.yml")
    private val stonecutterFile: File = Hardcraft.instance.dataFolder.resolve("stonecutter.yml")
    private val smithingFile: File = Hardcraft.instance.dataFolder.resolve("smithing.yml")

    val customRecipesKeys: MutableList<NamespacedKey> = mutableListOf()

    fun loadAll() {
        this.loadRecipesFromConfig()
        this.loadFurnaceRecipes()
        this.loadSmokingRecipes()
        this.loadCampfireRecipes()
        this.loadBlastFurnaceRecipes()
        this.loadStonecutterRecipes()
        this.loadSmithingRecipes()
        this.idk()
    }

    private fun createResultItem(resultSection: ConfigurationSection): ItemStack {
        val matType = resultSection.getString("type") ?: return ItemStack(Material.AIR)
        val mat = Material.matchMaterial(matType)
        if (mat == null) {
            println("Invalid material: ${resultSection.getString("type")}")
            return ItemStack(Material.AIR)
        }
        val amount = resultSection.getInt("amount", 1)
        val customModelData = resultSection.getInt("customModelData", -1)
        val result = ItemStack(mat, amount).apply {
            itemMeta = itemMeta?.apply {
                if (customModelData != -1) setCustomModelData(customModelData)
                resultSection.getString("displayName")?.let {
                    itemName(Hardcraft.minimessage.deserialize(it))
                }
                if (resultSection.contains("damage")) {
                    (this as? Damageable)?.damage = resultSection.getInt("damage")
                }
                setPotionEffects(resultSection.getConfigurationSection("potionEffects"))
                setColor(resultSection.getString("color"))
            }
        }
        return result
    }

    private fun ItemMeta.setPotionEffects(potionEffects: ConfigurationSection?) {
        potionEffects?.let {
            for (key in it.getKeys(false)) {
                val effect = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(key)) ?: continue
                val duration = it.getInt("$key.duration", 1)
                val amplifier = it.getInt("$key.amplifier", 1)
                val ambient = it.getBoolean("$key.ambient", false)
                val particles = it.getBoolean("$key.particles", true)
                val icon = it.getBoolean("$key.icon", true)
                val effectInstance = PotionEffect(effect, duration, amplifier, ambient, particles, icon)
                (this as? PotionMeta)?.addCustomEffect(effectInstance, true)
            }
        }
    }

    private fun ItemMeta.setColor(color: String?) {
        color?.let {
            (this as? PotionMeta)?.color = Color.fromRGB(it.substring(1).toInt(16))
        }
    }

    private fun getSource(sourceSection: ConfigurationSection): Any {
        return if (sourceSection.contains("type")) {
            val type = sourceSection.getString("type") ?: return Material.AIR
            if (type.startsWith("ANY_")) {
                val matName = type.substring(3)
                RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
            } else {
                Material.matchMaterial(type) ?: Material.AIR
            }
        } else {
            RecipeChoice.MaterialChoice(sourceSection.getStringList("type").mapNotNull { Material.matchMaterial(it) })
        }
    }

    private fun loadRecipesFromConfig() {
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
                val result = createResultItem(resultSection)

                val key = NamespacedKey(Hardcraft.instance, recipeName)
                val recipe = ShapedRecipe(key, result)
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
                customRecipesKeys.add(key)
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

    private fun idk() {
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
            val key = NamespacedKey(Hardcraft.instance, "${color}_bed")
            val recipe = ShapedRecipe(
                key,
                resultBed
            )
            recipe.shape("WW", "PP")
            recipe.setIngredient('W', woolColors[i])
            recipe.setIngredient('P', allWoodAdapter)
            Hardcraft.instance.server.addRecipe(recipe)
            customRecipesKeys.add(key)
        }
    }

    private fun loadFurnaceRecipes() {
        try {
            if (!furnaceFile.exists()) {
                Hardcraft.instance.saveResource("furnace.yml", false)
            }
            val config: YamlConfiguration = YamlConfiguration.loadConfiguration(furnaceFile)

            val addSection = config.getConfigurationSection("add") ?: return
            for (recipeName in addSection.getKeys(false)) {
                println("Loading recipe $recipeName")
                val resultSection = addSection.getConfigurationSection("$recipeName.result")
                if (resultSection == null) {
                    println("Result section is null for $recipeName")
                    continue
                }

                var source: Any

                val d = addSection.getConfigurationSection("$recipeName.source") ?: continue
                var k: String
                val kTemp = d.getStringList("type")
                if (kTemp.size > 1) {
                    val choice = RecipeChoice.MaterialChoice(kTemp.mapNotNull { Material.matchMaterial(it) })
                    source = choice
                } else {
                    k = d.getString("type") ?: continue

                    if (k.startsWith("ANY_")) {
                        val matName = k.substring(3)
                        val choice =
                            RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
                        source = choice
                    } else {
                        val mat = Material.matchMaterial(k) ?: continue
                        val amount = d.getInt("amount", 1)
                        val customModelData = d.getInt("customModelData", -1)
                        val displayName = d.getString("displayName")
                        val color = d.getString("color")
                        if (amount > 1 || customModelData != -1 || displayName != null || color != null) {
                            source = RecipeChoice.ExactChoice(ItemStack(mat, amount).apply {
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
                            source = mat
                        }
                    }
                }

                // Создаем рецепт
                val result = createResultItem(resultSection)

                val cookingTime = resultSection.getInt("cookingTime", 200)
                val experience = resultSection.getDouble("experience", 0.1).toFloat()

                val recipeName1 = "furnace_$recipeName"
                val key = NamespacedKey(Hardcraft.instance, recipeName1)

                val recipe: FurnaceRecipe = when (source) {
                    is Material -> {
                        FurnaceRecipe(
                            key,
                            result,
                            source,
                            experience,
                            cookingTime
                        )
                    }

                    is RecipeChoice -> {
                        FurnaceRecipe(
                            key,
                            result,
                            source,
                            experience,
                            cookingTime
                        )
                    }

                    else -> {
                        throw IllegalArgumentException("Invalid source type")
                    }
                }

                Hardcraft.instance.server.addRecipe(recipe)
                customRecipesKeys.add(key)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error while loading furnace recipes from config")
            e.printStackTrace()
        }
    }

    private fun loadSmokingRecipes() {
        try {
            if (!smokerFile.exists()) {
                Hardcraft.instance.saveResource("smoker.yml", false)
            }
            val config: YamlConfiguration = YamlConfiguration.loadConfiguration(furnaceFile)

            val addSection = config.getConfigurationSection("add") ?: return
            for (recipeName in addSection.getKeys(false)) {
                println("Loading recipe $recipeName")
                val resultSection = addSection.getConfigurationSection("$recipeName.result")
                if (resultSection == null) {
                    println("Result section is null for $recipeName")
                    continue
                }

                var source: Any

                val d = addSection.getConfigurationSection("$recipeName.source") ?: continue
                var k: String
                val kTemp = d.getStringList("type")
                if (kTemp.size > 1) {
                    val choice = RecipeChoice.MaterialChoice(kTemp.mapNotNull { Material.matchMaterial(it) })
                    source = choice
                } else {
                    k = d.getString("type") ?: continue

                    if (k.startsWith("ANY_")) {
                        val matName = k.substring(3)
                        val choice =
                            RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
                        source = choice
                    } else {
                        val mat = Material.matchMaterial(k) ?: continue
                        val amount = d.getInt("amount", 1)
                        val customModelData = d.getInt("customModelData", -1)
                        val displayName = d.getString("displayName")
                        val color = d.getString("color")
                        if (amount > 1 || customModelData != -1 || displayName != null || color != null) {
                            source = RecipeChoice.ExactChoice(ItemStack(mat, amount).apply {
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
                            source = mat
                        }
                    }
                }

                // Создаем рецепт
                val result = createResultItem(resultSection)

                val cookingTime = resultSection.getInt("cookingTime", 200)
                val experience = resultSection.getDouble("experience", 0.1).toFloat()

                val recipeName1 = "smoker_$recipeName"
                val key = NamespacedKey(Hardcraft.instance, recipeName1)

                val recipe: SmokingRecipe = when (source) {
                    is Material -> {
                        SmokingRecipe(
                            key,
                            result,
                            source,
                            experience,
                            cookingTime
                        )
                    }

                    is RecipeChoice -> {
                        SmokingRecipe(
                            key,
                            result,
                            source,
                            experience,
                            cookingTime
                        )
                    }

                    else -> {
                        throw IllegalArgumentException("Invalid source type")
                    }
                }

                Hardcraft.instance.server.addRecipe(recipe)
                customRecipesKeys.add(key)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error while loading furnace recipes from config")
            e.printStackTrace()
        }
    }

    private fun loadCampfireRecipes() {
        try {
            if (!campfireFile.exists()) {
                Hardcraft.instance.saveResource("campfire.yml", false)
            }
            val config: YamlConfiguration = YamlConfiguration.loadConfiguration(campfireFile)

            val addSection = config.getConfigurationSection("add") ?: return
            for (recipeName in addSection.getKeys(false)) {
                println("Loading recipe $recipeName")
                val resultSection = addSection.getConfigurationSection("$recipeName.result")
                if (resultSection == null) {
                    println("Result section is null for $recipeName")
                    continue
                }

                var source: Any

                val d = addSection.getConfigurationSection("$recipeName.source") ?: continue
                var k: String
                val kTemp = d.getStringList("type")
                if (kTemp.size > 1) {
                    val choice = RecipeChoice.MaterialChoice(kTemp.mapNotNull { Material.matchMaterial(it) })
                    source = choice
                } else {
                    k = d.getString("type") ?: continue

                    if (k.startsWith("ANY_")) {
                        val matName = k.substring(3)
                        val choice =
                            RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
                        source = choice
                    } else {
                        val mat = Material.matchMaterial(k) ?: continue
                        val amount = d.getInt("amount", 1)
                        val customModelData = d.getInt("customModelData", -1)
                        val displayName = d.getString("displayName")
                        val color = d.getString("color")
                        if (amount > 1 || customModelData != -1 || displayName != null || color != null) {
                            source = RecipeChoice.ExactChoice(ItemStack(mat, amount).apply {
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
                            source = mat
                        }
                    }
                }

                // Создаем рецепт
                val result = createResultItem(resultSection)

                val cookingTime = resultSection.getInt("cookingTime", 200)
                val experience = resultSection.getDouble("experience", 0.1).toFloat()

                val recipeName1 = "campfire_$recipeName"
                val key = NamespacedKey(Hardcraft.instance, recipeName1)

                val recipe: CampfireRecipe = when (source) {
                    is Material -> {
                        CampfireRecipe(
                            key,
                            result,
                            source,
                            experience,
                            cookingTime
                        )
                    }

                    is RecipeChoice -> {
                        CampfireRecipe(
                            key,
                            result,
                            source,
                            experience,
                            cookingTime
                        )
                    }

                    else -> {
                        throw IllegalArgumentException("Invalid source type")
                    }
                }

                Hardcraft.instance.server.addRecipe(recipe)
                customRecipesKeys.add(key)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error while loading furnace recipes from config")
            e.printStackTrace()
        }
    }

    private fun loadBlastFurnaceRecipes() {
        try {
            if (!blastfurnaceFile.exists()) {
                Hardcraft.instance.saveResource("blastfurnace.yml", false)
            }
            val config: YamlConfiguration = YamlConfiguration.loadConfiguration(blastfurnaceFile)

            val addSection = config.getConfigurationSection("add") ?: return
            for (recipeName in addSection.getKeys(false)) {
                println("Loading recipe $recipeName")
                val resultSection = addSection.getConfigurationSection("$recipeName.result")
                if (resultSection == null) {
                    println("Result section is null for $recipeName")
                    continue
                }

                var source: Any

                val d = addSection.getConfigurationSection("$recipeName.source") ?: continue
                var k: String
                val kTemp = d.getStringList("type")
                if (kTemp.size > 1) {
                    val choice = RecipeChoice.MaterialChoice(kTemp.mapNotNull { Material.matchMaterial(it) })
                    source = choice
                } else {
                    k = d.getString("type") ?: continue

                    if (k.startsWith("ANY_")) {
                        val matName = k.substring(3)
                        val choice =
                            RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
                        source = choice
                    } else {
                        val mat = Material.matchMaterial(k) ?: continue
                        val amount = d.getInt("amount", 1)
                        val customModelData = d.getInt("customModelData", -1)
                        val displayName = d.getString("displayName")
                        val color = d.getString("color")
                        if (amount > 1 || customModelData != -1 || displayName != null || color != null) {
                            source = RecipeChoice.ExactChoice(ItemStack(mat, amount).apply {
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
                            source = mat
                        }
                    }
                }

                // Создаем рецепт
                val result = createResultItem(resultSection)

                val cookingTime = resultSection.getInt("cookingTime", 200)
                val experience = resultSection.getDouble("experience", 0.1).toFloat()

                val recipeName1 = "blasting_$recipeName"
                val key = NamespacedKey(Hardcraft.instance, recipeName1)

                val recipe: BlastingRecipe = when (source) {
                    is Material -> {
                        BlastingRecipe(
                            key,
                            result,
                            source,
                            experience,
                            cookingTime
                        )
                    }

                    is RecipeChoice -> {
                        BlastingRecipe(
                            key,
                            result,
                            source,
                            experience,
                            cookingTime
                        )
                    }

                    else -> {
                        throw IllegalArgumentException("Invalid source type")
                    }
                }

                Hardcraft.instance.server.addRecipe(recipe)
                customRecipesKeys.add(key)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error while loading furnace recipes from config")
            e.printStackTrace()
        }
    }

    private fun loadStonecutterRecipes() {
        try {
            if (!stonecutterFile.exists()) {
                Hardcraft.instance.saveResource("stonecutter.yml", false)
            }
            val config: YamlConfiguration = YamlConfiguration.loadConfiguration(stonecutterFile)

            val addSection = config.getConfigurationSection("add") ?: return
            for (recipeName in addSection.getKeys(false)) {
                println("Loading recipe $recipeName")
                val resultSection = addSection.getConfigurationSection("$recipeName.result")
                if (resultSection == null) {
                    println("Result section is null for $recipeName")
                    continue
                }

                var source: Any

                val d = addSection.getConfigurationSection("$recipeName.source") ?: continue
                var k: String
                val kTemp = d.getStringList("type")
                if (kTemp.size > 1) {
                    val choice = RecipeChoice.MaterialChoice(kTemp.mapNotNull { Material.matchMaterial(it) })
                    source = choice
                } else {
                    k = d.getString("type") ?: continue

                    if (k.startsWith("ANY_")) {
                        val matName = k.substring(3)
                        val choice =
                            RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
                        source = choice
                    } else {
                        val mat = Material.matchMaterial(k) ?: continue
                        val amount = d.getInt("amount", 1)
                        val customModelData = d.getInt("customModelData", -1)
                        val displayName = d.getString("displayName")
                        val color = d.getString("color")
                        if (amount > 1 || customModelData != -1 || displayName != null || color != null) {
                            source = RecipeChoice.ExactChoice(ItemStack(mat, amount).apply {
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
                            source = mat
                        }
                    }
                }

                // Создаем рецепт
                val result = createResultItem(resultSection)

                val recipeName1 = "sc_$recipeName"
                val key = NamespacedKey(Hardcraft.instance, recipeName1)

                val recipe: StonecuttingRecipe = when (source) {
                    is Material -> {
                        StonecuttingRecipe(
                            key,
                            result,
                            source
                        )
                    }

                    is RecipeChoice -> {
                        StonecuttingRecipe(
                            key,
                            result,
                            source
                        )
                    }

                    else -> {
                        throw IllegalArgumentException("Invalid source type")
                    }
                }

                Hardcraft.instance.server.addRecipe(recipe)
                customRecipesKeys.add(key)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error while loading furnace recipes from config")
            e.printStackTrace()
        }
    }

    private fun loadSmithingRecipes() {
        try {
            if (!smithingFile.exists()) {
                Hardcraft.instance.saveResource("smithing.yml", false)
            }
            val config: YamlConfiguration = YamlConfiguration.loadConfiguration(smithingFile)

            val addSection = config.getConfigurationSection("add") ?: return
            for (recipeName in addSection.getKeys(false)) {
                println("Loading recipe $recipeName")
                val resultSection = addSection.getConfigurationSection("$recipeName.result")
                if (resultSection == null) {
                    println("Result section is null for $recipeName")
                    continue
                }

                var base: Any
                var addition: Any

                val d = addSection.getConfigurationSection("$recipeName.base") ?: continue
                var k: String
                val kTemp = d.getStringList("type")
                if (kTemp.size > 1) {
                    val choice = RecipeChoice.MaterialChoice(kTemp.mapNotNull { Material.matchMaterial(it) })
                    base = choice
                } else {
                    k = d.getString("type") ?: continue

                    if (k.startsWith("ANY_")) {
                        val matName = k.substring(3)
                        val choice =
                            RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
                        base = choice
                    } else {
                        val mat = Material.matchMaterial(k) ?: continue
                        val amount = d.getInt("amount", 1)
                        val customModelData = d.getInt("customModelData", -1)
                        val displayName = d.getString("displayName")
                        val color = d.getString("color")
                        if (amount > 1 || customModelData != -1 || displayName != null || color != null) {
                            base = RecipeChoice.ExactChoice(ItemStack(mat, amount).apply {
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
                            base = mat
                        }
                    }
                }

                val d2 = addSection.getConfigurationSection("$recipeName.addition") ?: continue
                var k2: String
                val kTemp2 = d2.getStringList("type")
                if (kTemp2.size > 1) {
                    val choice = RecipeChoice.MaterialChoice(kTemp2.mapNotNull { Material.matchMaterial(it) })
                    addition = choice
                } else {
                    k2 = d2.getString("type") ?: continue

                    if (k2.startsWith("ANY_")) {
                        val matName = k2.substring(3)
                        val choice =
                            RecipeChoice.MaterialChoice(Material.entries.filter { it.name.contains(matName) && it.isItem })
                        addition = choice
                    } else {
                        val mat = Material.matchMaterial(k2) ?: continue
                        val amount = d2.getInt("amount", 1)
                        val customModelData = d2.getInt("customModelData", -1)
                        val displayName = d2.getString("displayName")
                        val color = d2.getString("color")
                        if (amount > 1 || customModelData != -1 || displayName != null || color != null) {
                            addition = RecipeChoice.ExactChoice(ItemStack(mat, amount).apply {
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
                            addition = mat
                        }
                    }
                }

                // Создаем рецепт
                val result = createResultItem(resultSection)

                val recipeName1 = "smithing_$recipeName"

                val baseChoice: RecipeChoice = when (base) {
                    is Material -> {
                        RecipeChoice.MaterialChoice(base)
                    }

                    is RecipeChoice -> {
                        base
                    }

                    else -> {
                        throw IllegalArgumentException("Invalid source type")
                    }
                }

                val base2Choice: RecipeChoice = when (addition) {
                    is Material -> {
                        RecipeChoice.MaterialChoice(addition)
                    }

                    is RecipeChoice -> {
                        addition
                    }

                    else -> {
                        throw IllegalArgumentException("Invalid source type")
                    }
                }

                val key = NamespacedKey(Hardcraft.instance, recipeName1)

                val recipe = SmithingTransformRecipe(
                    key,
                    result,
                    RecipeChoice.empty(),
                    baseChoice,
                    base2Choice
                )

                Hardcraft.instance.server.addRecipe(recipe)
                customRecipesKeys.add(key)
            }
        } catch (e: Exception) {
            Hardcraft.instance.logger.severe("Error while loading furnace recipes from config")
            e.printStackTrace()
        }
    }
}