package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.NPCFishingHook;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

@SuppressWarnings({"resource", "SpellCheckingInspection", "SuspiciousNameCombination"})
public class NPCFishingGoal extends Goal {

    private final NPCSimpleEntity npc;
    private final NPCWorkMode workMode;

    private BlockPos currentTarget;
    private BlockPos initialPosition;

    private static final int WATER_SEARCH_RADIUS = 2;
    private static final double CAST_DISTANCE_SQR = 9.0D;

    private int castCooldown = 0;

    public NPCFishingGoal(
            NPCSimpleEntity npc
    ) {
        this.npc = npc;
        this.workMode = NPCWorkModes.FISHING;

        this.setFlags(EnumSet.of(
                Flag.MOVE,
                Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        return EntityTargetUtil.isThisWorkMode(
                this.npc,
                this.workMode
        );
    }

    @Override
    public boolean canContinueToUse() {
        return EntityTargetUtil.isThisWorkMode(
                this.npc,
                this.workMode
        );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.initialPosition = this.npc.blockPosition();
        this.currentTarget = null;
        this.castCooldown = 0;
    }

    @Override
    public void tick() {
        // 工作模式变化
        if (!EntityTargetUtil.isThisWorkMode(
                this.npc,
                this.workMode
        )) {
            this.stopFishing();

            this.currentTarget = null;
            this.initialPosition = null;
            this.castCooldown = 0;

            return;
        }

        // 冷却
        if (this.castCooldown > 0) {
            this.castCooldown--;
        }

        // 检查鱼钩
        NPCFishingHook hook = this.npc.fishing;

        if (hook != null) {
            if (!hook.isRemoved()) {
                this.tickFishing(hook);
                return;
            }

            // 鱼钩已经被删除
            this.npc.fishing = null;

            // 防止下一 tick 立即重新抛竿
            this.castCooldown = 20;
        }

        // 还在抛竿冷却
        if (this.castCooldown > 0) {
            return;
        }

        // 寻找附近水
        this.currentTarget = this.findNearbyWater();

        // 找不到水就回初始位置
        if (this.currentTarget == null) {
            if (this.initialPosition != null
                    && !this.isNearInitialPosition()) {

                this.npc.getNavigation().moveTo(
                        this.initialPosition.getX() + 0.5D,
                        this.initialPosition.getY(),
                        this.initialPosition.getZ() + 0.5D,
                        1.0D
                );

                this.lookAt(this.initialPosition);
            } else {
                this.npc.getNavigation().stop();
            }

            return;
        }

        // 不在抛竿范围内
        if (!this.canCastTo(this.currentTarget)) {
            BlockPos standPos = this.findStandPosition(this.currentTarget);

            if (standPos != null && !this.isReached(standPos)) {
                this.npc.getNavigation().moveTo(
                        standPos.getX() + 0.5D,
                        standPos.getY(),
                        standPos.getZ() + 0.5D,
                        1.0D
                );

                this.lookAt(this.currentTarget);
                return;
            }
        }

        // 可以抛竿
        this.npc.getNavigation().stop();

        this.lookAt(this.currentTarget);

        if (this.npc.fishing == null) {
            this.castRod(this.currentTarget);
        }
    }

    private void tickFishing(NPCFishingHook hook) {
        // 看向鱼漂
        this.npc.getLookControl().setLookAt(
                hook.getX(),
                hook.getY(),
                hook.getZ()
        );

        // 鱼咬钩
        if (hook.isBiting()) {
            ItemStack rod = this.getFishingRod();
            if (!rod.isEmpty()) {
                int damage = hook.retrieve(rod);

                if (damage > 0) {
                    this.damageFishingRod(
                            rod,
                            damage
                    );
                }
            } else {
                hook.discard();
            }

            this.npc.fishing = null;

            // 回收后等待一段时间再抛
            this.castCooldown = 20;

            SoundEventPlayUtils.playSound(
                    this.npc,
                    SoundEvents.FISHING_BOBBER_RETRIEVE,
                    SoundSource.PLAYERS
            );
        }
    }

    private BlockPos findNearbyWater() {
        BlockPos origin = this.npc.blockPosition();

        BlockPos.MutableBlockPos mutable =
                new BlockPos.MutableBlockPos();

        BlockPos closest = null;

        double closestDistance = Double.MAX_VALUE;

        for (int x = -WATER_SEARCH_RADIUS;
             x <= WATER_SEARCH_RADIUS;
             x++) {

            for (int y = -1;
                 y <= 1;
                 y++) {

                for (int z = -WATER_SEARCH_RADIUS;
                     z <= WATER_SEARCH_RADIUS;
                     z++) {

                    mutable.set(
                            origin.getX() + x,
                            origin.getY() + y,
                            origin.getZ() + z
                    );

                    if (!this.npc.level()
                                 .getFluidState(mutable)
                                 .is(FluidTags.WATER)) {
                        continue;
                    }

                    if (!this.npc.level()
                                 .getFluidState(mutable)
                                 .isSource()) {
                        continue;
                    }

                    BlockPos above = mutable.above();

                    if (!this.npc.level()
                                 .getBlockState(above)
                                 .getCollisionShape(
                                         this.npc.level(),
                                         above
                                 )
                                 .isEmpty()) {
                        continue;
                    }

                    double distance =
                            mutable.distToCenterSqr(
                                    this.npc.position()
                            );

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closest = mutable.immutable();
                    }
                }
            }
        }

        return closest;
    }

    private boolean canCastTo(BlockPos water) {
        Vec3 waterCenter = Vec3.atCenterOf(water);

        double distance =
                this.npc.position().distanceToSqr(
                        waterCenter
                );

        return distance <= CAST_DISTANCE_SQR;
    }

    private BlockPos findStandPosition(BlockPos water) {
        BlockPos.MutableBlockPos mutable =
                new BlockPos.MutableBlockPos();

        BlockPos origin =
                this.npc.blockPosition();

        BlockPos closest = null;

        double closestDistance =
                Double.MAX_VALUE;

        for (Direction direction :
                Direction.Plane.HORIZONTAL) {

            mutable.set(
                    water.relative(direction)
            );

            if (!this.isStandable(mutable)) {
                continue;
            }

            if (this.npc.level()
                        .getFluidState(mutable)
                        .is(FluidTags.WATER)) {
                continue;
            }

            double distance =
                    mutable.distToCenterSqr(
                            new Vec3(
                                    origin.getX(),
                                    origin.getY(),
                                    origin.getZ()
                            )
                    );

            if (distance < closestDistance) {
                closestDistance = distance;
                closest = mutable.immutable();
            }
        }

        return closest;
    }

    private boolean isStandable(BlockPos pos) {
        BlockPos feet = pos;
        BlockPos head = pos.above();

        if (!this.npc.level()
                     .getBlockState(feet)
                     .getCollisionShape(
                             this.npc.level(),
                             feet
                     )
                     .isEmpty()) {
            return false;
        }

        if (!this.npc.level()
                     .getBlockState(head)
                     .getCollisionShape(
                             this.npc.level(),
                             head
                     )
                     .isEmpty()) {
            return false;
        }

        BlockPos ground = pos.below();

        return !this.npc.level()
                        .getBlockState(ground)
                        .getCollisionShape(
                                this.npc.level(),
                                ground
                        )
                        .isEmpty();
    }

    private void castRod(BlockPos water) {
        if (this.npc.fishing != null) {
            return;
        }

        if (this.castCooldown > 0) {
            return;
        }

        ItemStack rod = this.getFishingRod();

        if (rod.isEmpty()) {
            return;
        }

        InteractionHand hand;

        if (this.npc.getItemBySlot(
                EquipmentSlot.MAINHAND
        ).is(item -> item.value() instanceof FishingRodItem)) {

            hand = InteractionHand.MAIN_HAND;

        } else if (this.npc.getItemBySlot(
                EquipmentSlot.OFFHAND
        ).is(item -> item.value() instanceof FishingRodItem)) {

            hand = InteractionHand.OFF_HAND;

        } else {
            return;
        }

        this.lookAt(water);

        NPCFishingHook hook = new NPCFishingHook(this.npc, this.npc.level(), 0, 0);

        Vec3 start = hook.position();
        Vec3 target = Vec3.atCenterOf(water).add(0.0D, 0.4D, 0.0D);
        Vec3 direction = target.subtract(start);

        if (direction.lengthSqr() < 0.0001D) {
            hook.discard();
            return;
        }

        direction = direction.normalize();

        hook.setDeltaMovement(
                direction.scale(0.8D)
        );

        hook.setYRot(
                (float) (
                        Mth.atan2(
                                direction.x,
                                direction.z
                        ) * 180.0D / Math.PI
                )
        );

        hook.setXRot(
                (float) (
                        Mth.atan2(
                                direction.y,
                                direction.horizontalDistance()
                        ) * 180.0D / Math.PI
                )
        );

        hook.yRotO = hook.getYRot();
        hook.xRotO = hook.getXRot();

        this.npc.level().addFreshEntity(hook);

        this.npc.fishing = hook;

        this.npc.swing(hand);

        SoundEventPlayUtils.playSound(
                this.npc,
                SoundEvents.FISHING_BOBBER_THROW,
                SoundSource.PLAYERS
        );

        // 防止抛竿状态还没同步就再次执行
        this.castCooldown = 10;
    }

    private boolean isReached(BlockPos pos) {
        return pos.distToCenterSqr(
                this.npc.position()
        ) <= 2.25D;
    }

    private boolean isNearInitialPosition() {
        if (this.initialPosition == null) {
            return true;
        }

        return this.initialPosition
                .distToCenterSqr(
                        this.npc.position()
                ) <= 2.25D;
    }

    private void lookAt(BlockPos pos) {
        this.npc.getLookControl().setLookAt(
                pos.getX() + 0.5D,
                pos.getY() + 0.78D,
                pos.getZ() + 0.5D
        );

        float headYaw =
                this.npc.getYHeadRot();

        this.npc.yBodyRot =
                approachAngle(
                        this.npc.yBodyRot,
                        headYaw,
                        10.0F
                );
    }

    private static float approachAngle(
            float current,
            float target,
            float maxChange
    ) {
        float delta =
                Mth.wrapDegrees(
                        target - current
                );

        if (delta > maxChange) {
            delta = maxChange;
        }

        if (delta < -maxChange) {
            delta = -maxChange;
        }

        return current + delta;
    }

    private ItemStack getFishingRod() {
        ItemStack mainHand = this.npc.getItemBySlot(EquipmentSlot.MAINHAND);
        if (mainHand.is(item -> item.value() instanceof FishingRodItem)) {
            return mainHand;
        }

        ItemStack offHand = this.npc.getItemBySlot(EquipmentSlot.OFFHAND);
        if (offHand.is(item -> item.value() instanceof FishingRodItem)) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private void damageFishingRod(
            ItemStack rod,
            int amount
    ) {
        if (rod.isEmpty() || amount <= 0) {
            return;
        }

        if (this.npc.level().isClientSide()) {
            return;
        }

        if (this.npc.getItemBySlot(
                EquipmentSlot.MAINHAND
        ) == rod) {

            rod.hurtAndBreak(
                    amount,
                    this.npc,
                    EquipmentSlot.MAINHAND
            );

        } else if (this.npc.getItemBySlot(
                EquipmentSlot.OFFHAND
        ) == rod) {

            rod.hurtAndBreak(
                    amount,
                    this.npc,
                    EquipmentSlot.OFFHAND
            );
        }
    }

    private void stopFishing() {
        NPCFishingHook hook = this.npc.fishing;

        if (hook != null && !hook.isRemoved()) {
            hook.discard();
        }

        this.npc.fishing = null;

        this.castCooldown = 0;
    }
}