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
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Bard : Listener {
    private val positiveEffects: List<PotionEffect> = listOf(
        PotionEffect(PotionEffectType.SPEED, 20 * 60, 0),
        PotionEffect(PotionEffectType.REGENERATION, 20 * 15, 0),
        PotionEffect(PotionEffectType.RESISTANCE, 20 * 20, 0),
        PotionEffect(PotionEffectType.LUCK, 20 * 60, 0),
        PotionEffect(PotionEffectType.GLOWING, 20 * 20, 0),
    )

    private val negativeEffects: List<PotionEffect> = listOf(
        PotionEffect(PotionEffectType.SLOWNESS, 20 * 60, 0),
        PotionEffect(PotionEffectType.NAUSEA, 20 * 15, 0),
        PotionEffect(PotionEffectType.WEAKNESS, 20 * 20, 0),
        PotionEffect(PotionEffectType.UNLUCK, 20 * 60, 0),
        PotionEffect(PotionEffectType.DARKNESS, 20 * 15, 0),
    )

    @EventHandler
    fun onBardUse(event: PlayerItemConsumeEvent) {
        val player = event.player
        if (player.getGroup() != Group.BARD) return
        val item = event.item
        if (item.type != Material.STICK || !item.itemMeta.hasCustomModelData() || item.itemMeta.customModelData != 5) {
            return
        }
        val maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE) ?: 100
        val nowDamage: Int = item.getData(DataComponentTypes.DAMAGE) ?: 0
        if (nowDamage >= maxDamage) {
//            item.amount--
        } else {
            item.setData(
                DataComponentTypes.DAMAGE, nowDamage + 1
            )
            event.isCancelled = true
        }

        // He buff or debuff all nearby units (80%-20%) for 25s
        val debuff = Hardcraft.instance.random.nextInt(100) < 20
        val effects = if (debuff) negativeEffects else positiveEffects
        val effect = effects[Hardcraft.instance.random.nextInt(effects.size)]

        for (entity in player.getNearbyEntities(7.0, 7.0, 7.0)) {
            if (entity is LivingEntity) {
                if (entity == player) continue
                if (!debuff && entity is Monster) continue
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
            itemName(Hardcraft.minimessage.deserialize("<lang:bts.bard_stick>"))

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
        setData(
            DataComponentTypes.CONSUMABLE, Consumable.consumable().consumeSeconds(7F).animation(
                ItemUseAnimation.CROSSBOW
            ).sound(
                NamespacedKey.minecraft("block.note_block.banjo")
            ).hasConsumeParticles(false).build()
        )
        setData(
            DataComponentTypes.MAX_STACK_SIZE, 1
        )
        setData(
            DataComponentTypes.MAX_DAMAGE, 50
        )
        setData(
            DataComponentTypes.DAMAGE, 0
        )
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val group = event.player.getGroup()
        if (group == Group.BARD) {
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
                if (group != Group.BARD) return

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