package dev.lrdcxdes.hardcraft.nms.mobs

import dev.lrdcxdes.hardcraft.Hardcraft
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.animal.Chicken
import net.minecraft.world.entity.animal.Rabbit
import net.minecraft.world.entity.animal.frog.Frog
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftSlime
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Matrix4f

data class SlimeColor(val cmd: Int, val name: String)

class CustomSlime {
    companion object {
        fun setGoals(entity: Entity) {
            val handle = (entity as CraftSlime).handle
            handle.targetSelector.addGoal(
                1, NearestAttackableTargetGoal(
                    handle,
                    Frog::class.java, true
                )
            )
            handle.targetSelector.addGoal(
                1, NearestAttackableTargetGoal(
                    handle,
                    Rabbit::class.java, true
                )
            )
            handle.targetSelector.addGoal(
                1, NearestAttackableTargetGoal(
                    handle,
                    Chicken::class.java, true
                )
            )

            // if already a passenger, ignore
            if (entity.passengers.isNotEmpty()) {
                return
            }

            val cmd = (Math.random() * 12).toInt() + 1

            val name: String = when (cmd) {
                1 -> "Blue Slime"
                2 -> "Dark Purple Slime"
                3 -> "Green Slime"
                4 -> "Light Blue Slime"
                5 -> "Lime Slime"
                6 -> "Orange Slime"
                7 -> "Pink Slime"
                8 -> "Purple Slime"
                9 -> "Red Slime"
                10 -> "Yellow Slime"
                11 -> "White Slime"
                12 -> "Black Slime"
                else -> "Green Slime"
            }

            entity.isCustomNameVisible = false
            entity.customName(Component.text(name))

            // Slimes
            entity.addPassenger(entity.world.spawn(entity.location, ItemDisplay::class.java).apply {
                setItemStack(ItemStack(Material.SLIME_BALL).apply {
                    itemMeta = itemMeta.apply {
                        setCustomModelData(cmd)
                        itemName(Hardcraft.minimessage.deserialize("<lang:bts.color_crystal>"))
                    }
                })

                // translation y 0.2
                // scale x 0.6, y 0.6, z 0.6

                setTransformationMatrix(
                    Matrix4f().apply {
                        setTranslation(0.0f, 0.2f, 0.0f)
                        scale(0.6f)
                    }
                )
            })
        }
    }
}