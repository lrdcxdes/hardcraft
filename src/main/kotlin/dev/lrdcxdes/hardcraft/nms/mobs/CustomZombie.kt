package dev.lrdcxdes.hardcraft.nms.mobs

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.animal.Chicken
import net.minecraft.world.entity.animal.Cow
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.entity.animal.Sheep
import org.bukkit.craftbukkit.entity.CraftZombie
import org.bukkit.entity.Entity

class CustomZombie {
    companion object {
        fun setGoals(ent: Entity) {
            val zombie = (ent as CraftZombie).handle
            zombie.targetSelector.addGoal(
                3, NearestAttackableTargetGoal(
                    zombie,
                    Chicken::class.java, true
                )
            )
            zombie.targetSelector.addGoal(
                3, NearestAttackableTargetGoal(
                    zombie,
                    Pig::class.java, true
                )
            )
            zombie.targetSelector.addGoal(
                3, NearestAttackableTargetGoal(
                    zombie,
                    Cow::class.java, true
                )
            )
            zombie.targetSelector.addGoal(
                3, NearestAttackableTargetGoal(
                    zombie,
                    Sheep::class.java, true
                )
            )
        }
    }
}