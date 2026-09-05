package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.UFO;
import cc.thonly.reverie_dreams.polymer.entity.inf.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.inf.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import cc.thonly.reverie_dreams.polymer.entity.bil.OverlayEntityHolder;
import cc.thonly.reverie_dreams.polymer.entity.bil.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;

public class UfoImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private final UFO source;
    private OverlayEntityHolder<UFO, UfoImpl> holder;

    public UfoImpl(UFO source) {
        this.source = source;
        if (!source.level().isClientSide()) {
            PolymerEntityHelper.addEntityHolderModel(this);
        }
    }

    @Override
    public void onCreated() {
        this.holder = new OverlayLivingEntityHolder<>(this.source, this, PolymerEntityHelper.UFO_MODEL);
        TickHolderEntity.addTickHolder(this);
        TickHolderEntity.addElementBind(this.source, this.holder);
        EntityAttachment.ofTicking(this.holder, this.source);
    }

    @Override
    public void onTick() {
        if (this.holder == null) {
            return;
        }
        if (this.source.tickCount % 2 == 0) {
            if (this.source.isCharging()) {
                AnimationHelper.updateAttackAnimation(this.source, this.holder);
            } else {
                AnimationHelper.updateWalkAnimation(this.source, this.holder);
            }
            AnimationHelper.updateHurtVariant(this.source, this.holder);
        }
    }

    @Override
    public LivingEntity getSource() {
        return this.source;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }
}
