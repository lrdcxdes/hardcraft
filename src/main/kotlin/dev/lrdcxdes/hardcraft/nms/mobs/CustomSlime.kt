package dev.lrdcxdes.hardcraft.nms.mobs

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.animal.Chicken
import net.minecraft.world.entity.animal.Rabbit
import net.minecraft.world.entity.animal.frog.Frog
import org.bukkit.craftbukkit.entity.CraftSlime
import org.bukkit.entity.Entity

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
        }
    }
}