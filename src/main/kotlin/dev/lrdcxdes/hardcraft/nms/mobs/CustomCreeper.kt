package dev.lrdcxdes.hardcraft.nms.mobs

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.animal.horse.AbstractHorse
import org.bukkit.craftbukkit.entity.CraftCreeper
import org.bukkit.entity.Entity

class CustomCreeper {
    companion object {
        fun setGoals(ent: Entity) {
            val creeper = (ent as CraftCreeper).handle
            creeper.targetSelector.addGoal(
                1, NearestAttackableTargetGoal(
                    creeper,
                    AbstractHorse::class.java, true
                )
            )
        }
    }
}