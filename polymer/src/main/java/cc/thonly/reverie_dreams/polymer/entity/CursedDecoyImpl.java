package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.CursedDecoyEntity;
import cc.thonly.reverie_dreams.polymer.entity.holder.CursedDecoyHolder;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;

/**
 * Polymer fallback for the cursed decoy: vanilla clients only receive a marker
 * entity, while the doll model is rendered through a virtual item display.
 */
public record CursedDecoyImpl(CursedDecoyEntity source) implements PolymerEntity, PolymerHolderEntity {
    public CursedDecoyImpl {
        if (!source.level().isClientSide()) {
            PolymerEntityHelper.addEntityHolderModel(this, source);
        }
    }

    @Override
    public void onCreated() {
        ItemDisplayElement element = new ItemDisplayElement();
        CursedDecoyHolder holder = new CursedDecoyHolder(this.source, element);
        holder.addElement(element);
        EntityAttachment.ofTicking(holder, this.source);
        VirtualEntityUtils.addVirtualPassenger(this.source, element.getEntityId());
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.MARKER;
    }
}