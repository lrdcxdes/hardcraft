package dev.lrdcxdes.hardcraft.races

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.potion.PotionEffect

enum class Race {
    HUMAN, ELF, DWARF, GIANT, VAMPIRE, AMPHIBIAN, UNDEAD, GOBLIN, DRAGONBORN
}

data class RaceAttributes(
    val baseAttributes: Map<Attribute, Double> = mapOf(),
    val potionEffects: List<PotionEffect>,  // или свой класс эффектов
    val spawnLocation: Location,
)

object RaceManager {
    private val races: Map<Race, RaceAttributes> = mapOf(
        Race.HUMAN to RaceAttributes(
            baseAttributes = mapOf(),
            potionEffects = listOf(),
            spawnLocation = Location(Bukkit.getWorld("world"), 0.0, 64.0, 0.0),
        ),
    )

    fun getAttributes(race: Race): RaceAttributes? = races[race]
}
