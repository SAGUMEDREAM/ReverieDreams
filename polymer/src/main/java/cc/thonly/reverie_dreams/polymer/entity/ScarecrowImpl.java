package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.polymer.entity.inf.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.inf.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import cc.thonly.reverie_dreams.polymer.entity.bil.OverlayEntityHolder;
import cc.thonly.reverie_dreams.polymer.entity.bil.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.entity.Scarecrow;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;

public class ScarecrowImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private final Scarecrow entity;
    private OverlayEntityHolder<Scarecrow, ScarecrowImpl> holder;

    public ScarecrowImpl(Scarecrow entity) {
        this.entity = entity;
        PolymerEntityHelper.addEntityHolderModel(this);
    }

    @Override
    public void onCreated() {
        this.holder = new OverlayLivingEntityHolder<>(this.entity, this, PolymerEntityHelper.SCARECROW_MODEL);
        TickHolderEntity.addTickHolder(this);
        TickHolderEntity.addElementBind(this.entity, this.holder);
        EntityAttachment.ofTicking(this.holder, this.entity);
    }
    @Override
    public void onTick() {
        if (this.holder == null) {
            return;
        }
        if (this.entity.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this.entity, this.holder);
            AnimationHelper.updateHurtVariant(this.entity, this.holder);
        }
    }
    @Override
    public LivingEntity getSource() {
        return this.entity;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }
}
