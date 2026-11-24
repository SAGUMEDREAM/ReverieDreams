package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.mixin.accessor.LivingEntityAccessor;
import cc.thonly.reverie_dreams.server.dialog.NPCSpeech;
import cc.thonly.reverie_dreams.world.settlement.SettlementDutyGoal;
import cc.thonly.reverie_dreams.world.settlement.SettlementGroundNavigation;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.world.item.ShieldItem;

public final class WhiteWolfTengu extends Tengu {
    private Goal guardCombat;
    private boolean guardCombatInstalled;
    private UUID intruder;
    private int warningTicks;
    private long nextWarning;
    private long nextBlock;

    public WhiteWolfTengu(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.getInventory().setMainHand(Items.IRON_SWORD.getDefaultInstance());
        this.getInventory().setOffHand(RDItems.TENGU_SHIELD.createStack());
        this.guardCombat = new GuardCombat();
        this.updateAttackType();
    }

    @Override
    public boolean isCrow() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SettlementGroundNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(SettlementDutyGoal.PRIORITY, new SettlementDutyGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.dead) {
            SettlementPilotData.unassign(this);
        }
    }

    @Override
    protected void updateAttackType() {
        if (this.guardCombat == null) {
            super.updateAttackType();
            return;
        }
        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.bowAttackGoal);
        this.goalSelector.removeGoal(this.crossBowAttackGoal);
        this.goalSelector.removeGoal(this.danmakuItemGoal);
        // The base NPC refreshes equipment periodically. Do not stop an active windup on refresh.
        if (!this.guardCombatInstalled) {
            this.goalSelector.addGoal(3, this.guardCombat);
            this.guardCombatInstalled = true;
        }
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return this.getAttackBoundingBox(1.5 * this.getScale()).intersects(((LivingEntityAccessor) target).reverie_dreams$getHitbox());
    }

    @Override
    public void tick() {
        SettlementDutyGoal.tickSleepingResident(this);
        super.tick();
        if (this.level().isClientSide() || this.isDeadOrDying())
            return;
        long now = this.level().getGameTime();
        if (this.action() != 3 && this.isUsingItem())
            this.stopUsingItem();
        var target = this.getTarget();
        if (target instanceof Player && this.post != null && target.distanceToSqr(this.post.getX() + .5, this.post.getY(), this.post.getZ() + .5) > 32 * 32) {
            this.setTarget(null);
            if (this.isTame() || !SettlementPilotData.hasAssignment(this)) {
                this.getNavigation().moveTo(this.post.getX() + .5, this.post.getY(), this.post.getZ() + .5, .8);
            }
            target = null;
        }
        double targetDistance = target == null ? 0 : this.distanceToSqr(target);
        if (target != null && this.action() == 0 && target.isUsingItem() && target.getUseItem().getItem() instanceof ProjectileWeaponItem
                && targetDistance > 9 && targetDistance <= 64 && this.getSensing().hasLineOfSight(target) && now >= this.nextBlock) {
            if (this.getOffhandItem().getItem() instanceof ShieldItem) {
                this.beginAction(3);
                this.startUsingItem(InteractionHand.OFF_HAND);
                this.nextBlock = now + 60;
            }
        }
        if (this.isTame() || target != null || this.post == null)
            return;
        Player nearest = this.level().getNearestPlayer(this.post.getX() + .5, this.post.getY() + 1, this.post.getZ() + .5, 7, p -> p instanceof Player player && !player.isCreative() && !player.isSpectator() && player.isAlive() && !this.isAlliedTo(player));
        if (nearest == null) {
            this.intruder = null;
            this.warningTicks = 0;
            return;
        }
        if (!nearest.getUUID().equals(this.intruder)) {
            this.intruder = nearest.getUUID();
            this.warningTicks = 0;
            if (now >= this.nextWarning && nearest instanceof ServerPlayer listener) {
                NPCSpeech.sayTo(this, listener, Component.translatable("reverie_dreams.tengu.warning"));
                this.nextWarning = now + 200;
            }
        }
        if (++this.warningTicks >= 80 && this.getSensing().hasLineOfSight(nearest)) {
            this.setTarget(nearest);
            this.warningTicks = 0;
        }
    }

    private final class GuardCombat extends Goal {
        private static final int WINDUP_TICKS = 7;
        private static final int RECOVERY_TICKS = 5;
        private static final int ATTACK_INTERVAL = 24;
        private static final double CHASE_SPEED = 1.30;

        private long strikeAt = -1, nextAttack, recoverUntil;
        private LivingEntity strikeTarget;
        private int repathTicks;

        private GuardCombat() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            var self = WhiteWolfTengu.this;
            var target = self.getTarget();
            return !self.isDeadOrDying() && target != null && target.isAlive() && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.repathTicks = 0;
            WhiteWolfTengu.this.setAggressive(true);
        }

        @Override
        public void stop() {
            var self = WhiteWolfTengu.this;
            self.getNavigation().stop();
            this.strikeAt = -1;
            this.strikeTarget = null;
            this.recoverUntil = 0;
            self.stopUsingItem();
            if (self.action() == 1 || self.action() == 3)
                self.beginAction(0);
            self.setAggressive(false);
        }

        @Override
        public void tick() {
            var self = WhiteWolfTengu.this;
            var target = self.getTarget();
            if (target == null)
                return;
            long now = self.level().getGameTime();
            self.setAggressive(true);
            self.getLookControl().setLookAt(target, 30, 30);
            if (this.strikeAt >= 0 && now >= this.strikeAt) {
                if (target == this.strikeTarget && self.isWithinMeleeAttackRange(target) && self.getSensing().hasLineOfSight(target)) {
                    self.swing(InteractionHand.MAIN_HAND);
                    self.doHurtTarget((ServerLevel) self.level(), target);
                }
                this.strikeAt = -1;
                this.strikeTarget = null;
            }
            boolean inRange = self.isWithinMeleeAttackRange(target);
            boolean canReach = inRange && self.getSensing().hasLineOfSight(target);
            if (now >= this.nextAttack && canReach && self.action() != 3) {
                self.beginAction(1);
                this.strikeTarget = target;
                this.strikeAt = now + WINDUP_TICKS;
                this.recoverUntil = this.strikeAt + RECOVERY_TICKS;
                this.nextAttack = now + ATTACK_INTERVAL;
            }
            boolean recovering = this.strikeAt < 0 && now < this.recoverUntil;
            if (canReach || recovering || self.action() == 3) {
                self.getNavigation().stop();
                self.getMoveControl().setWantedPosition(self.getX(), self.getY(), self.getZ(), 0);
                self.setSpeed(0);
                this.repathTicks = 0;
            } else {
                this.pursue(self, target);
            }
        }

        private void pursue(WhiteWolfTengu self, LivingEntity target) {
            if (--this.repathTicks <= 0) {
                this.repathTicks = self.getNavigation().moveTo(target, CHASE_SPEED) ? 5 : 10;
            }
        }
    }
}
