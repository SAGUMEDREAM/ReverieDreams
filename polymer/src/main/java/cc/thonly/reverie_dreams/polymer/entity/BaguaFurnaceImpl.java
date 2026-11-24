package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.polymer.entity.holder.BaguaFurnaceHolder;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;

/**
 * Polymer fallback for the Bagua Furnace turret.
 */
public record BaguaFurnaceImpl(BaguaFurnaceEntity source) implements PolymerEntity, PolymerHolderEntity {
    public BaguaFurnaceImpl {
        if (!source.level().isClientSide()) {
            PolymerEntityHelper.addEntityHolderModel(this, source);
        }
    }

    @Override
    public void onCreated() {
        BaguaFurnaceHolder holder = new BaguaFurnaceHolder(this.source);
        EntityAttachment.ofTicking(holder, this.source);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.MARKER;
    }
}
