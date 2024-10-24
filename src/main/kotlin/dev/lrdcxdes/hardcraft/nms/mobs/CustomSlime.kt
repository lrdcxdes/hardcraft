package dev.lrdcxdes.hardcraft.nms.mobs

import dev.lrdcxdes.hardcraft.Hardcraft
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

            // Slimes
            entity.addPassenger(entity.world.spawn(entity.location, ItemDisplay::class.java).apply {
                setItemStack(ItemStack(Material.SLIME_BALL).apply {
                    itemMeta = itemMeta.apply {
                        val rnd = (Math.random() * 100).toInt()

                        val cmd = when {
                            rnd < 15 -> 1
                            rnd < 25 -> 2
                            rnd < 40 -> 3
                            rnd < 50 -> 4
                            rnd < 65 -> 5
                            rnd < 70 -> 6
                            rnd < 75 -> 7
                            rnd < 90 -> 8
                            rnd < 95 -> 9
                            else -> 10
                        }
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