package cc.thonly.polymer.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.polymer.entity.bil.OverlayLivingEntityHolder;
import cc.thonly.polymer.entity.bil.OverlayEntityHolder;
import cc.thonly.reverie_dreams.entity.MushroomMonsterEntity;
import cc.thonly.reverie_dreams.util.entity.AnimationHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;

public class MushroomMonsterImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private final MushroomMonsterEntity entity;
    private OverlayEntityHolder<MushroomMonsterEntity, MushroomMonsterImpl> holder;

    public MushroomMonsterImpl(MushroomMonsterEntity entity) {
        this.entity = entity;
        PolymerEntityHelper.addEntityHolderModel(this);
    }

    @Override
    public void onCreated() {
        this.holder = new OverlayLivingEntityHolder<>(this.entity, this, PolymerEntityHelper.MUSHROOM_MONSTER_MODEL);
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
    public MushroomMonsterEntity getEntity() {
        return this.entity;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }
}
