package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import io.papermc.paper.datacomponent.DataComponentTypes
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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Wizard : Listener {
    private val book: ItemStack = ItemStack(Material.BOOK).apply {
        val meta = itemMeta as Damageable
        itemMeta = meta.apply {
            itemName(Hardcraft.minimessage.deserialize("<lang:bts.wizard_book>"))

            meta.setCustomModelData(5)
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
        if (group == Group.WIZARD) {
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
                if (group != Group.WIZARD) return

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
        NIGHT_VISION,
        SPEED,
        LEVITATION,
        FIRE_RESISTANCE,
        INVISIBILITY,
    }

    data class CastAttributes(
        val effect: PotionEffect,
        val hunger: Int,
    )

    private val casts = mapOf(
        Cast.NIGHT_VISION to CastAttributes(PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 20, 0), 3),
        Cast.SPEED to CastAttributes(PotionEffect(PotionEffectType.SPEED, 15 * 20, 0), 3),
        Cast.LEVITATION to CastAttributes(PotionEffect(PotionEffectType.LEVITATION, 10 * 20, 0), 4),
        Cast.FIRE_RESISTANCE to CastAttributes(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30 * 20, 0), 4),
        Cast.INVISIBILITY to CastAttributes(PotionEffect(PotionEffectType.INVISIBILITY, 30 * 20, 0), 6),
    )

    private val lastCast: MutableMap<String, Long> = mutableMapOf()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val group = event.player.getGroup()
        if (group != Group.WIZARD) return

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
                ) ?: Cast.NIGHT_VISION.ordinal
                val current = Cast.entries[currentOrdinal]
                val next = when (current) {
                    Cast.NIGHT_VISION -> Cast.SPEED
                    Cast.SPEED -> Cast.LEVITATION
                    Cast.LEVITATION -> Cast.FIRE_RESISTANCE
                    Cast.FIRE_RESISTANCE -> Cast.INVISIBILITY
                    Cast.INVISIBILITY -> Cast.NIGHT_VISION
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
                ) ?: Cast.NIGHT_VISION.ordinal
                val cast = Cast.entries[currentOrdinal]

                val castAttributes = casts[cast] ?: return
                if (event.player.foodLevel >= castAttributes.hunger) {
                    event.player.foodLevel -= castAttributes.hunger
                    event.player.addPotionEffect(castAttributes.effect)
                    event.player.sendMessage(
                        "Casted ${cast.name}"
                    )
                } else {
                    event.player.sendMessage("Not enough hunger")
                }

                lastCast[event.player.name] = System.currentTimeMillis()
            }
        }
    }
}