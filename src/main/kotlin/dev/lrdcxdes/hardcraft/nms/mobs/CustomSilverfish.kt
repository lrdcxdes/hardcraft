package dev.lrdcxdes.hardcraft.nms.mobs

import dev.lrdcxdes.hardcraft.nms.mobs.goals.RaidGardenGoal
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.monster.Silverfish
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.block.*
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.event.CraftEventFactory
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityRemoveEvent
import java.util.*

class CustomSilverfish(world: ServerLevel) : Silverfish(EntityType.SILVERFISH, world) {
    private class SilverfishWakeUpFriendsGoal(private val silverfish: Silverfish) : Goal() {
        private var lookForFriends = 0

        fun notifyHurt() {
            if (this.lookForFriends == 0) {
                this.lookForFriends = this.adjustedTickDelay(20)
            }
        }

        override fun canUse(): Boolean {
            return this.lookForFriends > 0
        }

        override fun tick() {
            --this.lookForFriends
            if (this.lookForFriends <= 0) {
                val world = silverfish.level()
                val randomsource = silverfish.getRandom()
                val blockposition = silverfish.blockPosition()

                var i = 0
                while (i <= 5 && i >= -5) {
                    var j = 0
                    while (j <= 10 && j >= -10) {
                        var k = 0
                        while (k <= 10 && k >= -10) {
                            val blockposition1 = blockposition.offset(j, i, k)
                            val iblockdata = world.getBlockState(blockposition1)
                            val block = iblockdata.block

                            if (block is InfestedBlock) {
                                // CraftBukkit start
                                val afterState =
                                    if (world.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) iblockdata.fluidState.createLegacyBlock() else block.hostStateByInfested(
                                        world.getBlockState(blockposition1)
                                    ) // Paper - fix wrong block state
                                if (!CraftEventFactory.callEntityChangeBlockEvent(
                                        this.silverfish,
                                        blockposition1,
                                        afterState
                                    )
                                ) { // Paper - fix wrong block state
                                    k = (if (k <= 0) 1 else 0) - k
                                    continue
                                }
                                // CraftBukkit end
                                if (world.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) {
                                    world.destroyBlock(blockposition1, true, this.silverfish)
                                } else {
                                    world.setBlock(
                                        blockposition1,
                                        block.hostStateByInfested(world.getBlockState(blockposition1)),
                                        3
                                    )
                                }

                                if (randomsource.nextBoolean()) {
                                    return
                                }
                            }
                            k = (if (k <= 0) 1 else 0) - k
                        }
                        j = (if (j <= 0) 1 else 0) - j
                    }
                    i = (if (i <= 0) 1 else 0) - i
                }
            }
        }
    }

    private class SilverfishMergeWithStoneGoal(silverfish: Silverfish) :
        RandomStrollGoal(silverfish, 1.0, 10) {
        private var selectedDirection: Direction? = null
        private var doMerge = false

        init {
            this.setFlags(EnumSet.of(Flag.MOVE))
        }

        override fun canUse(): Boolean {
            if (mob.target != null) {
                return false
            } else if (!mob.navigation.isDone) {
                return false
            } else {
                val randomsource = mob.getRandom()

                if (mob.level().gameRules.getBoolean(GameRules.RULE_MOBGRIEFING) && randomsource.nextInt(
                        reducedTickDelay(10)
                    ) == 0
                ) {
                    this.selectedDirection = Direction.getRandom(randomsource)
                    val blockposition = BlockPos.containing(
                        mob.x,
                        mob.y + 0.5, mob.z
                    ).relative(this.selectedDirection!!)
                    val iblockdata = mob.level().getBlockState(blockposition)

                    if (InfestedBlock.isCompatibleHostBlock(iblockdata)) {
                        this.doMerge = true
                        return true
                    }
                }

                this.doMerge = false
                return super.canUse()
            }
        }

        override fun canContinueToUse(): Boolean {
            return if (this.doMerge) false else super.canContinueToUse()
        }

        override fun start() {
            if (!this.doMerge) {
                super.start()
            } else {
                val world = mob.level()
                val blockposition = BlockPos.containing(mob.x, mob.y + 0.5, mob.z).relative(this.selectedDirection!!)
                val iblockdata = world.getBlockState(blockposition)

                if (InfestedBlock.isCompatibleHostBlock(iblockdata)) {
                    // CraftBukkit start
                    if (!CraftEventFactory.callEntityChangeBlockEvent(
                            this.mob,
                            blockposition,
                            InfestedBlock.infestedStateByHost(iblockdata)
                        )
                    ) {
                        return
                    }
                    // CraftBukkit end
                    world.setBlock(blockposition, InfestedBlock.infestedStateByHost(iblockdata), 3)
                    mob.spawnAnim()
                    mob.discard(EntityRemoveEvent.Cause.ENTER_BLOCK) // CraftBukkit - add Bukkit remove cause
                }
            }
        }
    }

    private var friendsGoal: SilverfishWakeUpFriendsGoal? = null

    override fun registerGoals() {
        this.friendsGoal = SilverfishWakeUpFriendsGoal(this)
        goalSelector.addGoal(1, FloatGoal(this))
//        goalSelector.addGoal(1, ClimbOnTopOfPowderSnowGoal(this, this.level()))
//        goalSelector.addGoal(3, this.friendsGoal!!)
//        goalSelector.addGoal(4, MeleeAttackGoal(this, 1.0, false))
        goalSelector.addGoal(
            2, RaidGardenGoal(
                this, listOf(
                    CropBlock::class.java,
                    CarrotBlock::class.java,
                    PotatoBlock::class.java,
                    BeetrootBlock::class.java
                )
            )
        )
        goalSelector.addGoal(6, SilverfishMergeWithStoneGoal(this))
//        targetSelector.addGoal(1, (HurtByTargetGoal(this, *arrayOfNulls(0))).setAlertOthers())
//        targetSelector.addGoal(
//            2, NearestAttackableTargetGoal(
//                this,
//                Player::class.java, true
//            )
//        )
    }

    override fun getAttributes(): AttributeMap {
        return AttributeMap(createAttributes().build())
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (this.isInvulnerableTo(source)) {
            return false
        } else {
            if ((source.entity != null || source.`is`(DamageTypeTags.ALWAYS_TRIGGERS_SILVERFISH)) && this.friendsGoal != null) {
                friendsGoal!!.notifyHurt()
            }

            return super.hurt(source, amount)
        }
    }

    fun spawn(loc: org.bukkit.Location) {
        this.setPosRaw(loc.x, loc.y, loc.z)
        this.persist = true
        (loc.world as CraftWorld).handle.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        println("CustomSilverfish spawned")
    }

    private var moreFoodTicks = 0

    fun wantsMoreFood(): Boolean {
        return this.moreFoodTicks <= 0
    }

    fun setMoreFoodTicks(ticks: Int) {
        this.moreFoodTicks = ticks
    }

    override fun customServerAiStep() {
        if (this.moreFoodTicks > 0) {
            this.moreFoodTicks -= random.nextInt(3)
            if (this.moreFoodTicks < 0) {
                this.moreFoodTicks = 0
            }
        }
        super.customServerAiStep()
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
        }
    }
}