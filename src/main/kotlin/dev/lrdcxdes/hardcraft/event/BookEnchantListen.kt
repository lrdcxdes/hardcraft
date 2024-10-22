package dev.lrdcxdes.hardcraft.event

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta

data class EnchantData(val enchant: Enchantment, val level: Int, val maxLevel: Int = level)

class BookEnchantListen : Listener {
    private val enchantList = listOf(
        EnchantData(Enchantment.BANE_OF_ARTHROPODS, 1),
        EnchantData(Enchantment.BLAST_PROTECTION, 1),
        EnchantData(Enchantment.DEPTH_STRIDER, 1),
        EnchantData(Enchantment.EFFICIENCY, 1),
        EnchantData(Enchantment.FIRE_PROTECTION, 1),
        EnchantData(Enchantment.KNOCKBACK, 1),
        EnchantData(Enchantment.LURE, 1),
        EnchantData(Enchantment.PIERCING, 1),
        EnchantData(Enchantment.PROJECTILE_PROTECTION, 1),
        EnchantData(Enchantment.PROTECTION, 1, 2),
        EnchantData(Enchantment.SHARPNESS, 1),
        EnchantData(Enchantment.SMITE, 1),
    )

    private val deathList = listOf(
        EnchantData(Enchantment.BINDING_CURSE, 1),
        EnchantData(Enchantment.VANISHING_CURSE, 1),
    )

    @EventHandler
    fun onEnchant(event: PlayerInteractEvent) {
        if (event.item == null) {
            return
        }
        val item = event.item!!
        if (item.type == org.bukkit.Material.BOOK) {
            val clickedBlock = event.clickedBlock
            if (clickedBlock != null) {
                val blockType = clickedBlock.type
                if (blockType == org.bukkit.Material.BOOKSHELF) {
                    if (event.player.level < 2) {
                        event.player.playSound(
                            event.player.location,
                            "minecraft:block.chiseled_bookshelf.pickup",
                            1.0f,
                            1.0f
                        )
                        return
                    }

                    val chance = Hardcraft.instance.random.nextInt(100)

                    item.amount -= 1

                    val bookstack: ItemStack
                    val book: ItemMeta

                    // minecraft:item.book.page_turn
                    event.player.playSound(event.player.location, "minecraft:item.book.page_turn", 1.0f, 1.0f)
                    event.player.level -= 2

                    if (chance < 70) {
                        bookstack = ItemStack(Material.WRITTEN_BOOK)
                        book = bookstack.itemMeta as BookMeta
                        // book.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true)
                        // set author
                        book.author(Hardcraft.minimessage.deserialize(event.player.name))
                        // set title
                        // random title len 4-9
                        val len = Hardcraft.instance.random.nextInt(5) + 4
                        val rndTitle = (1..len).map { ('a'..'z').random() }.joinToString("")
                        book.title(Hardcraft.minimessage.deserialize("<obfuscated>${rndTitle}"))
                        book.itemName(Hardcraft.minimessage.deserialize("<obfuscated>${rndTitle}"))
                    } else if (chance < 90) {
                        bookstack = ItemStack(Material.ENCHANTED_BOOK)
                        book = bookstack.itemMeta as EnchantmentStorageMeta
                        // random enchant
                        val enchant = enchantList[Hardcraft.instance.random.nextInt(enchantList.size)]
                        val level = Hardcraft.instance.random.nextInt(enchant.maxLevel) + 1
                        book.addEnchant(enchant.enchant, level, true)
                    } else {
                        bookstack = ItemStack(Material.ENCHANTED_BOOK)
                        book = bookstack.itemMeta as EnchantmentStorageMeta
                        val enchant = deathList[Hardcraft.instance.random.nextInt(deathList.size)]
                        book.addEnchant(enchant.enchant, enchant.level, true)
                    }

                    bookstack.itemMeta = book
                    event.player.inventory.addItem(bookstack)
                }
            }
        }
    }
}