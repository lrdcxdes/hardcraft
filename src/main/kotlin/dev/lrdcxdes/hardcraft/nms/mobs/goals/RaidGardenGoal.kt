package dev.lrdcxdes.hardcraft.nms.mobs.goals

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.gameevent.GameEvent
import org.bukkit.craftbukkit.event.CraftEventFactory

class RaidGardenGoal(
    private val animal: PathfinderMob,
    private val raidBlocks: List<Class<out CropBlock>> = listOf(),
) :
    MoveToBlockGoal(animal, 0.699999988079071, 16) {
    private var wantsToRaid = false
    private var canRaid = false

    override fun canUse(): Boolean {
        if (this.nextStartTick <= 0) {
            if (!animal.level().gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false
            }

            this.canRaid = false
            try {
                val method = animal.javaClass.getMethod("wantsMoreFood")
                this.wantsToRaid = method.invoke(animal) as Boolean
            } catch (e: NoSuchMethodException) {
                println("Method wantsMoreFood not found")
                this.wantsToRaid = false
            }
        }

        return super.canUse()
    }

    override fun canContinueToUse(): Boolean {
        return this.canRaid && super.canContinueToUse()
    }

    override fun tick() {
        super.tick()
        animal.lookControl.setLookAt(
            blockPos.x.toDouble() + 0.5, (blockPos.y + 1).toDouble(),
            blockPos.z.toDouble() + 0.5, 10.0f, animal.maxHeadXRot.toFloat()
        )
        if (this.isReachedTarget) {
            val world = animal.level()
            val blockposition = blockPos.above()
            val iblockdata = world.getBlockState(blockposition)
            val block = iblockdata.block

            if (this.canRaid && block is CropBlock && block.javaClass in raidBlocks) {
                // get field public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
                val age = block.javaClass.getField("AGE").get(null) as IntegerProperty
                val i = iblockdata.getValue(age)

                if (i == 0) {
                    // CraftBukkit start
                    if (!CraftEventFactory.callEntityChangeBlockEvent(
                            this.animal,
                            blockposition,
                            iblockdata.fluidState.createLegacyBlock()
                        )
                    ) { // Paper - fix wrong block state
                        return
                    }
                    // CraftBukkit end
                    world.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 2)
                    world.destroyBlock(blockposition, true, this.animal)
                } else {
                    // CraftBukkit start
                    if (!CraftEventFactory.callEntityChangeBlockEvent(
                            this.animal,
                            blockposition,
                            iblockdata.setValue(age, i - 1)
                        )
                    ) {
                        return
                    }
                    // CraftBukkit end
                    world.setBlock(blockposition, iblockdata.setValue(age, i - 1) as BlockState, 2)
                    world.gameEvent(
                        GameEvent.BLOCK_CHANGE, blockposition, GameEvent.Context.of(
                            animal as Entity
                        )
                    )
                    world.levelEvent(2001, blockposition, Block.getId(iblockdata))
                }

//                animal.moreFoodTicks = 40
                // check if has .moreFoodTicks field
                try {
                    val method = animal.javaClass.getMethod("setMoreFoodTicks", Int::class.java)
                    method.invoke(animal, 40)
                } catch (e: NoSuchMethodException) {
                    println("Method moreFoodTicks not found")
                }
            }

            this.canRaid = false
            this.nextStartTick = 10
        }
    }

    override fun isValidTarget(world: LevelReader, pos: BlockPos): Boolean {
        var iblockdata = world.getBlockState(pos)

        if (iblockdata.`is`(Blocks.FARMLAND) && this.wantsToRaid && !this.canRaid) {
            iblockdata = world.getBlockState(pos.above())
            val block = iblockdata.block
            if (block is CropBlock && block.javaClass in raidBlocks && block.isMaxAge(iblockdata)) {
                this.canRaid = true
                return true
            }
        }

        return false
    }
}