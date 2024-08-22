package dev.lrdcxdes.hardcraft.nms.mobs

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.animal.Chicken
import net.minecraft.world.entity.animal.Rabbit
import net.minecraft.world.entity.monster.Spider
import org.bukkit.craftbukkit.entity.CraftSpider
import org.bukkit.entity.Entity

class CustomSpider {
    class SpiderTargetGoal<T : LivingEntity?>(spider: Spider, targetEntityClass: Class<T>) :
        NearestAttackableTargetGoal<T>(spider, targetEntityClass, true) {
        @Suppress("DEPRECATION")
        override fun canUse(): Boolean {
            val f = mob.lightLevelDependentMagicValue

            return if (f >= 0.5f) false else super.canUse()
        }
    }

    companion object {
        fun setGoals(ent: Entity) {
            val spider = (ent as CraftSpider).handle
            spider.targetSelector.addGoal(
                3, SpiderTargetGoal(
                    spider,
                    Chicken::class.java
                )
            )
            spider.targetSelector.addGoal(
                3, SpiderTargetGoal(
                    spider,
                    Rabbit::class.java
                )
            )
        }
    }
}