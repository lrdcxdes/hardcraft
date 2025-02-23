package dev.lrdcxdes.hardcraft.races

import org.bukkit.attribute.Attribute

enum class Race {
    HUMAN, ELF, DWARF, COBOLD, GIANT, VAMPIRE, AMPHIBIAN, SKELETON, GOBLIN, DRAGONBORN,
    SNOLEM, AGAR, CIBLE
}

data class RaceAttributes(
    val baseAttributes: Map<Attribute, Double> = mapOf(),
)

object RaceManager {
    private val defaultAttributes: RaceAttributes = RaceAttributes(
        baseAttributes = mapOf(
            Attribute.SCALE to 1.0,
            Attribute.MAX_HEALTH to 20.0,
            Attribute.ATTACK_DAMAGE to 1.0,
            Attribute.MOVEMENT_SPEED to 0.10000000149011612,
            Attribute.LUCK to 0.0,
            Attribute.SAFE_FALL_DISTANCE to 3.0,
            Attribute.FALL_DAMAGE_MULTIPLIER to 1.0,
            Attribute.BLOCK_INTERACTION_RANGE to 4.5,
            Attribute.ENTITY_INTERACTION_RANGE to 3.0,
            Attribute.OXYGEN_BONUS to 0.0,
            Attribute.WATER_MOVEMENT_EFFICIENCY to 0.0,
            Attribute.ARMOR to 0.0,
            Attribute.ARMOR_TOUGHNESS to 0.0,
            Attribute.MINING_EFFICIENCY to 0.0,
            Attribute.JUMP_STRENGTH to 0.45
        ),
    )

    private val races: Map<Race, RaceAttributes> = mapOf(
        Race.HUMAN to RaceAttributes(
            baseAttributes = mapOf(),
        ),
        Race.ELF to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.05,
                Attribute.MAX_HEALTH to 24.0,
                Attribute.LUCK to 0.7,
            ),
        ),
        Race.GOBLIN to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.85,
                Attribute.MAX_HEALTH to 16.0,
            ),
        ),
        Race.DWARF to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.85,
                Attribute.MAX_HEALTH to 16.0,
                Attribute.MOVEMENT_SPEED to 0.08500000126,
                Attribute.BLOCK_INTERACTION_RANGE to 3.15,
                Attribute.ENTITY_INTERACTION_RANGE to 2.1,
                Attribute.ARMOR_TOUGHNESS to 1.15,
                Attribute.MINING_EFFICIENCY to 1.1,
            ),
        ),
        Race.COBOLD to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.65,
                Attribute.MAX_HEALTH to 14.0,
                Attribute.MOVEMENT_SPEED to 0.11500000171,
                Attribute.BLOCK_INTERACTION_RANGE to 2.925,
                Attribute.ENTITY_INTERACTION_RANGE to 1.95,
                Attribute.MINING_EFFICIENCY to 1.4,
            ),
        ),
        Race.GIANT to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.3,
                Attribute.MAX_HEALTH to 40.0,
                Attribute.MOVEMENT_SPEED to 0.10000000149011612,
                Attribute.SAFE_FALL_DISTANCE to 5.0,
                Attribute.BLOCK_INTERACTION_RANGE to 5.4,
                Attribute.ENTITY_INTERACTION_RANGE to 3.6,
                Attribute.JUMP_STRENGTH to 0.52,
            ),
        ),
        Race.VAMPIRE to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.FALL_DAMAGE_MULTIPLIER to 0.3,
            ),
        ),
        Race.AMPHIBIAN to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.OXYGEN_BONUS to Double.MAX_VALUE,
                Attribute.WATER_MOVEMENT_EFFICIENCY to 2.0,
            ),
        ),
        Race.SKELETON to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.MAX_HEALTH to 12.0,
                Attribute.OXYGEN_BONUS to 5.0,
            ),
        ),
        Race.DRAGONBORN to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.15,
                Attribute.MAX_HEALTH to 16.0,
                Attribute.ARMOR to 10.0,
            ),
        ),
        Race.SNOLEM to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.25,
                Attribute.MAX_HEALTH to 24.0,
            ),
        ),
        Race.CIBLE to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.9,
                Attribute.MAX_HEALTH to 16.0,
                Attribute.ARMOR to 7.0,
            ),
        ),
        Race.AGAR to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.25,
                Attribute.MAX_HEALTH to 6.0,
                Attribute.JUMP_STRENGTH to 0.45,
                Attribute.FALL_DAMAGE_MULTIPLIER to 1.0,
                Attribute.MOVEMENT_SPEED to 0.10000000149011612,
            ),
        ),
    )

    fun getAttributes(race: Race): RaceAttributes? = races[race]
    fun getDefaultAttributes(): RaceAttributes = defaultAttributes
}
