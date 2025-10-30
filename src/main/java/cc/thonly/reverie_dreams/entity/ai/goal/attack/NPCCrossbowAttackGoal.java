package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import java.util.EnumSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

public class NPCCrossbowAttackGoal extends Goal {
	public static final UniformInt COOLDOWN_RANGE = TimeUtil.rangeOfSeconds(1, 2);
	private NPCCrossbowAttackGoal.Stage stage = NPCCrossbowAttackGoal.Stage.UNCHARGED;
	private final BaseNPCLikeEntity maid;
	private final double speed;
	private final float squaredRange;
	private int seeingTargetTicker;
	private int chargedTicksLeft;
	private int cooldown;

	public NPCCrossbowAttackGoal(BaseNPCLikeEntity maid, double speed, float range) {
		this.maid = maid;

		this.speed = speed;
		this.squaredRange = range * range;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return this.hasAliveTarget() && this.isEntityHoldingCrossbow();
	}

	private boolean isEntityHoldingCrossbow() {
		return this.maid.isHolding(Items.CROSSBOW);
	}

	@Override
	public boolean canContinueToUse() {
		return this.hasAliveTarget() && (this.canUse() || !this.maid.getNavigation().isDone()) && this.isEntityHoldingCrossbow();
	}

	private boolean hasAliveTarget() {
		return this.maid.getTarget() != null && this.maid.getTarget().isAlive();
	}

	@Override
	public void stop() {
		super.stop();
		this.maid.setAggressive(false);
		this.maid.setTarget(null);
		this.seeingTargetTicker = 0;
		if (this.maid.isUsingItem()) {
			this.maid.stopUsingItem();
			this.maid.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity livingEntity = this.maid.getTarget();
		if (livingEntity != null) {
			boolean bl = this.maid.getSensing().hasLineOfSight(livingEntity);
			boolean bl2 = this.seeingTargetTicker > 0;
			if (bl != bl2) {
				this.seeingTargetTicker = 0;
			}

			if (bl) {
				this.seeingTargetTicker++;
			} else {
				this.seeingTargetTicker--;
			}

			double d = this.maid.distanceToSqr(livingEntity);
			boolean bl3 = (d > this.squaredRange || this.seeingTargetTicker < 5) && this.chargedTicksLeft == 0;
			if (bl3) {
				this.cooldown--;
				if (this.cooldown <= 0) {
					this.maid.getNavigation().moveTo(livingEntity, this.isUncharged() ? this.speed : this.speed * 0.5);
					this.cooldown = COOLDOWN_RANGE.sample(this.maid.getRandom());
				}
			} else {
				this.cooldown = 0;
				this.maid.getNavigation().stop();
			}

			this.maid.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
			if (this.stage == NPCCrossbowAttackGoal.Stage.UNCHARGED) {
				if (!bl3) {
					this.maid.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.maid, Items.CROSSBOW));
					this.stage = NPCCrossbowAttackGoal.Stage.CHARGING;
				}
			} else if (this.stage == NPCCrossbowAttackGoal.Stage.CHARGING) {
				if (!this.maid.isUsingItem()) {
					this.stage = NPCCrossbowAttackGoal.Stage.UNCHARGED;
				}

				int i = this.maid.getTicksUsingItem();
				ItemStack itemStack = this.maid.getUseItem();


//				Integer slot = inventory.findSlot(stack -> NPCEntityImpl.ARROW_ITEMS.contains(stack.getItem()));
//				if (slot!=null){
//					ItemStack stack = inventory.getStack(slot);
//					stack.decrement(1);
//				}else return;
				if (i >= CrossbowItem.getChargeDuration(itemStack, this.maid)) {
					this.maid.releaseUsingItem();
					if (chargeCrossBow()){
						this.stage = NPCCrossbowAttackGoal.Stage.CHARGED;
						this.chargedTicksLeft = 10 + this.maid.getRandom().nextInt(20);
					}
				}
			} else if (this.stage == NPCCrossbowAttackGoal.Stage.CHARGED) {
				this.chargedTicksLeft--;
				if (this.chargedTicksLeft == 0) {
					this.stage = NPCCrossbowAttackGoal.Stage.READY_TO_ATTACK;
				}
			} else if (this.stage == NPCCrossbowAttackGoal.Stage.READY_TO_ATTACK && bl) {
				this.maid.performRangedAttack(livingEntity, 1.0F);
				this.stage = NPCCrossbowAttackGoal.Stage.UNCHARGED;
			}
		}
	}

	private boolean isUncharged() {
		return this.stage == NPCCrossbowAttackGoal.Stage.UNCHARGED;
	}

	static enum Stage {
		UNCHARGED,
		CHARGING,
		CHARGED,
		READY_TO_ATTACK;
	}
	private boolean chargeCrossBow(){
		InteractionHand crossBowHand = ProjectileUtil.getWeaponHoldingHand(this.maid, Items.CROSSBOW);
		ItemStack crossBow = maid.getItemInHand(crossBowHand);
		ItemStack arrowStack = RangedAttackUtil.getCrossBowAmmoStack(this.maid);
		if (arrowStack!=null&&RangedAttackUtil.loadProjectiles(crossBow, arrowStack, maid)){
//			arrowStack.decrement(1);
			return true;
		}else return false;

	}
}
