package dev.lrdcxdes.hardcraft.nms.mobs

import dev.lrdcxdes.hardcraft.Hardcraft
import dev.lrdcxdes.hardcraft.nms.mobs.goals.PoopGoal
import io.papermc.paper.event.entity.EntityToggleSitEvent
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.stats.Stats
import net.minecraft.tags.FluidTags
import net.minecraft.tags.ItemTags
import net.minecraft.util.Mth
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.LookControl
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.animal.*
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Silverfish
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.event.CraftEventFactory
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*
import java.util.function.Predicate
import kotlin.math.*

class CustomChicken(world: Level) : Chicken(EntityType.CHICKEN, world) {
//    val DATA_TYPE_ID: EntityDataAccessor<Int> = SynchedEntityData.defineId(
//        CustomChicken::class.java, EntityDataSerializers.INT
//    )
//    val DATA_FLAGS_ID: EntityDataAccessor<Byte> = SynchedEntityData.defineId(
//        CustomChicken::class.java, EntityDataSerializers.BYTE
//    )
    private val FLAG_SITTING: Int = 1
    val FLAG_CROUCHING: Int = 4
    val FLAG_INTERESTED: Int = 8
    val FLAG_POUNCING: Int = 16
    private val FLAG_SLEEPING: Int = 32
    private val FLAG_FACEPLANTED: Int = 64
    private val FLAG_DEFENDING: Int = 128

    val STALKABLE_PREY: Predicate<Entity> =
        Predicate { entity: Entity? -> entity is Silverfish }

    val ALLOWED_ITEMS: Predicate<ItemEntity> =
        Predicate { entityitem: ItemEntity -> !entityitem.hasPickUpDelay() && entityitem.isAlive }

    private val trustedUUIDs: MutableSet<UUID> = mutableSetOf()
    private val getTrustedUUIDs: () -> Set<UUID> = { this.trustedUUIDs }

    private var interestedAngle = 0f
    private var interestedAngleO = 0f
    var crouchAmount: Float = 0f
    var crouchAmountO: Float = 0f

    private val flags: MutableSet<Int> = mutableSetOf()

    private fun getFlag(bitmask: Int): Boolean {
        return flags.contains(bitmask)
    }

    private fun setFlag(mask: Int, value: Boolean) {
        if (value) {
            flags.add(mask)
        } else {
            flags.remove(mask)
        }
    }
//
//    private fun getFlag(bitmask: Int): Boolean {
//        return ((entityData.get(DATA_FLAGS_ID) as Byte).toInt() and bitmask) != 0
//    }
//
//    private fun setFlag(mask: Int, value: Boolean) {
//        if (value) {
//            entityData.set(DATA_FLAGS_ID, ((entityData.get(DATA_FLAGS_ID) as Byte).toInt() or mask).toByte())
//        } else {
//            entityData.set(
//                DATA_FLAGS_ID,
//                ((entityData.get(DATA_FLAGS_ID) as Byte).toInt() and mask.inv()).toByte()
//            )
//        }
//    }

    private fun isDefending(): Boolean {
        return this.getFlag(128)
    }

    fun isInterested(): Boolean {
        return this.getFlag(8)
    }

    fun setIsInterested(interested: Boolean) {
        this.setFlag(8, interested)
    }

    fun isPouncing(): Boolean {
        return this.getFlag(16)
    }

    fun setIsPouncing(chasing: Boolean) {
        this.setFlag(16, chasing)
    }

    override fun isCrouching(): Boolean {
        return this.getFlag(4)
    }

    fun setIsCrouching(crouching: Boolean) {
        this.setFlag(4, crouching)
    }

    fun isSitting(): Boolean {
        return this.getFlag(1)
    }

    private fun setSitting(sitting: Boolean, fireEvent: Boolean) {
        if (fireEvent && !EntityToggleSitEvent(this.bukkitEntity, sitting).callEvent()) return
        // Paper start - Add EntityToggleSitEvent
        this.setFlag(1, sitting)
    }

    fun setSitting(sitting: Boolean) {
        this.setSitting(sitting, true)
    }

    fun isJumping(): Boolean {
        return this.jumping
    }

    private fun setDefending(aggressive: Boolean) {
        this.setFlag(128, aggressive)
    }

    fun isFaceplanted(): Boolean {
        return this.getFlag(64)
    }

