package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.EnumSet;

public class NPCCrossbowAttackGoal extends Goal {
	public static final UniformIntProvider COOLDOWN_RANGE = TimeHelper.betweenSeconds(1, 2);
	private NPCCrossbowAttackGoal.Stage stage = NPCCrossbowAttackGoal.Stage.UNCHARGED;
	private final NPCEntityImpl maid;
	private final double speed;
	private final float squaredRange;
	private int seeingTargetTicker;
	private int chargedTicksLeft;
	private int cooldown;

	public NPCCrossbowAttackGoal(NPCEntityImpl maid, double speed, float range) {
		this.maid = maid;

		this.speed = speed;
		this.squaredRange = range * range;
		this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
	}

	@Override
	public boolean canStart() {
		return this.hasAliveTarget() && this.isEntityHoldingCrossbow();
	}

	private boolean isEntityHoldingCrossbow() {
		return this.maid.isHolding(Items.CROSSBOW);
	}

	@Override
	public boolean shouldContinue() {
		return this.hasAliveTarget() && (this.canStart() || !this.maid.getNavigation().isIdle()) && this.isEntityHoldingCrossbow();
	}

	private boolean hasAliveTarget() {
		return this.maid.getTarget() != null && this.maid.getTarget().isAlive();
	}

	@Override
	public void stop() {
		super.stop();
		this.maid.setAttacking(false);
		this.maid.setTarget(null);
		this.seeingTargetTicker = 0;
		if (this.maid.isUsingItem()) {
			this.maid.clearActiveItem();
			this.maid.getActiveItem().set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
		}
	}

	@Override
	public boolean shouldRunEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity livingEntity = this.maid.getTarget();
		if (livingEntity != null) {
			boolean bl = this.maid.getVisibilityCache().canSee(livingEntity);
			boolean bl2 = this.seeingTargetTicker > 0;
			if (bl != bl2) {
				this.seeingTargetTicker = 0;
			}

			if (bl) {
				this.seeingTargetTicker++;
			} else {
				this.seeingTargetTicker--;
			}

			double d = this.maid.squaredDistanceTo(livingEntity);
			boolean bl3 = (d > this.squaredRange || this.seeingTargetTicker < 5) && this.chargedTicksLeft == 0;
			if (bl3) {
				this.cooldown--;
				if (this.cooldown <= 0) {
					this.maid.getNavigation().startMovingTo(livingEntity, this.isUncharged() ? this.speed : this.speed * 0.5);
					this.cooldown = COOLDOWN_RANGE.get(this.maid.getRandom());
				}
			} else {
				this.cooldown = 0;
				this.maid.getNavigation().stop();
			}

			this.maid.getLookControl().lookAt(livingEntity, 30.0F, 30.0F);
			if (this.stage == NPCCrossbowAttackGoal.Stage.UNCHARGED) {
				if (!bl3) {
					this.maid.setCurrentHand(ProjectileUtil.getHandPossiblyHolding(this.maid, Items.CROSSBOW));
					this.stage = NPCCrossbowAttackGoal.Stage.CHARGING;
				}
			} else if (this.stage == NPCCrossbowAttackGoal.Stage.CHARGING) {
				if (!this.maid.isUsingItem()) {
					this.stage = NPCCrossbowAttackGoal.Stage.UNCHARGED;
				}

				int i = this.maid.getItemUseTime();
				ItemStack itemStack = this.maid.getActiveItem();


//				Integer slot = inventory.findSlot(stack -> NPCEntityImpl.ARROW_ITEMS.contains(stack.getItem()));
//				if (slot!=null){
//					ItemStack stack = inventory.getStack(slot);
//					stack.decrement(1);
//				}else return;
				if (i >= CrossbowItem.getPullTime(itemStack, this.maid)) {
					this.maid.stopUsingItem();
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
				this.maid.shootAt(livingEntity, 1.0F);
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
		Hand crossBowHand = ProjectileUtil.getHandPossiblyHolding(this.maid, Items.CROSSBOW);
		ItemStack crossBow = maid.getStackInHand(crossBowHand);
		ItemStack arrowStack = RangedAttackUtil.getCrossBowAmmoStack(this.maid);
		if (arrowStack!=null&&RangedAttackUtil.loadProjectiles(crossBow, arrowStack, maid)){
//			arrowStack.decrement(1);
			return true;
		}else return false;

	}
}
