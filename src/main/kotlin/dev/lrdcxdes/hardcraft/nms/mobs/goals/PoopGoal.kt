package dev.lrdcxdes.hardcraft.nms.mobs.goals

import dev.lrdcxdes.hardcraft.Hardcraft
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.level.gameevent.GameEvent
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack


// CraftBukkit end
open class PoopGoal(
    private val mob: PathfinderMob,
    private val poopDelay: IntRange,
) :
    Goal() {
    private var withoutPoopTicks = 0
    private var waitToPoop = poopDelay.random()

    override fun canUse(): Boolean {
        return true
    }

    private fun poop() {
        this.mob.playSound(SoundEvents.CHICKEN_EGG, 1.0f, 2.0f)

        this.mob.forceDrops = true // CraftBukkit
        val itemStack = ItemStack(Material.BROWN_DYE).apply {
            amount = 1
            itemMeta = itemMeta.apply {
                itemName(Hardcraft.minimessage.deserialize("<color:#562B00><lang:btn.poop></color>"))
                setCustomModelData(3)
            }
        }
        val stack = CraftItemStack.asNMSCopy(itemStack)
        this.mob.spawnAtLocation(this.mob.level() as ServerLevel, stack, 0f)
        this.mob.forceDrops = false // CraftBukkit
        this.mob.gameEvent(GameEvent.ENTITY_PLACE)
    }

    override fun tick() {
        withoutPoopTicks++
        if (withoutPoopTicks >= waitToPoop) {
            poop()
            withoutPoopTicks = 0
            waitToPoop = poopDelay.random()
        }
    }
}
