package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.polymer.entity.inf.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.inf.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import cc.thonly.reverie_dreams.polymer.entity.bil.OverlayEntityHolder;
import cc.thonly.reverie_dreams.polymer.entity.bil.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.entity.MushroomMonster;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;

public class MushroomMonsterImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private final MushroomMonster entity;
    private OverlayEntityHolder<MushroomMonster, MushroomMonsterImpl> holder;

    public MushroomMonsterImpl(MushroomMonster entity) {
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
    public MushroomMonster getSource() {
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
