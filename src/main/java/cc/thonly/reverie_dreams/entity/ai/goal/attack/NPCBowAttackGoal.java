package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.EnumSet;

@Setter
@Getter
public class NPCBowAttackGoal<T extends TamableAnimal> extends Goal {
    private final T actor;
    private final double speed;
    private int attackInterval;
    private final float squaredRange;
    private int cooldown = -1;
    private int targetSeeingTicker;
    private boolean movingToLeft;
    private boolean backward;
    private int combatTicks = -1;
    public NPCBowAttackGoal(T actor, double speed, int attackInterval, float range) {
        this.actor = actor;
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.squaredRange = range * range;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if ((this.actor).getTarget() == null) {
            return false;
        }
        return this.isHoldingBow();
    }

    protected boolean isHoldingBow() {
        return this.actor.isHolding(Items.BOW);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.actor.getNavigation().isDone()) && this.isHoldingBow();
    }

    @Override
    public void start() {
        super.start();
        this.actor.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.actor.setAggressive(false);
        this.targetSeeingTicker = 0;
        this.cooldown = -1;
        this.actor.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        boolean bl2;
        LivingEntity livingEntity = this.actor.getTarget();
        if (livingEntity == null) {
            return;
        }
        double d = this.actor.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        boolean bl = this.actor.getSensing().hasLineOfSight(livingEntity);
        boolean bl3 = bl2 = this.targetSeeingTicker > 0;
        if (bl != bl2) {
            this.targetSeeingTicker = 0;
        }
        this.targetSeeingTicker = bl ? ++this.targetSeeingTicker : --this.targetSeeingTicker;
        if (d > (double)this.squaredRange || this.targetSeeingTicker < 20) {
            this.actor.getNavigation().moveTo(livingEntity, this.speed);
            this.combatTicks = -1;
        } else {
            this.actor.getNavigation().stop();
            ++this.combatTicks;
        }
        if (this.combatTicks >= 20) {
            if ((double)(this.actor).getRandom().nextFloat() < 0.3) {
                boolean bl4 = this.movingToLeft = !this.movingToLeft;
            }
            if ((double)(this.actor).getRandom().nextFloat() < 0.3) {
                this.backward = !this.backward;
            }
            this.combatTicks = 0;
        }
        if (this.combatTicks > -1) {
            if (d > (double)(this.squaredRange * 0.75f)) {
                this.backward = false;
            } else if (d < (double)(this.squaredRange * 0.25f)) {
                this.backward = true;
            }
            (this.actor).getMoveControl().strafe(this.backward ? -0.5f : 0.5f, this.movingToLeft ? 0.5f : -0.5f);
            Entity entity = (this.actor).getControlledVehicle();
            if (entity instanceof Mob) {
                Mob mobEntity = (Mob)entity;
                mobEntity.lookAt(livingEntity, 30.0f, 30.0f);
            }
            (this.actor).lookAt(livingEntity, 30.0f, 30.0f);
        } else {
            (this.actor).getLookControl().setLookAt(livingEntity, 30.0f, 30.0f);
        }
        if ((this.actor).isUsingItem()) {
            int i;
            if (!bl && this.targetSeeingTicker < -60) {
                (this.actor).stopUsingItem();
            } else if (bl && (i = (this.actor).getTicksUsingItem()) >= 20) {
                NPCInventoryImpl inventory = ((BaseNPCLikeEntity) this.actor).getInventory();
                ItemStack arrowStack = RangedAttackUtil.getArrowStack(((BaseNPCLikeEntity) this.actor));
                if (arrowStack==null){
                    return;
                }

//                if(inventory.containsAny(NPCEntityImpl.ARROW_ITEMS)) {
//
//                    for (int j = 0; j < inventory.size(); j++) {
//                        ItemStack stack = inventory.getStack(j);
//                        if(NPCEntityImpl.ARROW_ITEMS.contains(stack.getItem())) {
//                            stack.decrement(1);
//                            canNext = true;
//                            break;
//                        }
//                    }
//                }
//                if(!canNext) return;
                (this.actor).stopUsingItem();
                ((RangedAttackMob)this.actor).performRangedAttack(livingEntity, BowItem.getPowerForTime(i));
                this.cooldown = this.attackInterval;
            }
        } else if (--this.cooldown <= 0 && this.targetSeeingTicker >= -60) {
            (this.actor).startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.actor, Items.BOW));
        }
    }
}
