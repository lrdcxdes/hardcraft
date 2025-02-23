package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
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

class Shaman: Listener {
    // Shaman
    //Уникальные способности
    //Начинает с предметом: палка.
    //Может потратить 5 уровней для призыва цыплёнка.

    private val stick: ItemStack = ItemStack(Material.STICK).apply {
        val meta = itemMeta as Damageable
        itemMeta = meta.apply {
            itemName(Hardcraft.minimessage.deserialize("<lang:bts.shaman_stick>"))

            meta.setCustomModelData(4)

            addUnsafeEnchantment(
                Enchantment.VANISHING_CURSE,
                1
            )
            addEnchant(Enchantment.VANISHING_CURSE, 1, true)

            // hide flags
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
            addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val group = event.player.getGroup()
        if (group == Group.SHAMAN) {
            // check if not have rock
            val haveStick = event.player.inventory.firstOrNull {
                it != null && it.type == Material.STICK && it.itemMeta.customModelData == 4
            } != null

            if (!haveStick) {
                event.player.inventory.addItem(stick)
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
                if (group != Group.SHAMAN) return

                val haveStick = player.inventory.firstOrNull {
                    it != null && it.type == Material.STICK && it.itemMeta.customModelData == 4
                } != null

                if (!haveStick) {
                    player.inventory.addItem(stick)
                }
            }
        }.runTaskLater(Hardcraft.instance, 1)
    }

    enum class Cast {
        BABY_CHICKEN,
        POISON_DART  // 2fp = 5s poison dart
    }

    private val lastCast = mutableMapOf<String, Long>()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val group = event.player.getGroup()
        if (group != Group.SHAMAN) return

        val item = event.item ?: return
        if (item.type == Material.STICK) {
            if (lastCast[event.player.name] != null && System.currentTimeMillis() - lastCast[event.player.name]!! < 5000) {
                event.player.sendMessage(
                    "Cooldown: ${(5000 - (System.currentTimeMillis() - lastCast[event.player.name]!!)) / 1000} seconds"
                )
                return
            }

            // rmb = cast
            // lmb = change cast (cycle)
            if (event.action.isLeftClick) {
                val currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("castShaman"),
                    PersistentDataType.INTEGER
                ) ?: Cast.BABY_CHICKEN.ordinal
                val current = Cast.entries[currentOrdinal]
                val next = when (current) {
                    Cast.BABY_CHICKEN -> Cast.POISON_DART
                    Cast.POISON_DART -> Cast.BABY_CHICKEN
                }

                event.player.persistentDataContainer.set(
                    Hardcraft.instance.key("castShaman"),
                    PersistentDataType.INTEGER,
                    next.ordinal
                )

                event.player.sendMessage(
                    "Current cast: ${next.name}"
                )
            } else {
                val currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("castShaman"),
                    PersistentDataType.INTEGER
                ) ?: Cast.BABY_CHICKEN.ordinal
                val cast = Cast.entries[currentOrdinal]

                if (cast == Cast.BABY_CHICKEN) {
                    // summon chicken
                    if (event.player.totalExperience >= 20) {
                        event.player.totalExperience -= 20
                        event.player.world.spawnEntity(event.player.location, EntityType.CHICKEN).apply {
                            if (this is org.bukkit.entity.Breedable) {
                                age = -24000
                            }
                        }
                    } else {
                        event.player.sendMessage("Not enough levels")
                    }
                } else if (cast == Cast.POISON_DART) {
                    // poison dart
                    if (event.player.foodLevel >= 2) {
                        event.player.foodLevel -= 2
                        val arrow = event.player.launchProjectile(Arrow::class.java)
                        arrow.shooter = event.player
                        arrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY
                        arrow.addCustomEffect(PotionEffect(PotionEffectType.POISON, 3 * 20, 0), true)
                        event.player.world.playSound(
                            event.player.location,
                            "minecraft:entity.horse.breathe",
                            1f,
                            2f
                        )
                    } else {
                        event.player.sendMessage("Not enough levels")
                    }

                    lastCast[event.player.name] = System.currentTimeMillis()
                }
            }
        }
    }
}