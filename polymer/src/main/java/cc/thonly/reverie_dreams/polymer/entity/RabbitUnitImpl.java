package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitAppearance;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitAction;
import cc.thonly.reverie_dreams.entity.variant.WalkingLegs;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import cc.thonly.reverie_dreams.polymer.entity.holder.EquippedHumanoidHolder;
import cc.thonly.reverie_dreams.polymer.entity.holder.RabbitUnitEquipmentHolder;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** The same authored skeleton and animation selection as the native GeckoLib renderer. */
public final class RabbitUnitImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private final RabbitUnit source;
    private EquippedHumanoidHolder<RabbitUnit, RabbitUnitImpl> holder;
    private RabbitUnitAppearance appearance;
    private RabbitUnitAction action;
    private String animation;
    private int revision = -1;
    private int previousHurt;
    private double walkFrame;
    private boolean walkingLegs;

    public RabbitUnitImpl(RabbitUnit source) {
        this.source = source;
        if (!source.level().isClientSide()) PolymerEntityHelper.addEntityHolderModel(this, source);
    }
    @Override public void onCreated() {
        if (this.source.isRemoved() || this.holder != null) return;
        this.createHolder();
        TickHolderEntity.addTickHolder(this);
        this.updateAnimation();
        this.updateFrame();
    }
    private void createHolder() {
        this.appearance = RabbitUnitAppearance.of(this.source.getSkinType().getId());
        this.holder = new RabbitUnitEquipmentHolder<>(this.source, this, PolymerEntityHelper.getRabbitUnitModel(this.appearance));
        TickHolderEntity.addElementBind(this.source, this.holder);
        EntityAttachment.ofTicking(this.holder, this.source);
        this.action = null;
        this.walkingLegs = false;
    }
    @Override public void onTick() {
        if (this.holder == null) return;
        if (this.appearance != RabbitUnitAppearance.of(this.source.getSkinType().getId())) {
            this.holder.destroy();
            this.createHolder();
        }
        this.updateAnimation();
        this.updateFrame();
        if (this.source.isDeadOrDying()) this.holder.clearColor();
        else AnimationHelper.updateHurtVariant(this.source, this.holder);
    }
    private void updateFrame() {
        boolean legs = this.source.hasWalkingLegAnimation();
        if (legs != this.walkingLegs) {
            if (legs) this.holder.getAnimator().playAnimation(WalkingLegs.ANIMATION, 10);
            else this.holder.getAnimator().stopAnimation(WalkingLegs.ANIMATION);
            this.walkingLegs = legs;
        }
        if (legs) this.holder.getAnimator().setAnimationFrame(WalkingLegs.ANIMATION, (int) (this.source.walkingLegPhase(1) * 20));
        if (this.action == RabbitUnitAction.EVACUATE) {
            this.holder.getAnimator().setAnimationFrame(this.animation, this.source.evacuationTicks());
        } else if (this.action == RabbitUnitAction.RELOAD) {
            this.holder.getAnimator().setAnimationFrame(this.animation, (int) (this.source.reloadProgress() * 20));
        } else if (!this.action.looping() && this.action != RabbitUnitAction.HURT) {
            this.holder.getAnimator().setAnimationFrame(this.animation, this.source.combatActionElapsedTicks());
        }
        if (this.action == RabbitUnitAction.WALK || this.action == RabbitUnitAction.GUN_WALK) {
            this.walkFrame = (this.walkFrame + this.source.walkAnimationSpeed()) % 20;
            this.holder.getAnimator().setAnimationFrame(this.animation, (int) this.walkFrame);
        }
    }
    private void updateAnimation() {
        var next = this.source.modelAction();
        var nextAnimation = this.source.modelAnimationName();
        boolean repeat = !next.looping() && this.revision != this.source.actionRevision()
                || next == RabbitUnitAction.HURT && this.source.hurtTime > this.previousHurt;
        this.revision = this.source.actionRevision();
        this.previousHurt = this.source.hurtTime;
        if (next == this.action && nextAnimation.equals(this.animation) && !repeat) return;
        if (this.animation != null) this.holder.getAnimator().stopAnimation(this.animation);
        this.walkFrame = 0;
        this.holder.getAnimator().playAnimation(nextAnimation);
        this.action = next;
        this.animation = nextAnimation;
    }
    @Override public LivingEntity getSource() { return this.source; }
    @Override public AnimatedEntityHolder getHolder() { return this.holder; }
    @Override public EntityType<?> getPolymerEntityType(PacketContext context) { return EntityType.BLOCK_DISPLAY; }
}
