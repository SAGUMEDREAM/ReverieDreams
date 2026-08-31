package cc.thonly.reverie_dreams.entity.ai.goal;


import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("resource")
public class NPCHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    private Class<?> @Nullable [] toIgnoreAlert;

    public NPCHurtByTargetGoal(BaseNPCLikeEntity mob, Class<?>... ignoreDamageFromTheseTypes) {
        super(mob, true);
        this.toIgnoreDamage = ignoreDamageFromTheseTypes;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        int timestamp = this.mob.getLastHurtByMobTimestamp();
        LivingEntity lastHurtByMob = this.mob.getLastHurtByMob();
        if (timestamp != this.timestamp && lastHurtByMob != null) {
            if (this.mob instanceof BaseNPCLikeEntity self && lastHurtByMob instanceof BaseNPCLikeEntity other) {
                if (self.isTame() && other.isTame()
                        && self.getOwner() != null
                        && self.getOwnerUuid() != null && other.getOwnerUuid() != null
                        && (self.getOwner() == other.getOwner() || Objects.equals(self.getOwnerUuid(), other.getOwnerUuid()))) {
                    return false; // 同主人不反击
                }
            }
            if (lastHurtByMob.is(EntityType.PLAYER) && getServerLevel(this.mob).getGameRules().get(GameRules.UNIVERSAL_ANGER)) {
                return false;
            } else {
                for(Class<?> ignoreClass : this.toIgnoreDamage) {
                    if (ignoreClass.isAssignableFrom(lastHurtByMob.getClass())) {
                        return false;
                    }
                }

                return this.canAttack(lastHurtByMob, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public NPCHurtByTargetGoal setAlertOthers(Class<?>... exceptTheseTypes) {
        this.alertSameType = true;
        this.toIgnoreAlert = exceptTheseTypes;
        return this;
    }

    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }

        super.start();
    }

    protected void alertOthers() {
        double within = this.getFollowDistance();
        AABB searchAabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(within, 10.0F, within);
        List<? extends Mob> nearby = this.mob.level().getEntitiesOfClass(this.mob.getClass(), searchAabb, EntitySelector.NO_SPECTATORS);
        Iterator<? extends Mob> var5 = nearby.iterator();

        while(true) {
            Mob other;
            while(true) {
                if (!var5.hasNext()) {
                    return;
                }

                other = (Mob)var5.next();
                if (this.mob != other && other.getTarget() == null && (!(this.mob instanceof TamableAnimal) || ((TamableAnimal)this.mob).getOwner() == ((TamableAnimal)other).getOwner()) && !other.isAlliedTo(this.mob.getLastHurtByMob())) {
                    if (this.toIgnoreAlert == null) {
                        break;
                    }

                    boolean ignore = false;

                    for(Class<?> ignoreClass : this.toIgnoreAlert) {
                        if (other.getClass() == ignoreClass) {
                            ignore = true;
                            break;
                        }
                    }

                    if (!ignore) {
                        break;
                    }
                }
            }

            this.alertOther(other, this.mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob other, LivingEntity hurtByMob) {
        other.setTarget(hurtByMob);
    }
}
