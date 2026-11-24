package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import cc.thonly.reverie_dreams.polymer.entity.holder.OverlayEntityHolder;
import cc.thonly.reverie_dreams.polymer.entity.holder.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.entity.Hairball;
import cc.thonly.reverie_dreams.entity.variant.HairballAnimationState;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;
import java.util.Objects;

public class HairballImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private final Hairball entity;
    private OverlayEntityHolder<Hairball, HairballImpl> holder;
    private String animation;
    private int revision = -1;
    private double walkFrame;

    public HairballImpl(Hairball entity) {
        this.entity = entity;
        PolymerEntityHelper.addEntityHolderModel(this, entity);
    }

    @Override
    public void onCreated() {
        this.holder = new OverlayLivingEntityHolder<>(this.entity, this, this.entity.isBlackColor() ? PolymerEntityHelper.BLACK_HAIRBALL_MODEL : PolymerEntityHelper.HAIRBALL_MODEL);
        TickHolderEntity.addTickHolder(this);
        TickHolderEntity.addElementBind(this.entity, this.holder);
        EntityAttachment.ofTicking(this.holder, this.entity);
        this.updateAnimation();
    }

    @Override
    public void onTick() {
        if (this.holder == null) {
            return;
        }
        this.updateAnimation();
        AnimationHelper.updateHurtVariant(this.entity, this.holder);
    }

    private void updateAnimation() {
        var actions = this.entity.getAnimationState();
        String next = this.entity.isDeadOrDying() ? null : actions.clip().animation();
        boolean restart = !actions.clip().looping() && this.revision != actions.revision();
        this.revision = actions.revision();
        if (!Objects.equals(next, this.animation) || restart) {
            if (this.animation != null) this.holder.getAnimator().stopAnimation(this.animation);
            if (next != null) this.holder.getAnimator().playAnimation(next);
            this.animation = next;
            this.walkFrame = 0;
        }
        if ("walk".equals(this.animation)) {
            this.holder.getAnimator().setAnimationFrame(this.animation, (int) this.walkFrame);
            this.walkFrame = (this.walkFrame + HairballAnimationState.walkSpeed(
                    this.entity.getDeltaMovement().horizontalDistance())) % 12;
        }
    }

    @Override
    public Hairball getSource() {
        return this.entity;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.BLOCK_DISPLAY;
    }
}