    fun setFaceplanted(walking: Boolean) {
        this.setFlag(64, walking)
    }

    fun isFullyCrouched(): Boolean {
        return this.crouchAmount == 3.0f
    }

    private fun setSleeping(sleeping: Boolean) {
        this.setFlag(32, sleeping)
    }

    override fun isSleeping(): Boolean {
        return this.getFlag(32)
    }

    override fun setTarget(target: LivingEntity?) {
        if (this.isDefending() && target == null) {
            this.setDefending(false)
        }

        super.setTarget(target)
    }

    fun isPathClear(chicken: CustomChicken, chasedEntity: LivingEntity): Boolean {
        val d0 = chasedEntity.z - chicken.z
        val d1 = chasedEntity.x - chicken.x
        val d2 = d0 / d1
        val flag = true

        for (i in 0..5) {
            val d3 = if (d2 == 0.0) 0.0 else d0 * (i.toFloat() / 6.0f).toDouble()
            val d4 = if (d2 == 0.0) d1 * (i.toFloat() / 6.0f).toDouble() else d3 / d2

            for (j in 1..3) {
                if (!chicken.level()
                        .getBlockState(BlockPos.containing(chicken.x + d4, chicken.y + j.toDouble(), chicken.z + d3))
                        .canBeReplaced()
                ) {
                    return false
                }
            }
        }

        return true
    }

