package dev.lrdcxdes.hardcraft.races

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack


class ArmorListener : Listener {
    private fun calculateNewBase(
        base: Double,
        activeArmorCount: Int,
        totalArmor: Int = 4,
        totalPercentage: Double = 15.0,
        action: String = "subtract"
    ): Double {
        if (activeArmorCount > totalArmor) {
            throw IllegalArgumentException("Active armor count cannot exceed total armor count")
        }

        val percentage = (totalPercentage / totalArmor) * activeArmorCount
        return when (action) {
            "subtract" -> base - (base * percentage / 100)
            "add" -> base + (base * percentage / 100)
            else -> throw IllegalArgumentException("Action must be either 'subtract' or 'add'")
        }
    }

    private fun countArmor(
        armor: Array<ItemStack?>,
        validTypes: List<String>
    ): Int = armor.count { it?.type?.name?.let { type -> validTypes.any { type.contains(it) } } == true }

    private fun modifyAttribute(
        player: Player,
        race: Race,
        attribute: Attribute,
        activeArmorCount: Int,
        percentage: Double,
        action: String = "subtract"
    ) {
        val baseValue = RaceManager.getAttributes(race)?.baseAttributes?.get(attribute) ?: RaceManager.getDefaultAttributes().baseAttributes[attribute]!!
        val newValue = calculateNewBase(baseValue, activeArmorCount, totalPercentage = percentage, action = action)
        player.getAttribute(attribute)!!.baseValue = newValue
    }

    // Configuration for each race
    private val raceConfigs = mapOf(
        Race.HUMAN to listOf(
            Triple(Attribute.MOVEMENT_SPEED, listOf("IRON", "DIAMOND", "NETHERITE"), 15.0) to "subtract"
        ),
        Race.ELF to listOf(
            Triple(Attribute.MOVEMENT_SPEED, listOf("IRON", "DIAMOND", "NETHERITE"), 15.0) to "subtract",
            Triple(Attribute.ATTACK_DAMAGE, listOf("DIAMOND", "NETHERITE"), 30.0) to "subtract"
        ),
        Race.DWARF to listOf(
            Triple(Attribute.MOVEMENT_SPEED, listOf("CHAINMAIL"), 15.0) to "add",
            Triple(Attribute.ATTACK_DAMAGE, listOf("DIAMOND", "NETHERITE"), 20.0) to "subtract"
        ),
        Race.KOBOLD to listOf(
            Triple(Attribute.MOVEMENT_SPEED, listOf("IRON", "DIAMOND", "NETHERITE"), 40.0) to "subtract",
            Triple(Attribute.ATTACK_DAMAGE, listOf("IRON", "DIAMOND", "NETHERITE"), 35.0) to "subtract"
        ),
        Race.GOBLIN to listOf(
            Triple(Attribute.MOVEMENT_SPEED, listOf("IRON", "DIAMOND", "NETHERITE"), 15.0) to "subtract",
            Triple(Attribute.ATTACK_DAMAGE, listOf("DIAMOND", "NETHERITE"), 20.0) to "subtract",
            Triple(Attribute.ARMOR_TOUGHNESS, listOf("GOLD"), 15.0) to "add"
        ),
        Race.GIANT to null,
        Race.DRAGONBORN to null,
        Race.SNOLEM to null,
        Race.CIBLE to null,
        Race.AGAR to null,
        Race.VAMPIRE to listOf(
            Triple(Attribute.MOVEMENT_SPEED, listOf("IRON", "DIAMOND", "NETHERITE"), 15.0) to "subtract",
            Triple(Attribute.ATTACK_DAMAGE, listOf("DIAMOND", "NETHERITE"), 30.0) to "subtract"
        ),
        Race.AMPHIBIAN to listOf(
            Triple(
                Attribute.MOVEMENT_SPEED,
                listOf("IRON", "CHAIN", "GOLD", "DIAMOND", "NETHERITE"),
                20.0
            ) to "subtract"
        ),
        Race.SKELETON to listOf(
            Triple(Attribute.MOVEMENT_SPEED, listOf("IRON", "DIAMOND", "NETHERITE"), 10.0) to "subtract"
        )
    )

    private fun removeArmor(p: Player) {
        val armors = p.equipment!!.armorContents.filterNotNull().toTypedArray()

        p.equipment!!.armorContents = arrayOf(null, null, null, null)

        val map = p.inventory.addItem(*armors)

        for ((_, value) in map) {
            p.world.dropItem(p.location, value)
        }
    }

    @EventHandler
    fun onChange(event: EntityEquipmentChangedEvent) {
        val player = event.entity as? Player ?: return
        val race = player.getRace() ?: return
        val armor = player.inventory.armorContents

        if (race in listOf(Race.GIANT, Race.DRAGONBORN, Race.SNOLEM, Race.CIBLE, Race.AGAR)) {
            removeArmor(player)
        } else {
            val config = raceConfigs[race] ?: return
            config.forEach {
                val (attribute, validTypes, percentage) = it.first
                val action = it.second
                val activeArmorCount = countArmor(armor, validTypes)
                modifyAttribute(player, race, attribute, activeArmorCount, percentage, action)
            }
        }
    }
}
