package dev.lrdcxdes.hardcraft.customtables

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class FletchingTable : CustomTable(
    Hardcraft.minimessage.deserialize("<lang:block.minecraft.fletching_table>"), listOf(
        CustomTableItem(
            ItemStack(Material.ARROW, 4).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.flint>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.stick>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.feather>")
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(Material.FEATHER, 1),
                CustomTableItemMatch(Material.STICK, 1),
                CustomTableItemMatch(Material.FLINT, 1)
            )
        ),
        CustomTableItem(
            ItemStack(Material.SPECTRAL_ARROW, 1).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x2) <lang:item.minecraft.glowstone_dust>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.arrow>")
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(Material.GLOWSTONE_DUST, 2),
                CustomTableItemMatch(Material.ARROW, 1)
            )
        ),
        CustomTableItem(
            ItemStack(Material.CROSSBOW).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.iron_ingot>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x5) <lang:item.minecraft.stick>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x3) <lang:item.minecraft.string>")
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(Material.STICK, 5),
                CustomTableItemMatch(Material.STRING, 3),
                CustomTableItemMatch(Material.IRON_INGOT, 1)
            )
        ),
        CustomTableItem(
            ItemStack(Material.BOW).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x3) <lang:item.minecraft.stick>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x3) <lang:item.minecraft.string>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.iron_nugget>")
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(Material.STICK, 3),
                CustomTableItemMatch(Material.STRING, 3),
                CustomTableItemMatch(Material.IRON_NUGGET, 1)
            )
        ),
        CustomTableItem(
            ItemStack(Material.TRIPWIRE_HOOK).apply {
                itemMeta = itemMeta.apply {
                    lore(
                        listOf(
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.iron_nugget>"),
                            Hardcraft.minimessage.deserialize("<!italic><gray>(x1) <lang:item.minecraft.stick>"),
                        )
                    )
                }
            },
            listOf(
                CustomTableItemMatch(Material.IRON_NUGGET, 1),
                CustomTableItemMatch(Material.STICK, 1)
            )
        ),
    ), Sound.sound().source(Sound.Source.BLOCK).type(NamespacedKey.minecraft("entity.villager.work_fletcher")).build(),
    Sound.sound().source(Sound.Source.BLOCK).type(NamespacedKey.minecraft("block.crafter.fail")).build()
)