    private inner class StalkPreyGoal : Goal() {
        init {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK))
        }

        override fun canUse(): Boolean {
            val entityliving: LivingEntity? = this@CustomChicken.target

            return entityliving != null && entityliving.isAlive && STALKABLE_PREY.test(entityliving) && this@CustomChicken.distanceToSqr(
                entityliving as Entity
            ) > 36.0 && !this@CustomChicken.isCrouching && !this@CustomChicken.isInterested() && !this@CustomChicken.jumping
        }

        override fun start() {
            this@CustomChicken.setSitting(false)
            this@CustomChicken.setFaceplanted(false)
        }

        override fun stop() {
            val entityliving: LivingEntity? = this@CustomChicken.target

            if (entityliving != null && isPathClear(this@CustomChicken, entityliving)) {
                this@CustomChicken.setIsInterested(true)
                this@CustomChicken.setIsCrouching(true)
                this@CustomChicken.getNavigation().stop()
                this@CustomChicken.getLookControl().setLookAt(
                    entityliving,
                    this@CustomChicken.maxHeadYRot.toFloat(),
                    this@CustomChicken.maxHeadXRot.toFloat()
                )
            } else {
                this@CustomChicken.setIsInterested(false)
                this@CustomChicken.setIsCrouching(false)
            }
        }

        override fun tick() {
            val entityliving: LivingEntity? = this@CustomChicken.target

            if (entityliving != null) {
                this@CustomChicken.getLookControl().setLookAt(
                    entityliving,
                    this@CustomChicken.maxHeadYRot.toFloat(),
                    this@CustomChicken.maxHeadXRot.toFloat()
                )
                if (this@CustomChicken.distanceToSqr(entityliving as Entity) <= 36.0) {
                    this@CustomChicken.setIsInterested(true)
                    this@CustomChicken.setIsCrouching(true)
                    this@CustomChicken.getNavigation().stop()
                } else {
                    this@CustomChicken.getNavigation().moveTo(entityliving as Entity, 1.5)
                }
            }
        }
    }

    private class ChickenFollowParentGoal(private val chicken: CustomChicken, d0: Double) :
        FollowParentGoal(chicken, d0) {

        override fun canUse(): Boolean {
            return !this.chicken.isDefending() && super.canUse()
        }

        override fun canContinueToUse(): Boolean {
            return !this.chicken.isDefending() && super.canContinueToUse()
        }

        override fun start() {
            this.chicken.clearStates()
            super.start()
        }
    }

    private inner class FaceplantGoal : Goal() {
        var countdown: Int = 0

        init {
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.JUMP, Flag.MOVE))
        }

        override fun canUse(): Boolean {
            return this@CustomChicken.isFaceplanted()
        }

        override fun canContinueToUse(): Boolean {
            return this.canUse() && this.countdown > 0
        }

        override fun start() {
            this.countdown = this.adjustedTickDelay(40)
        }

        override fun stop() {
            this@CustomChicken.setFaceplanted(false)
        }

        override fun tick() {
            --this.countdown
        }
    }

    private inner class ChickenPanicGoal(d0: Double) : PanicGoal(this@CustomChicken, d0) {
        public override fun shouldPanic(): Boolean {
            return !this@CustomChicken.isDefending() && super.shouldPanic()
        }
    }

    inner class ChickenPounceGoal : JumpGoal() {
        override fun canUse(): Boolean {
            if (!this@CustomChicken.isFullyCrouched()) {
                return false
            } else {
                val entityliving: LivingEntity? = this@CustomChicken.target

                if (entityliving != null && entityliving.isAlive) {
                    if (entityliving.motionDirection != entityliving.direction) {
                        return false
                    } else {
                        val flag = isPathClear(this@CustomChicken, entityliving)

                        if (!flag) {
                            this@CustomChicken.getNavigation().createPath(entityliving as Entity, 0)
                            this@CustomChicken.setIsCrouching(false)
                            this@CustomChicken.setIsInterested(false)
                        }

                        return flag
                    }
                } else {
                    return false
                }
            }
        }

        override fun canContinueToUse(): Boolean {
            val entityliving: LivingEntity? = this@CustomChicken.target

            if (entityliving != null && entityliving.isAlive) {
                val d0: Double = this@CustomChicken.deltaMovement.y

                return (d0 * d0 >= 0.05000000074505806 || abs(
                    this@CustomChicken.xRot.toDouble()
                ) >= 15.0f || !this@CustomChicken.onGround()) && !this@CustomChicken.isFaceplanted()
            } else {
                return false
            }
        }

        override fun isInterruptable(): Boolean {
            return false
        }

        override fun start() {
            this@CustomChicken.setJumping(true)
            this@CustomChicken.setIsPouncing(true)
            this@CustomChicken.setIsInterested(false)
            val entityliving: LivingEntity? = this@CustomChicken.target

            if (entityliving != null) {
                this@CustomChicken.getLookControl().setLookAt(entityliving, 60.0f, 30.0f)
                val vec3d = (Vec3(
                    entityliving.x - this@CustomChicken.x,
                    entityliving.y - this@CustomChicken.y,
                    entityliving.z - this@CustomChicken.z
                )).normalize()

                this@CustomChicken.deltaMovement =
                    this@CustomChicken.deltaMovement.add(vec3d.x * 0.8, 0.9, vec3d.z * 0.8)
            }

            this@CustomChicken.getNavigation().stop()
        }

        override fun stop() {
            this@CustomChicken.setIsCrouching(false)
            this@CustomChicken.crouchAmount = 0.0f
            this@CustomChicken.crouchAmountO = 0.0f
            this@CustomChicken.setIsInterested(false)
            this@CustomChicken.setIsPouncing(false)
        }

        override fun tick() {
            val entityliving: LivingEntity? = this@CustomChicken.target

            if (entityliving != null) {
                this@CustomChicken.getLookControl().setLookAt(entityliving, 60.0f, 30.0f)
            }

            if (!this@CustomChicken.isFaceplanted()) {
                val vec3d: Vec3 = this@CustomChicken.deltaMovement

                if (vec3d.y * vec3d.y < 0.029999999329447746 && this@CustomChicken.xRot != 0.0f) {
                    this@CustomChicken.xRot = Mth.rotLerp(0.2f, this@CustomChicken.xRot, 0.0f)
                } else {
                    val d0 = vec3d.horizontalDistance()
                    val d1 = sign(-vec3d.y) * acos(d0 / vec3d.length()) * 57.2957763671875

                    this@CustomChicken.xRot = d1.toFloat()
                }
            }

            if (entityliving != null && this@CustomChicken.distanceTo(entityliving) <= 2.0f) {
                this@CustomChicken.doHurtTarget(entityliving)
            } else if (this@CustomChicken.xRot > 0.0f && this@CustomChicken.onGround() && this@CustomChicken.deltaMovement.y.toFloat() != 0.0f && this@CustomChicken.level()
                    .getBlockState(
                        this@CustomChicken.blockPosition()
                    ).`is`(Blocks.SNOW)
            ) {
                this@CustomChicken.xRot = 60.0f
                this@CustomChicken.setTarget(null as LivingEntity?)
                this@CustomChicken.setFaceplanted(true)
            }
        }
    }

    private inner class ChickenMeleeAttackGoal(d0: Double, flag: Boolean) :
        MeleeAttackGoal(this@CustomChicken, d0, flag) {
        override fun checkAndPerformAttack(target: LivingEntity) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown()
                mob.doHurtTarget(target)
                this@CustomChicken.playSound(SoundEvents.CHICKEN_AMBIENT, 1.0f, 1.0f)
            }
        }

        override fun start() {
            this@CustomChicken.setIsInterested(false)
            super.start()
        }

        override fun canUse(): Boolean {
            return !this@CustomChicken.isSitting() && !this@CustomChicken.isSleeping && !this@CustomChicken.isCrouching && !this@CustomChicken.isFaceplanted() && super.canUse()
        }
    }

    inner class ChickenLookControl : LookControl(this@CustomChicken) {
        override fun tick() {
            if (!this@CustomChicken.isSleeping) {
                super.tick()
            }
        }

        override fun resetXRotOnTick(): Boolean {
            return !this@CustomChicken.isPouncing() && !this@CustomChicken.isCrouching && !this@CustomChicken.isInterested() && !this@CustomChicken.isFaceplanted()
        }
    }

    private inner class ChickenFloatGoal : FloatGoal(this@CustomChicken) {
        override fun start() {
            super.start()
            this@CustomChicken.clearStates()
        }

        override fun canUse(): Boolean {
            return this@CustomChicken.isInWater && this@CustomChicken.getFluidHeight(FluidTags.WATER) > 0.25 || this@CustomChicken.isInLava
        }
    }

    inner class AlertableEntitiesSelector : Predicate<LivingEntity> {
        override fun test(entityliving: LivingEntity): Boolean {
            return if (entityliving is Chicken) false
            else (
                    if (entityliving !is Silverfish && entityliving !is Monster) (
                            if (entityliving is TamableAnimal)
                                !entityliving.isTame
                            else (
                                    if (
                                        entityliving is Player &&
                                        (entityliving.isSpectator() || entityliving.isCreative)
                                    ) (false)
                                    else (!entityliving.isSleeping && !entityliving.isDiscrete)
                                    )
                            ) else (true)
                    )
        }
    }

    private abstract inner class ChickenBehaviorGoal : Goal() {
        private val alertableTargeting: TargetingConditions =
            TargetingConditions.forCombat().range(12.0).ignoreLineOfSight().selector(
                this@CustomChicken.AlertableEntitiesSelector()
            )

        protected fun alertable(): Boolean {
            return this@CustomChicken.level().getNearbyEntities(
                LivingEntity::class.java,
                this.alertableTargeting,
                this@CustomChicken,
                this@CustomChicken.boundingBox.inflate(12.0, 6.0, 12.0)
            ).isNotEmpty()
        }
    }

    private inner class PerchAndSearchGoal : ChickenBehaviorGoal() {
        private var relX = 0.0
        private var relZ = 0.0
        private var lookTime = 0
        private var looksRemaining = 0

        init {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK))
        }

        override fun canUse(): Boolean {
            return this@CustomChicken.getLastHurtByMob() == null && this@CustomChicken.getRandom()
                .nextFloat() < 0.02f && !this@CustomChicken.isSleeping && this@CustomChicken.target == null && this@CustomChicken.getNavigation()
                .isDone && !this.alertable() && !this@CustomChicken.isPouncing() && !this@CustomChicken.isCrouching
        }

        override fun canContinueToUse(): Boolean {
            return this.looksRemaining > 0
        }

        override fun start() {
            this.resetLook()
            this.looksRemaining = 2 + this@CustomChicken.getRandom().nextInt(3)
            this@CustomChicken.setSitting(true)
            this@CustomChicken.getNavigation().stop()
        }

        override fun stop() {
            this@CustomChicken.setSitting(false)
        }

        override fun tick() {
            --this.lookTime
            if (this.lookTime <= 0) {
                --this.looksRemaining
                this.resetLook()
            }

            this@CustomChicken.getLookControl().setLookAt(
                this@CustomChicken.x + this.relX,
                this@CustomChicken.eyeY,
                this@CustomChicken.z + this.relZ,
                this@CustomChicken.maxHeadYRot.toFloat(),
                this@CustomChicken.maxHeadXRot.toFloat()
            )
        }

        fun resetLook() {
            val d0: Double = 6.283185307179586 * this@CustomChicken.getRandom().nextDouble()

            this.relX = cos(d0)
            this.relZ = sin(d0)
            this.lookTime = this.adjustedTickDelay(80 + this@CustomChicken.getRandom().nextInt(20))
        }
    }

    private inner class ChickenSearchForItemsGoal : Goal() {
        init {
            this.setFlags(EnumSet.of(Flag.MOVE))
        }

        override fun canUse(): Boolean {
            if (!this@CustomChicken.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty) {
                return false
            } else if (this@CustomChicken.target == null && this@CustomChicken.getLastHurtByMob() == null) {
                if (!this@CustomChicken.canMove()) {
                    return false
                } else if (this@CustomChicken.getRandom().nextInt(reducedTickDelay(10)) != 0) {
                    return false
                } else {
                    val list: List<ItemEntity> = this@CustomChicken.level().getEntitiesOfClass<ItemEntity>(
                        ItemEntity::class.java,
                        this@CustomChicken.boundingBox.inflate(8.0, 8.0, 8.0),
                        ALLOWED_ITEMS
                    )

                    return list.isNotEmpty() && this@CustomChicken.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty
                }
            } else {
                return false
            }
        }

        override fun tick() {
            val list: List<ItemEntity> = this@CustomChicken.level().getEntitiesOfClass(
                ItemEntity::class.java, this@CustomChicken.boundingBox.inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS
            )
            val itemstack: ItemStack = this@CustomChicken.getItemBySlot(EquipmentSlot.MAINHAND)

            if (itemstack.isEmpty && !list.isEmpty()) {
                this@CustomChicken.getNavigation().moveTo(list[0] as Entity, 1.2000000476837158)
            }
        }

        override fun start() {
            val list: List<ItemEntity> = this@CustomChicken.level().getEntitiesOfClass<ItemEntity>(
                ItemEntity::class.java, this@CustomChicken.boundingBox.inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS
            )

            if (!list.isEmpty()) {
                this@CustomChicken.getNavigation().moveTo(list[0] as Entity, 1.2000000476837158)
            }
        }
    }

    private inner class ChickenLookAtPlayerGoal(mob: Mob, targetType: Class<out LivingEntity>, range: Float) :
        LookAtPlayerGoal(mob, targetType, range) {
        override fun canUse(): Boolean {
            return super.canUse() && !this@CustomChicken.isFaceplanted() && !this@CustomChicken.isInterested()
        }

        override fun canContinueToUse(): Boolean {
            return super.canContinueToUse() && !this@CustomChicken.isFaceplanted() && !this@CustomChicken.isInterested()
        }
    }

    private inner class DefendTrustedTargetGoal(
        oclass: Class<LivingEntity>,
        flag: Boolean,
        flag1: Boolean,
        predicate: Predicate<LivingEntity>?
    ) :
        NearestAttackableTargetGoal<LivingEntity>(this@CustomChicken, oclass, 10, flag, flag1, predicate) {
        private var trustedLastHurtBy: LivingEntity? = null
        private var trustedLastHurt: LivingEntity? = null
        private var timestamp = 0

        override fun canUse(): Boolean {
            if (this.randomInterval > 0 && mob.getRandom().nextInt(this.randomInterval) != 0) {
                return false
            } else {
                val iterator: Iterator<*> = this@CustomChicken.getTrustedUUIDs().iterator()

                while (iterator.hasNext()) {
                    val uuid = iterator.next() as UUID?

                    if (uuid != null && this@CustomChicken.level() is ServerLevel) {
                        val entity = (this@CustomChicken.level() as ServerLevel).getEntity(uuid)

                        if (entity is LivingEntity) {
                            this.trustedLastHurt = entity
                            this.trustedLastHurtBy = entity.getLastHurtByMob()
                            val i = entity.getLastHurtByMobTimestamp()

                            return i != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetConditions)
                        }
                    }
                }

                return false
            }
        }

        override fun start() {
            this.setTarget(this.trustedLastHurtBy)
            this.target = this.trustedLastHurtBy
            if (this.trustedLastHurt != null) {
                this.timestamp = trustedLastHurt!!.getLastHurtByMobTimestamp()
            }

            this@CustomChicken.playSound(SoundEvents.FOX_AGGRO, 1.0f, 1.0f)
            this@CustomChicken.setDefending(true)
//            this@CustomChicken.wakeUp()
            super.start()
        }
    }

    private class ChickenBreedGoal(chance: CustomChicken, chicken: Double) :
        BreedGoal(chance, chicken) {
        override fun start() {
            clearStates(animal)
            clearStates(partner)
            super.start()
        }

        override fun breed() {
            val worldserver = level as ServerLevel
            val entityfox = this.partner?.let { animal.getBreedOffspring(worldserver, it) } as Chicken?

            if (entityfox != null) {
                val entityplayer = animal.getLoveCause()
                val entityplayer1 = partner!!.getLoveCause()

                // CraftBukkit start - call EntityBreedEvent
                entityfox.age = -24000
                entityfox.moveTo(animal.x, animal.y, animal.z, 0.0f, 0.0f)
                var experience = animal.getRandom().nextInt(7) + 1
                val entityBreedEvent = CraftEventFactory.callEntityBreedEvent(
                    entityfox,
                    this.animal,
                    this.partner, entityplayer,
                    animal.breedItem, experience
                )
                if (entityBreedEvent.isCancelled) {
                    return
                }
                experience = entityBreedEvent.experience

                // CraftBukkit end
                if (entityplayer1 != null) {
                    entityplayer1.awardStat(Stats.ANIMALS_BRED)
                    this.partner?.let {
                        CriteriaTriggers.BRED_ANIMALS.trigger(
                            entityplayer1, this.animal,
                            it, entityfox
                        )
                    }
                }

                animal.age = 6000
                partner!!.age = 6000
                animal.resetLove()
                partner!!.resetLove()
                worldserver.addFreshEntityWithPassengers(
                    entityfox,
                    CreatureSpawnEvent.SpawnReason.BREEDING
                ) // CraftBukkit - added SpawnReason
                level.broadcastEntityEvent(this.animal, 18.toByte())
                if (level.gameRules.getBoolean(GameRules.RULE_DOMOBLOOT)) {
                    // CraftBukkit start - use event experience
                    if (experience > 0) {
                        level.addFreshEntity(
                            ExperienceOrb(
                                this.level,
                                animal.x,
                                animal.y,
                                animal.z,
                                experience,
                                org.bukkit.entity.ExperienceOrb.SpawnReason.BREED,
                                entityplayer,
                                entityfox
                            )
                        ) // Paper
                    }
                    // CraftBukkit end
                }
            }
        }
    }

    fun clearStates() {
        this.setIsInterested(false)
        this.setIsCrouching(false)
        this.setSitting(false)
        this.setDefending(false)
        this.setFaceplanted(false)
    }

    override fun finalizeSpawn(
        world: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        spawnReason: MobSpawnType,
        entityData: SpawnGroupData?
    ): SpawnGroupData? {
        val flag = false

        if (flag) {
            this.setAge(-24000)
        }

        if (world is ServerLevel) {
            this.setTargetGoals()
        }

        this.populateDefaultEquipmentSlots(world.random, difficulty)
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData as SpawnGroupData)
    }

