package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Ageable
import org.bukkit.entity.Breedable
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

class Druid: Listener {
    // Druid
    //Уникальные способности
    //Может призывать мобов с помощью любой книги, затрачивая на это голод.
    //Tadpole: 5 единиц голода
    //Cod Fish: 4 единицы голода
    //Baby Chicken: 6 единиц голода
    //Baby Pig: 8 единиц голода
    //Baby Wolf: 10 единиц голода

    private val book: ItemStack = ItemStack(Material.BOOK).apply {
        val meta = itemMeta as Damageable
        itemMeta = meta.apply {
            itemName(Hardcraft.minimessage.deserialize("<lang:bts.druid_book>"))

            meta.setCustomModelData(4)
            addUnsafeEnchantment(
                Enchantment.VANISHING_CURSE,
                1
            )
            addEnchant(Enchantment.VANISHING_CURSE, 1, true)

            setEnchantmentGlintOverride(false)

            // hide flags
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
            addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val group = event.player.getGroup()
        if (group == Group.DRUID) {
            // check if not have rock
            val haveStick = event.player.inventory.firstOrNull {
                it != null && it.type == Material.BOOK
            } != null

            if (!haveStick) {
                event.player.inventory.addItem(book)
            }
        }

        // give player all recipes
        event.player.discoverRecipes(Hardcraft.instance.cc.customRecipesKeys)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        object : BukkitRunnable() {
            override fun run() {
                val group = event.player.getGroup()
                if (group != Group.DRUID) return

                val haveStick = player.inventory.firstOrNull {
                    it != null && it.type == Material.BOOK
                } != null

                if (!haveStick) {
                    player.inventory.addItem(book)
                }
            }
        }.runTaskLater(Hardcraft.instance, 1)
    }

    enum class Cast {
        TADPOLE,
        COD_FISH,
        BABY_CHICKEN,
        BABY_PIG,
        BABY_WOLF,
    }

    data class CastAttributes(
        val type: EntityType,
        val hunger: Int,
    )

    private val casts = mapOf(
        Cast.TADPOLE to CastAttributes(EntityType.SALMON, 5),
        Cast.COD_FISH to CastAttributes(EntityType.COD, 4),
        Cast.BABY_CHICKEN to CastAttributes(EntityType.CHICKEN, 6),
        Cast.BABY_PIG to CastAttributes(EntityType.PIG, 8),
        Cast.BABY_WOLF to CastAttributes(EntityType.WOLF, 10),
    )

    private val lastCast = mutableMapOf<String, Long>()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val group = event.player.getGroup()
        if (group != Group.DRUID) return

        val item = event.item ?: return
        if (item.type == Material.BOOK) {
            if (lastCast[event.player.name] != null && System.currentTimeMillis() - lastCast[event.player.name]!! < 1000) {
                event.player.sendMessage(
                    "Cooldown: ${(1000 - (System.currentTimeMillis() - lastCast[event.player.name]!!)) / 1000} seconds"
                )
                return
            }

            // rmb = cast
            // lmb = change cast (cycle)
            if (event.action.isLeftClick) {
                val currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("cast"),
                    PersistentDataType.INTEGER
                ) ?: Cast.TADPOLE.ordinal
                val current = Cast.entries[currentOrdinal]
                val next = when (current) {
                    Cast.TADPOLE -> Cast.COD_FISH
                    Cast.COD_FISH -> Cast.BABY_CHICKEN
                    Cast.BABY_CHICKEN -> Cast.BABY_PIG
                    Cast.BABY_PIG -> Cast.BABY_WOLF
                    Cast.BABY_WOLF -> Cast.TADPOLE
                }
                event.player.persistentDataContainer.set(
                    Hardcraft.instance.key("cast"),
                    PersistentDataType.INTEGER,
                    next.ordinal
                )

                event.player.sendMessage(
                    "Current cast: ${next.name}"
                )
            } else {
                val currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("cast"),
                    PersistentDataType.INTEGER
                ) ?: Cast.TADPOLE.ordinal
                val cast = Cast.entries[currentOrdinal]

                val castAttributes = casts[cast] ?: return
                if (event.player.foodLevel >= castAttributes.hunger) {
                    event.player.foodLevel -= castAttributes.hunger
                    event.player.world.spawnEntity(event.player.location, castAttributes.type).apply {
                        if (this is Breedable) {
                            this.age = -24000
                        }
                    }
                    event.player.sendMessage("Casted ${cast.name}")
                } else {
                    event.player.sendMessage("Not enough hunger")
                }

                lastCast[event.player.name] = System.currentTimeMillis()
            }
        }
    }
}