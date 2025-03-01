package dev.lrdcxdes.hardcraft.groups

import dev.lrdcxdes.hardcraft.Hardcraft
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Drummer : Listener {
    private val positiveEffects: List<PotionEffect> = listOf(
        PotionEffect(PotionEffectType.SPEED, 20, 0),
        PotionEffect(PotionEffectType.REGENERATION, 20, 0),
        PotionEffect(PotionEffectType.RESISTANCE, 20, 0),
        PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 0),
    )

    enum class Cast {
        SPEED,
        REGENERATION,
        RESISTANCE,
        FIRE_RESISTANCE,
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val group = event.player.getGroup()
        if (group != Group.DRUMMER) return

        val item = event.item ?: return
        if (item.type == Material.STICK && item.itemMeta.hasCustomModelData() && item.itemMeta.customModelData == 5) {
            // lmb = change cast (cycle)
            if (event.action.isLeftClick) {
                var currentOrdinal = event.player.persistentDataContainer.get(
                    Hardcraft.instance.key("castDrummer"),
                    PersistentDataType.INTEGER
                ) ?: Cast.SPEED.ordinal
                if (currentOrdinal !in 0..Cast.entries.size) {
                    currentOrdinal = 0
                }
                val current = Cast.entries[currentOrdinal]
                val next = when (current) {
                    Cast.SPEED -> Cast.REGENERATION
                    Cast.REGENERATION -> Cast.RESISTANCE
                    Cast.RESISTANCE -> Cast.FIRE_RESISTANCE
                    Cast.FIRE_RESISTANCE -> Cast.SPEED
                }
                event.player.persistentDataContainer.set(
                    Hardcraft.instance.key("castDrummer"),
                    PersistentDataType.INTEGER,
                    next.ordinal
                )

                event.player.sendMessage(
                    Hardcraft.minimessage.deserialize("<lang:btn.current_cast>: <green>${next.name}")
                )
            }
        }
    }

    @EventHandler
    fun onBardUse(event: PlayerItemConsumeEvent) {
        val player = event.player
        if (player.getGroup() != Group.DRUMMER) return
        val item = event.item
        if (item.type != Material.STICK || !item.itemMeta.hasCustomModelData() || item.itemMeta.customModelData != 5) {
            return
        }
        val maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE) ?: 100
        val nowDamage: Int = item.getData(DataComponentTypes.DAMAGE) ?: 0
        if (nowDamage >= maxDamage) {
            item.amount--
        } else {
            item.setData(
                DataComponentTypes.DAMAGE, nowDamage + 1
            )
            event.isCancelled = true
        }

        val effect = when (Cast.entries[player.persistentDataContainer.get(
            Hardcraft.instance.key("castDrummer"),
            PersistentDataType.INTEGER
        ) ?: Cast.SPEED.ordinal]) {
            Cast.SPEED -> positiveEffects[0]
            Cast.REGENERATION -> positiveEffects[1]
            Cast.RESISTANCE -> positiveEffects[2]
            Cast.FIRE_RESISTANCE -> positiveEffects[3]
        }

        for (entity in player.getNearbyEntities(15.0, 15.0, 15.0)) {
            if (entity is LivingEntity) {
                if (entity == player) continue
                if (entity is Monster) continue
                entity.addPotionEffect(effect)
                entity.world.spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    entity.location,
                    1,
                    0.5,
                    0.5,
                    0.5,
                    0.0
                )
            }
        }
    }

    private val startItem: ItemStack = ItemStack(Material.STICK).apply {
        val meta = itemMeta
        itemMeta = meta.apply {
            itemName(Hardcraft.minimessage.deserialize("<lang:bts.drummer_stick>"))

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

        setData(
            DataComponentTypes.CONSUMABLE, Consumable.consumable().consumeSeconds(0.3F).animation(
                ItemUseAnimation.CROSSBOW
            ).sound(
                NamespacedKey.minecraft("block.note_block.snare")
            ).hasConsumeParticles(false).build()
        )
        setData(
            DataComponentTypes.MAX_STACK_SIZE, 1
        )
        setData(
            DataComponentTypes.MAX_DAMAGE, 1000
        )
        setData(
            DataComponentTypes.DAMAGE, 0
        )
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val group = event.player.getGroup()
        if (group == Group.DRUMMER) {
            // check if not have rock
            val haveStick = event.player.inventory.firstOrNull {
                it != null && it.type == Material.STICK && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 5
            } != null

            if (!haveStick) {
                event.player.inventory.addItem(startItem)
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
                if (group != Group.DRUMMER) return

                val haveStick = player.inventory.firstOrNull {
                    it != null && it.type == Material.STICK && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 5
                } != null

                if (!haveStick) {
                    player.inventory.addItem(startItem)
                }
            }
        }.runTaskLater(Hardcraft.instance, 1)
    }
}