//    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
//        super.defineSynchedData(builder)
//        builder.define(DATA_TYPE_ID, 0)
//        builder.define(DATA_FLAGS_ID, 0.toByte())
//    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        super.readAdditionalSaveData(nbt)

        this.setSleeping(nbt.getBoolean("Sleeping"))
        this.setSitting(nbt.getBoolean("Sitting"), false) // Paper - Add EntityToggleSitEvent
        this.setIsCrouching(nbt.getBoolean("Crouching"))
        if (level() is ServerLevel) {
            this.setTargetGoals()
        }
    }

    private fun setTargetGoals() {
        targetSelector.addGoal(6, NearestAttackableTargetGoal(
            this,
            Mob::class.java, 10, false, false
        ) { entityliving: LivingEntity? -> entityliving is Silverfish })
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, ChickenFloatGoal())
        goalSelector.addGoal(0, PoopGoal(this, 6000..12000))
        goalSelector.addGoal(1, FaceplantGoal())
        goalSelector.addGoal(2, ChickenPanicGoal(1.4))
        goalSelector.addGoal(3, ChickenBreedGoal(this, 1.0))
        goalSelector.addGoal(4, AvoidEntityGoal(
            this,
            Player::class.java, 16.0f, 1.6, 1.4
        ) { entityliving: LivingEntity ->
            Predicate<Entity> { entity: Entity ->
                !entity.isDiscrete && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(
                    entity
                )
            }.test(
                entityliving
            ) && !this.isDefending()
        })
        goalSelector.addGoal(4, AvoidEntityGoal(
            this,
            Wolf::class.java, 8.0f, 1.6, 1.4
        ) { entityliving -> !(entityliving as Wolf).isTame && !this.isDefending() })
        goalSelector.addGoal(4, AvoidEntityGoal(
            this,
            Fox::class.java, 8.0f, 1.6, 1.4
        ) { _ -> !this.isDefending() })
        goalSelector.addGoal(4, AvoidEntityGoal(
            this,
            PolarBear::class.java, 8.0f, 1.6, 1.4
        ) { _ -> !this.isDefending() })
        goalSelector.addGoal(5, StalkPreyGoal())
        goalSelector.addGoal(6, ChickenPounceGoal())
        goalSelector.addGoal(7, ChickenMeleeAttackGoal(1.2000000476837158, true))
        goalSelector.addGoal(
            3, TemptGoal(
                this, 1.0,
                { itemstack: ItemStack -> itemstack.`is`(ItemTags.CHICKEN_FOOD) }, false
            )
        )
        goalSelector.addGoal(8, ChickenFollowParentGoal(this, 1.25))
        goalSelector.addGoal(10, LeapAtTargetGoal(this, 0.4f))
        goalSelector.addGoal(11, WaterAvoidingRandomStrollGoal(this, 1.0))
        goalSelector.addGoal(11, ChickenSearchForItemsGoal())
        goalSelector.addGoal(
            12, ChickenLookAtPlayerGoal(
                this,
                Player::class.java, 24.0f
            )
        )
        goalSelector.addGoal(13, PerchAndSearchGoal())
