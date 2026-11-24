package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import cc.thonly.reverie_dreams.entity.variant.WalkingLegs;
import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitAppearance;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import cc.thonly.reverie_dreams.polymer.entity.holder.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.polymer.entity.holder.YoukaiRabbitHolder;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;

import java.util.Objects;

public final class YoukaiRabbitImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private final YoukaiRabbit entity;
    private OverlayLivingEntityHolder<YoukaiRabbit, YoukaiRabbitImpl> holder;
    private boolean female;
    private int variant;
    private String animation;
    private int revision = -1;
    private double frame;
    private boolean walkingLegs;
    public YoukaiRabbitImpl(YoukaiRabbit entity) {
        this.entity = entity;
        if (!entity.level().isClientSide()) PolymerEntityHelper.addEntityHolderModel(this, entity);
    }
    private void createHolder() {
        this.female = this.entity.isFemale();
        this.variant = this.entity.getVariant();
        var model = PolymerEntityHelper.getYoukaiRabbitModel(YoukaiRabbitAppearance.byId(this.variant));
        this.holder = new YoukaiRabbitHolder(this.entity, this, model);
        TickHolderEntity.addElementBind(this.entity, this.holder); EntityAttachment.ofTicking(this.holder, this.entity);
        this.animation = null; this.revision = -1; this.frame = 0;
        this.walkingLegs = false;
    }
    @Override public void onCreated() {
        if (this.holder != null || this.entity.isRemoved()) return;
        this.createHolder(); TickHolderEntity.addTickHolder(this); this.onTick();
    }
    @Override public void onTick() {
        if (this.holder == null) return;
        if (this.female != this.entity.isFemale() || this.variant != this.entity.getVariant()) { this.holder.destroy(); this.createHolder(); }
        String next = this.entity.modelAnimation();
        boolean restart = next != null && !YoukaiRabbit.loopingAnimation(next) && this.revision != this.entity.animationRevision();
        if (!Objects.equals(this.animation, next) || restart) {
            if (this.animation != null) this.holder.getAnimator().stopAnimation(this.animation);
            if (next != null) this.holder.getAnimator().playAnimation(next);
            this.animation = next; this.frame = 0;
        }
        this.revision = this.entity.animationRevision();
        if ("walk".equals(this.animation)) {
            this.holder.getAnimator().setAnimationFrame(this.animation, (int)this.frame);
            this.frame = (this.frame + this.entity.walkAnimationSpeed()) % 20;
        }
        boolean legs = this.entity.hasWalkingLegAnimation();
        if (legs != this.walkingLegs) {
            if (legs) this.holder.getAnimator().playAnimation(WalkingLegs.ANIMATION, 10);
            else this.holder.getAnimator().stopAnimation(WalkingLegs.ANIMATION);
            this.walkingLegs = legs;
        }
        if (legs) this.holder.getAnimator().setAnimationFrame(WalkingLegs.ANIMATION, (int)(this.entity.walkingLegPhase(1) * 20));
        AnimationHelper.updateHurtVariant(this.entity, this.holder);
    }
    @Override public YoukaiRabbit getSource() { return this.entity; }
    @Override public AnimatedEntityHolder getHolder() { return this.holder; }
    @Override public EntityType<?> getPolymerEntityType(PacketContext context) { return EntityType.BLOCK_DISPLAY; }
}
