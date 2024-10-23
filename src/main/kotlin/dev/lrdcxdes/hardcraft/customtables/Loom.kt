package dev.lrdcxdes.hardcraft.customtables

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class Loom : CustomTable(
    Hardcraft.minimessage.deserialize("<lang:block.minecraft.loom>"), listOf(
        CustomTableItem(
            ItemStack(Material.COBWEB, 1).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x16) <lang:item.minecraft.string>"),
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(Material.STRING, 16)
            )
        ),
        CustomTableItem(
            ItemStack(Material.LEATHER, 1).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x4) <lang:item.minecraft.rabbit_hide>"),
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(Material.RABBIT_HIDE, 4)
            )
        ),
        CustomTableItem(
            ItemStack(Material.PAINTING).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:block.minecraft.white_wool>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x8) <lang:item.minecraft.stick>"),
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(Material.STICK, 8),
                CustomTableItemMatch(
                    listOf(
                        Material.WHITE_WOOL,
                        Material.RED_WOOL,
                        Material.GREEN_WOOL,
                        Material.BROWN_WOOL,
                        Material.BLUE_WOOL,
                        Material.PURPLE_WOOL,
                        Material.CYAN_WOOL,
                        Material.LIGHT_GRAY_WOOL,
                        Material.GRAY_WOOL,
                        Material.PINK_WOOL,
                        Material.LIME_WOOL,
                        Material.YELLOW_WOOL,
                        Material.LIGHT_BLUE_WOOL,
                        Material.MAGENTA_WOOL,
                        Material.ORANGE_WOOL,
                        Material.BLACK_WOOL
                    ), 1
                )
            )
        ),
        CustomTableItem(
            ItemStack(Material.STRING, 2).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:bts.zaza>"),
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(
                    Material.GREEN_DYE,
                    1,
                    Hardcraft.minimessage.deserialize("<color:#00AA00><lang:bts.zaza>"),
                    3
                )
            )
        ),
        CustomTableItem(
            ItemStack(Material.LEATHER_HELMET).apply {
                val meta = itemMeta as Damageable
                itemMeta = meta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x4) <lang:item.minecraft.leather>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x2) <lang:item.minecraft.string>"),
                        )
                    )
                    damage = 40
                }
            },
            listOf(
                CustomTableItemMatch(Material.LEATHER, 4),
                CustomTableItemMatch(Material.STRING, 2)
            )
        ),
        CustomTableItem(
            ItemStack(Material.LEATHER_BOOTS).apply {
                val meta = itemMeta as Damageable
                itemMeta = meta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x4) <lang:item.minecraft.leather>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x2) <lang:item.minecraft.string>"),
                        )
                    )
                    damage = 50
                }
            },
            listOf(
                CustomTableItemMatch(Material.LEATHER, 4),
                CustomTableItemMatch(Material.STRING, 2)
            )
        ),
        CustomTableItem(
            ItemStack(Material.LEATHER_CHESTPLATE).apply {
                val meta = itemMeta as Damageable
                itemMeta = meta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x6) <lang:item.minecraft.leather>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x2) <lang:item.minecraft.string>"),
                        )
                    )
                    damage = 65
                }
            },
            listOf(
                CustomTableItemMatch(Material.LEATHER, 6),
                CustomTableItemMatch(Material.STRING, 2)
            )
        ),
        CustomTableItem(
            ItemStack(Material.LEATHER_LEGGINGS).apply {
                val meta = itemMeta as Damageable
                itemMeta = meta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x6) <lang:item.minecraft.leather>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x2) <lang:item.minecraft.string>"),
                        )
                    )
                    damage = 60
                }
            },
            listOf(
                CustomTableItemMatch(Material.LEATHER, 6),
                CustomTableItemMatch(Material.STRING, 2)
            )
        ),
    ), Sound.sound().source(Sound.Source.BLOCK).type(NamespacedKey.minecraft("ui.loom.take_result")).build(),
    Sound.sound().source(Sound.Source.BLOCK).type(NamespacedKey.minecraft("block.crafter.fail")).build()
)