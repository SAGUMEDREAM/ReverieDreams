package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.GapPortalEntity;
import cc.thonly.reverie_dreams.polymer.entity.holder.GapPortalHolder;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Polymer fallback for the server-side entity: vanilla clients get a rotating
 * ItemDisplay using the dedicated portal model, plus the normal server-side
 * particles and sounds.
 */
public record GapPortalImpl(GapPortalEntity source) implements PolymerEntity, PolymerHolderEntity {
    public GapPortalImpl {
        if (!source.level().isClientSide()) {
            PolymerEntityHelper.addEntityHolderModel(this, source);
        }
    }

    @Override
    public void onCreated() {
        var element = new ItemDisplayElement();
        var holder = new GapPortalHolder(this.source, element);
        holder.addElement(element);
        EntityAttachment.ofTicking(holder, this.source);
    }

    @Override
    public void onTrackingStopped(ServerPlayer player) {
        // EntityAttachment owns the holder's lifetime. Nothing else to release.
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        // The actual visual is a virtual item display; the source entity itself
        // can be invisible to clients without the mod.
        return EntityType.MARKER;
    }
}