//        goalSelector.addGoal(14, RandomLookAroundGoal(this))
//        targetSelector.addGoal(
//            3, DefendTrustedTargetGoal(
//                LivingEntity::class.java, false, false
//            ) { entityliving: LivingEntity ->
//                TRUSTED_TARGET_SELECTOR.test(entityliving)
//            }
//        )
    }

    override fun tick() {
        super.tick()
        if (this.isEffectiveAi) {
            val flag = this.isInWater

            if (flag || this.isSleeping) {
                this.setSitting(false)
            }

            if (this.isFaceplanted() && level().random.nextFloat() < 0.2f) {
                val blockposition = this.blockPosition()
                val iblockdata = level().getBlockState(blockposition)

                level().levelEvent(2001, blockposition, Block.getId(iblockdata))
            }
        }

        this.interestedAngleO = this.interestedAngle
        if (this.isInterested()) {
            this.interestedAngle += (1.0f - this.interestedAngle) * 0.4f
        } else {
            this.interestedAngle += (0.0f - this.interestedAngle) * 0.4f
        }

        this.crouchAmountO = this.crouchAmount
        if (this.isCrouching) {
            this.crouchAmount += 0.2f
            if (this.crouchAmount > 3.0f) {
                this.crouchAmount = 3.0f
            }
        } else {
            this.crouchAmount = 0.0f
        }
    }

    fun canMove(): Boolean {
        return !this.isSleeping && !this.isSitting() && !this.isFaceplanted()
    }

    init {
        this.lookControl = ChickenLookControl()
        this.setPathfindingMalus(PathType.DANGER_OTHER, 0.0f)
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 0.0f)
//        this.setCanPickUpLoot(true)
    }

    fun spawn(loc: Location) {
        this.setPosRaw(loc.x, loc.y, loc.z)

        this.getBukkitEntity().persistentDataContainer.set(KEY, PersistentDataType.BOOLEAN, true)

        this.persist = true
        (loc.world as CraftWorld).handle.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        this.setSleeping(false)
        this.setSitting(false, false) // Paper - Add EntityToggleSitEvent
        this.setIsCrouching(false)

        setTargetGoals()
    }

    override fun getAttributes(): AttributeMap {
        return AttributeMap(createAttributes().build())
    }

    companion object {
        val KEY: NamespacedKey = NamespacedKey(Hardcraft.instance, "CustomEntity")

        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 4.0).add(
                    Attributes.FOLLOW_RANGE, 32.0
                ).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.SAFE_FALL_DISTANCE, 5.0)
        }

        fun clearStates(chicken: Animal?) {
            if (chicken is CustomChicken) {
                chicken.clearStates()
            }
        }
    }
}