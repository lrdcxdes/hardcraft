package dev.lrdcxdes.hardcraft.races

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.nms.mobs.CustomSlime
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

class Agar : Listener {
    private val maxStacks = 6

    init {
        // Schedule task to change a random inventory item into a slimeball every 5 minutes.
        Bukkit.getScheduler().runTaskTimer(Hardcraft.instance, Runnable {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (player.getRace() == Race.AGAR) {
                    convertRandomItemToSlimeball(player)
                }
            }
        }, 5 * 60 * 20, 5 * 60 * 20)
    }

    @EventHandler
    fun onKill(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (killer.getRace() != Race.AGAR) return

        // Must be at full HP to gain a stack.
        val maxHealth = killer.getAttribute(Attribute.MAX_HEALTH)?.value ?: return
        if (killer.health >= maxHealth) {
            var currentStacks = getKillerStacks(killer)
            if (currentStacks < maxStacks) {
                setKillerStacks(killer, currentStacks + 1)
                currentStacks += 1
            }
            // Adjust jump height and other attributes based on stacks.
            adjustAttributes(killer, currentStacks)
            // Play Rise UP sound
            killer.playSound(killer.location, "minecraft:block.chest.locked", 1.0f, 2.0f)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (player.getRace() != Race.AGAR) return
        if (event.isCancelled) return

        val currentStacks = getKillerStacks(player)

        // Lose one stack if the player loses at least 3 hearts (6 points) in one hit.
        val diff = player.getAttribute(Attribute.MAX_HEALTH)?.value!! - (player.health - event.finalDamage)
        println("hp: ${player.health}, damage: ${event.finalDamage}, diff: $diff")
        if (diff >= 6.0) {
            if (currentStacks > 1) {
                event.isCancelled = true

                setKillerStacks(player, currentStacks - 1)
                adjustAttributes(player, currentStacks - 1, heal = false)
                // Summon a baby slime at the player position.
                player.world.spawn(player.location, Slime::class.java) { slime ->
                    slime.size = 1
                    CustomSlime.setGoals(slime)
                }

                // playSound for losing a stack
                player.world.playSound(player.location, "minecraft:block.chest.locked", 1.0f, 0.5f)
            }
        }
    }

    private val lastUnstack = mutableMapOf<String, Long>()

    @EventHandler
    fun onUse(event: PlayerInteractEvent) {
        // use 15 exp points to shoot 1 fireball with cd 5 sec
        val player = event.player
        val race = player.getRace() ?: return
        if (race != Race.AGAR) return
        if (player.isSneaking && event.action.isRightClick && event.item?.type?.isBlock != true) {
            if (lastUnstack[player.name] != null && System.currentTimeMillis() - lastUnstack[player.name]!! < 1000) {
                return
            }

            val currentStacks = getKillerStacks(player)
            if (currentStacks > 1) {
                setKillerStacks(player, currentStacks - 1)
                adjustAttributes(player, currentStacks - 1)
                lastUnstack[player.name] = System.currentTimeMillis()

                // Summon a baby slime at the player position.
                player.world.spawn(player.location, Slime::class.java) { slime ->
                    slime.size = 1
                    CustomSlime.setGoals(slime)
                }
            }
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player
        if (player.getRace() != Race.AGAR) return

        val stacks = getKillerStacks(player)
        if (stacks > 1) {
            event.isCancelled = true
            return
        }

        // Lose all stacks on death.
        setKillerStacks(player, 1)
        adjustAttributes(player, 1, heal = false)
    }

    private fun convertRandomItemToSlimeball(player: Player) {
        val inventory = player.inventory
        val nonEmptySlots = (0 until inventory.size).filter { inventory.getItem(it) != null }
        if (nonEmptySlots.isEmpty()) return
        val slot = nonEmptySlots.random()
        inventory.setItem(slot, ItemStack(Material.SLIME_BALL))
    }

    companion object {
        fun getKillerStacks(killer: Player): Int {
            val container = killer.persistentDataContainer
            val agarStacks = container.get(NamespacedKey(Hardcraft.instance, "agarStacks"), PersistentDataType.INTEGER)
            return agarStacks?.toInt() ?: 1
        }

        fun setKillerStacks(killer: Player, stacks: Int) {
            val container = killer.persistentDataContainer
            container.set(NamespacedKey(Hardcraft.instance, "agarStacks"), PersistentDataType.INTEGER, stacks)
        }

        fun adjustAttributes(player: Player, stacks: Int, heal: Boolean = true) {
            // Предполагаем, что stacks принимает значения от 1 до 6.
            // Приводим к индексу от 0 до 5:
            val stageIndex = (stacks - 1).coerceIn(0, 5)
            val totalStages = 5.0 // всего 5 шагов между 6 стадиями

            // Размер (SCALE) от 0.5 до 1.6:
            val scale = 0.5 + (1.6 - 0.5) * (stageIndex / totalStages)

            // Скорость движения: от 0.12 (на маленькой форме) до 0.095 (на большой форме)
            val movementSpeed = 0.135 - (0.009 * stageIndex)

            // Фолл-дамедж: -0.7 -0.35 0 +0.05 +0.1 +0.15
            val fallDamages = listOf(0.35, 0.7, 1.0, 1.05, 1.1, 1.15)
            val fallDamageMultiplier = fallDamages[stageIndex]

            // 2.5 3 3.5 4.5 6.5 7.5 SAFE_FALL_DISTANCE
            val safeDistances = listOf(2.5, 3.0, 3.5, 4.5, 6.5, 7.5)
            val safeFallDistance = safeDistances[stageIndex]

            // Сила прыжка: базовый прыжок умножается на коэффициент от 0.45 0.5 0.55 0.6 0.65 0.7=
            val jumpStrength = 0.45 + 0.05 * stageIndex

            // Длина рук (интервалы взаимодействия)
            // Для блоков: от 1.75 до 5.7
            val blockInteractionRange = 1.75 + (5.7 - 1.75) * (stageIndex / totalStages)
            // Для сущностей: от 1.5 до 4.5
            val entityInteractionRange = 1.5 + (4.5 - 1.5) * (stageIndex / totalStages)

            // Attack damage from 0.5 to 1.6
            val attackDamage = 0.5 + (1.6 - 0.5) * (stageIndex / totalStages)

            // Здоровье – пусть остаётся линейно: 6 единиц на каждую стадию
            val maxHealth = 6.0 * stacks

            // Применяем рассчитанные атрибуты:
            player.getAttribute(Attribute.JUMP_STRENGTH)?.baseValue = jumpStrength
            player.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER)?.baseValue = fallDamageMultiplier
            player.getAttribute(Attribute.SAFE_FALL_DISTANCE)?.baseValue = safeFallDistance
            player.getAttribute(Attribute.SCALE)?.baseValue = scale
            player.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = movementSpeed
            player.getAttribute(Attribute.MAX_HEALTH)?.baseValue = maxHealth
            player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.baseValue = blockInteractionRange
            player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)?.baseValue = entityInteractionRange
            player.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue = attackDamage

            if (heal) {
                // Полностью востанавливаем здоровье
                object : BukkitRunnable() {
                    override fun run() {
                        player.health = maxHealth
                    }
                }.runTaskLater(Hardcraft.instance, 1)
            }
        }
    }
}