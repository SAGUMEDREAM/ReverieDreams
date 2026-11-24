package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.Oni;
import cc.thonly.reverie_dreams.entity.variant.OniAppearance;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import cc.thonly.reverie_dreams.polymer.entity.holder.EquippedHumanoidHolder;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public final class OniImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private final Oni source;
    private EquippedHumanoidHolder<Oni, OniImpl> holder;
    private OniAppearance appearance;
    private String animation;
    private int attackRevision = -1;
    private int previousHurtTime;
    private double walkFrame;

    public OniImpl(Oni source) {
        this.source = source;
        if (!source.level().isClientSide()) PolymerEntityHelper.addEntityHolderModel(this, source);
    }

    @Override public void onCreated() {
        if (this.source.isRemoved() || this.holder != null) return;
        this.createHolder();
        TickHolderEntity.addTickHolder(this);
        this.updateAnimation();
    }

    private void createHolder() {
        this.appearance = OniAppearance.of(this.source.getSkinType().getId());
        this.holder = new EquippedHumanoidHolder<>(this.source, this, PolymerEntityHelper.getOniModel(this.appearance));
        TickHolderEntity.addElementBind(this.source, this.holder);
        EntityAttachment.ofTicking(this.holder, this.source);
        this.animation = null;
    }

    @Override public void onTick() {
        if (this.holder == null) return;
        if (this.appearance != OniAppearance.of(this.source.getSkinType().getId())) {
            this.holder.destroy();
            this.createHolder();
        }
        this.updateAnimation();
        if ("animation.walk".equals(this.animation)) {
            this.walkFrame = (this.walkFrame + this.source.walkAnimationSpeed()) % Oni.WALK_CYCLE_TICKS;
            this.holder.getAnimator().setAnimationFrame(this.animation, (int) this.walkFrame);
        }
        AnimationHelper.updateHurtVariant(this.source, this.holder);
    }

    private void updateAnimation() {
        String next = "animation." + this.source.modelAnimation();
        boolean repeat = next.equals("animation.melee") && this.attackRevision != this.source.attackAnimationRevision()
                || next.equals("animation.hurt") && this.source.hurtTime > this.previousHurtTime;
        this.previousHurtTime = this.source.hurtTime;
        this.attackRevision = this.source.attackAnimationRevision();
        if (next.equals(this.animation) && !repeat) return;
        if (this.animation != null) this.holder.getAnimator().stopAnimation(this.animation);
        this.walkFrame = 0;
        this.holder.getAnimator().playAnimation(next);
        this.animation = next;
    }

    @Override public LivingEntity getSource() { return this.source; }
    @Override public AnimatedEntityHolder getHolder() { return this.holder; }
    @Override public EntityType<?> getPolymerEntityType(PacketContext context) { return EntityType.BLOCK_DISPLAY; }
}
