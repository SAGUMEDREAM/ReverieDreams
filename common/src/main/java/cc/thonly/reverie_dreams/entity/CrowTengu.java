package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.entity.ai.goal.SmartFlyGoal;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.EnumSet;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;

public final class CrowTengu extends Tengu {
    private final Goal windCombat;
    private final TenguLocomotion.Flight flight = new TenguLocomotion.Flight();
    public CrowTengu(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.getInventory().setMainHand(RDItems.TENGU_CAMERA.createStack());
        this.windCombat = new WindCombat();
        this.updateAttackType();
        this.goalSelector.addGoal(3, this.windCombat);
        this.enableFairyFlight();
    }
    @Override public boolean isCrow(){return true;}
    @Override protected String locomotionAnimation() {
        String clip = this.flight.phase().clip;
        return clip == null ? super.locomotionAnimation() : clip;
    }
    @Override protected String photoAnimation() {
        return this.flight.airborne() ? "photo_air" : super.photoAnimation();
    }
    @Override protected int locomotionAnimationElapsed() {
        return this.flight.elapsed();
    }
    @Override protected void updateFlightAnimation(boolean grounded, boolean eligible, double horizontal, double vertical) {
        this.flight.update(grounded, eligible, horizontal, vertical);
    }
    @Override protected void registerGoals(){super.registerGoals();this.goalSelector.addGoal(8,new SmartFlyGoal(this,1));}
    @Override
    protected void updateAttackType() {
        if (this.windCombat == null) {
            super.updateAttackType();
            return;
        }
        // Equipment refreshes must not stop a running windup.
        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.bowAttackGoal);
        this.goalSelector.removeGoal(this.crossBowAttackGoal);
        this.goalSelector.removeGoal(this.danmakuItemGoal);
    }
    @Override public void tick(){
        super.tick();
        if(!this.level().isClientSide()&&!this.isDeadOrDying()&&this.getTarget()==null&&this.tickCount%180==0&&this.action()==0&&this.level().getNearestPlayer(this,6)!=null)this.beginAction(4);
    }
    private final class WindCombat extends Goal {
        private static final int WINDUP_TICKS = 7;
        private static final int SHOT_INTERVAL = 60;
        private static final int REPATH_INTERVAL = 8;
        private static final double START_RANGE_SQ = 12 * 12;
        private static final double RELEASE_RANGE_SQ = 16 * 16;
        private long nextShot, releaseAt = -1, repathAt;
        private LivingEntity shotTarget;

        private WindCombat() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            var self = CrowTengu.this;
            var target = self.getTarget();
            return !self.isDeadOrDying() && target != null && target.isAlive()
                    && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
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
            this.repathAt = 0;
        }

        @Override
        public void stop() {
            CrowTengu.this.getNavigation().stop();
            this.cancelShot();
        }

        @Override
        public void tick() {
            var self = CrowTengu.this;
            var target = self.getTarget();
            if (target == null) {
                return;
            }
            long now = self.level().getGameTime();
            double distanceSq = self.distanceToSqr(target);
            boolean visible = self.getSensing().hasLineOfSight(target);
            boolean inFiringPosition = distanceSq <= START_RANGE_SQ && visible;

            self.getLookControl().setLookAt(target, 30, 30);
            if (this.releaseAt >= 0 && this.shotTarget != target) {
                this.cancelShot();
            }
            if (this.releaseAt < 0 && now >= this.nextShot && inFiringPosition) {
                self.beginAction(2);
                this.shotTarget = target;
                this.releaseAt = now + WINDUP_TICKS;
                this.nextShot = now + SHOT_INTERVAL;
            }
            if (this.releaseAt >= 0 || inFiringPosition) {
                if (!self.getNavigation().isDone()) {
                    self.getNavigation().stop();
                }
                this.repathAt = 0;
                this.turnBodyToward(self, target);
            } else if (now >= this.repathAt) {
                self.getNavigation().moveTo(target.getX(), target.getY() + 1.2, target.getZ(), 1);
                this.repathAt = now + REPATH_INTERVAL;
            }
            if (this.releaseAt >= 0 && now >= this.releaseAt) {
                this.releaseAt = -1;
                this.shotTarget = null;
                if (visible && distanceSq <= RELEASE_RANGE_SQ) {
                    this.fire(self, target);
                }
            }
        }

        private void cancelShot() {
            var self = CrowTengu.this;
            this.releaseAt = -1;
            this.shotTarget = null;
            if (self.action() == 2) {
                self.beginAction(0);
            }
        }

        private void turnBodyToward(CrowTengu self, LivingEntity target) {
            double dx = target.getX() - self.getX();
            double dz = target.getZ() - self.getZ();
            if (dx * dx + dz * dz < 1.0E-4) {
                return;
            }
            float targetYaw = (float) Math.toDegrees(Mth.atan2(dz, dx)) - 90.0F;
            float yaw = Mth.approachDegrees(self.getYRot(), targetYaw, 26.0F);
            self.setYRot(yaw);
            self.setYBodyRot(yaw);
        }

        private void fire(CrowTengu self, LivingEntity target) {
            float[] aim = DanmakuShooter.getPitchYaw(self, target);
            var stack = DanmakuTypes.random(DanmakuTypes.ARROWHEAD).create();
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0xc7dfd8));
            stack.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(),
                    stack.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(),
                            DanmakuProperties.ofDefault()).withSpeedVersion(1).withSpeed(.75F));
            for (int spread = -10; spread <= 10; spread += 10) {
                DanmakuShooter.spawn((ServerLevel) self.level(), self, stack.copy(), aim[0], aim[1] + spread, .75F, 1, .4F);
            }
            DanmakuShooter.soundDefault(self);
        }
    }
}
