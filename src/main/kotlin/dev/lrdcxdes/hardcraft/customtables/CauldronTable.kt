package dev.lrdcxdes.hardcraft.customtables

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.sound.Sound
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class CauldronTable : CustomTable(
    Hardcraft.minimessage.deserialize("<lang:block.minecraft.cauldron>"),
    listOf(
        CustomTableItem(
            ItemStack(Material.MUSHROOM_STEW, 1).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x3) <lang:item.minecraft.brown_mushroom>"),
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(listOf(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM), 3)
            ),
            forceUpdate = true
        ),
        CustomTableItem(
            ItemStack(Material.POTION, 1).apply {
                val meta = itemMeta as PotionMeta
                itemMeta = meta.apply {
                    itemName(Hardcraft.minimessage.deserialize("<lang:item.minecraft.potion.effect.regeneration>"))

                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.honey_bottle>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.sweet_berries>"),
                        )
                    )

                    addCustomEffect(PotionEffect(PotionEffectType.REGENERATION, 20 * 40, 0), true)

                    color = Color.fromRGB(217, 24, 50)
                }
            },
            listOf(
                CustomTableItemMatch(Material.HONEY_BOTTLE, 1),
                CustomTableItemMatch(Material.SWEET_BERRIES, 1)
            )
        ),
        CustomTableItem(
            ItemStack(Material.POTION, 1).apply {
                val meta = itemMeta as PotionMeta
                itemMeta = meta.apply {
                    itemName(Hardcraft.minimessage.deserialize("<lang:item.minecraft.potion.effect.poison>"))

                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.potion.effect.regeneration>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.pufferfish>"),
                        )
                    )

                    addCustomEffect(PotionEffect(PotionEffectType.POISON, 20 * 40, 0), true)

                    color = Color.fromRGB(42, 156, 25)
                }
            },
            listOf(
                CustomTableItemMatch(Material.PUFFERFISH, 1),
                CustomTableItemMatch(Material.POTION, 1, PotionEffectType.REGENERATION)
            )
        ),
        CustomTableItem(
            ItemStack(Material.SPLASH_POTION, 1).apply {
                val meta = itemMeta as PotionMeta
                itemMeta = meta.apply {
                    itemName(Hardcraft.minimessage.deserialize("<lang:item.minecraft.splash_potion.effect.poison>"))

                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.potion.effect.poison>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.gunpowder>"),
                        )
                    )

                    addCustomEffect(PotionEffect(PotionEffectType.POISON, 20 * 40, 0), true)

                    color = Color.fromRGB(42, 156, 25)
                }
            },
            listOf(
                CustomTableItemMatch(Material.GUNPOWDER, 1),
                CustomTableItemMatch(Material.POTION, 1, PotionEffectType.POISON)
            )
        ),
        CustomTableItem(
            ItemStack(Material.POTION, 1).apply {
                val meta = itemMeta as PotionMeta
                itemMeta = meta.apply {
                    itemName(Hardcraft.minimessage.deserialize("<lang:item.minecraft.splash_potion.effect.fire_resistance>"))

                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.honey_bottle>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.glow_berries>"),
                        )
                    )

                    addCustomEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 40, 0), true)

                    color = Color.fromRGB(235, 177, 52)
                }
            },
            listOf(
                CustomTableItemMatch(Material.HONEY_BOTTLE, 1),
                CustomTableItemMatch(Material.GLOW_BERRIES, 1)
            )
        ),
        CustomTableItem(
            ItemStack(Material.POTION, 1).apply {
                val meta = itemMeta as PotionMeta
                itemMeta = meta.apply {
                    itemName(Hardcraft.minimessage.deserialize("<lang:item.minecraft.splash_potion.effect.healing>"))

                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.potion.effect.regeneration>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.glistering_melon_slice>"),
                        )
                    )

                    addCustomEffect(PotionEffect(PotionEffectType.INSTANT_HEALTH, 0, 0), true)

                    color = Color.fromRGB(145, 7, 23)
                }
            },
            listOf(
                CustomTableItemMatch(Material.GLISTERING_MELON_SLICE, 1),
                CustomTableItemMatch(Material.POTION, 1, PotionEffectType.REGENERATION)
            )
        ),
        CustomTableItem(
            ItemStack(Material.POTION, 1).apply {
                val meta = itemMeta as PotionMeta
                itemMeta = meta.apply {
                    itemName(Hardcraft.minimessage.deserialize("<lang:item.minecraft.splash_potion.effect.weakness>"))

                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.potion.effect.poison>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.fermented_spider_eye>"),
                        )
                    )

                    addCustomEffect(PotionEffect(PotionEffectType.WEAKNESS, 20 * 40, 0), true)

                    color = Color.fromRGB(48, 53, 66)
                }
            },
            listOf(
                CustomTableItemMatch(Material.FERMENTED_SPIDER_EYE, 1),
                CustomTableItemMatch(Material.POTION, 1, PotionEffectType.POISON)
            )
        ),
        CustomTableItem(
            ItemStack(Material.SPLASH_POTION, 1).apply {
                val meta = itemMeta as PotionMeta
                itemMeta = meta.apply {
                    itemName(Hardcraft.minimessage.deserialize("<lang:item.minecraft.splash_potion.effect.weakness>"))

                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.potion.effect.weakness>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.gunpowder>"),
                        )
                    )

                    addCustomEffect(PotionEffect(PotionEffectType.WEAKNESS, 20 * 40, 0), true)

                    color = Color.fromRGB(48, 53, 66)
                }
            },
            listOf(
                CustomTableItemMatch(Material.GUNPOWDER, 1),
                CustomTableItemMatch(Material.POTION, 1, PotionEffectType.WEAKNESS)
            )
        )
    ),
    Sound.sound().source(Sound.Source.BLOCK)
        .type(NamespacedKey.minecraft("block.pointed_dripstone.drip_lava_into_cauldron")).build(),
    Sound.sound().source(Sound.Source.BLOCK).type(NamespacedKey.minecraft("block.crafter.fail")).build()
)