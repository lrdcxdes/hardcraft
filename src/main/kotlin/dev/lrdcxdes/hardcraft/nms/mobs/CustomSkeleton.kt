package dev.lrdcxdes.hardcraft.nms.mobs

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.npc.AbstractVillager

class CustomSkeleton {
    companion object {
        fun setGoals(entity: org.bukkit.entity.Skeleton) {
            val skeleton = (entity as org.bukkit.craftbukkit.entity.CraftSkeleton).handle
            skeleton.targetSelector.addGoal(
                3, NearestAttackableTargetGoal(
                    skeleton,
                    AbstractVillager::class.java, false
                )
            )
        }
    }
}