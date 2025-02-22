package dev.lrdcxdes.hardcraft.races

import org.bukkit.attribute.Attribute

/*
Race Definitions

Human
Base Attributes
Health: 20 ()
Scale: 1.0 (default)
Movement speed: 0.10000000149011612 (default)
Armour Handlers
Movement Speed Modifier (when wearing iron/diamond/nether armor): -15% (base 0.08500000126)
Food Handlers
None
Unique Handlers
None

Elf
Base Attributes
Health: 24 (
                   )
Scale: 1.05 (+5%)
Unique Handlers
Constant Potion Effects: (infinite duration – specify effects as needed)
Luck Attribute Modifier: 0.70 (i.e. +70% bonus)
Receive +50% XP Points
Armour Handlers
Movement Speed Modifier (iron/diamond/nether armor): -15% (base 0.08500000126)
Damage Modifier (diamond/nether armor): -30% (base 0.70)
Food Handlers
Allowed: Can consume Fish
Restricted: Cannot consume Meat

Dwarf
Base Attributes
Health: 16 ()
Scale: 0.85 (-15%)
Interaction Range:
Blocks: 3.15  (-30%)
Entities: 2.10 (-30%)
Movement Speed (base): 0.08500000126 (-15%)
Unique Handlers
Armor Toughness Bonus: 1.15
Mining Efficiency Modifier: 1.10
Armour Handlers
Chain Armor Speed Modifier: 1.15
Damage Modifier (diamond/nether armor): 0.80
Food Handlers
Coal Consumption: Grants +2 hunger bars

Cobold
Base Attributes
Health: 14 ()
Scale: 0.65 (-35%)
Unique Handlers
Movement Speed Bonus: 0.11500000171 (+15%)
Mining Efficiency Modifier: 1.4 (+40%)
Armour Handlers
Movement Speed Modifier (iron/diamond/nether armor): 0.06000000089  (-40%)
Damage Modifier (iron/diamond/nether armor): 0.65 (-35%)
General/Other Handlers
Interaction Range:
Blocks: 2.925  (-35%)
Entities: 1.95 (-35%)

Goblin
Base Attributes
Health: 16 ()
Scale: 0.85 (-15%)
Unique Handlers
Regeneration: Self-heal 1 HP every 60 seconds
Group Synergy: +5% movement speed per nearby Goblin (max bonus up to +50%, i.e. multiplier up to 0.15000000223)
Armour Handlers
Movement Speed Modifier (iron/diamond/nether armor): 0.85
Damage Modifier (diamond/nether armor): 0.80
Armor Toughness Modifier (when wearing gold armor): 1.15
Food Handlers
Restricted: Cannot consume Crops

Giant
Base Attributes
Health: 40
Scale: 1.3
Unique Handlers
Damage Bonus: 1.40 multiplier
Jump Boost Modifier: 1.50
Additional Note: Giants receive reduced benefits from food (i.e. food efficiency is lowered)
Armour Handlers
Restricted Armor: Cannot wear gold, iron, diamond, or nether iron armors
Food Handlers
Food Efficiency: Reduced (must eat more food for standard benefit)

Vampire
Base Attributes
Health: 20
Scale: 1.0 (default)
Unique Handlers
Fall Damage Reduction: 70% reduction (multiplier 0.30)
Sunlight Vulnerability: Burns in sunlight (no cap on damage)
Vulnerability to Iron: Iron items inflict +30% extra damage
Armour Handlers
Movement Speed Modifier (iron/diamond/nether armor): 0.85
Damage Modifier (while wearing iron/diamond/nether armor): 0.70
Food Handlers
None

Amphibia
Base Attributes
Health: 20
Scale: 1.0
Unique Handlers
Underwater Adaptation:
Dolphin Grace effect in water
Unlimited underwater breathing
Weather Immunity: No debuffs during rain
Armour Handlers
Movement Speed Modifier (iron/chain/gold/diamond/nether armor): 0.80
General/Other Handlers
Land Movement Modifier: 0.65 (movement speed reduced on land)

Skeleton
Base Attributes
Health: 12
Scale: 1.0
Unique Handlers
Hunger Floor: Hunger cannot drop below 3 bars
Bone Consumption Healing: 1 bone = 1 heart recovered
Sunlight Vulnerability: Burns in sunlight (no cap on damage)
Armour Handlers
Movement Speed Modifier (iron/diamond/nether armor): 0.90
Food Handlers
None

Dragonborn
Base Attributes
Health: 16
Scale: 1.15
Unique Handlers
Temperature Resistance: Immune to high temperatures
Poison Attack Bonus: 1.20 multiplier on poison attacks
Additional Armor: +5 armor bars
Additional Immunities: Poison immunity; night vision
Armour Handlers
Restricted Armor: Cannot wear any armor
Food Handlers
Restricted: Cannot consume Crops

Snow Golem
Base Attributes
Health: 14
Scale: 1.25
Unique Handlers
Temperature Adaptation:
Immune to low-temperature effects
Increased vulnerability to high temperatures
Armour Handlers
Restricted Armor: Can wear only elemental armor (snow/ice)
Food Handlers
None
 */

enum class Race {
    HUMAN, ELF, DWARF, COBOLD, GIANT, VAMPIRE, AMPHIBIAN, SKELETON, GOBLIN, DRAGONBORN, SNOWGOLEM
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
            ),
        ),
        Race.DRAGONBORN to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.15,
                Attribute.MAX_HEALTH to 16.0,
                Attribute.ARMOR to 10.0,
            ),
        ),
        Race.SNOWGOLEM to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.25,
                Attribute.MAX_HEALTH to 24.0,
            ),
        ),
    )

    fun getAttributes(race: Race): RaceAttributes? = races[race]
    fun getDefaultAttributes(): RaceAttributes = defaultAttributes
}
