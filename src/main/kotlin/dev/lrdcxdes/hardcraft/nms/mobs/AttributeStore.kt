package dev.lrdcxdes.hardcraft.nms.mobs

import net.minecraft.world.entity.ai.attributes.AttributeMap

class AttributeStore {
    companion object {
        private val attributeStore: MutableMap<String, AttributeMap> = mutableMapOf(
            "pig" to AttributeMap(CustomPig.createAttributes().build()),
            "cow" to AttributeMap(CustomCow.createAttributes().build()),
            "chicken" to AttributeMap(CustomChicken.createAttributes().build()),
            "sheep" to AttributeMap(CustomSheep.createAttributes().build()),
            "silverfish" to AttributeMap(CustomSilverfish.createAttributes().build()),
            "villager" to AttributeMap(CustomVillager.createAttributes().build()),
            "horse" to AttributeMap(CustomHorse.createAttributes().build()),
        )

        fun getAttributes(entityType: String): AttributeMap {
            return attributeStore[entityType]
                ?: throw IllegalArgumentException("No attributes found for entity type $entityType")
        }
    }
}