package cc.thonly.reverie_dreams.fabric.polymer.entity;

import cc.thonly.reverie_dreams.fabric.polymer.entity.inf.PlayerPolymerEntity;
import cc.thonly.reverie_dreams.fabric.polymer.helper.PolymerEntityHelper;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

public record NPCImpl(BaseNPCLikeEntity source) implements PlayerPolymerEntity {

    public NPCImpl {
        if (!source.level().isClientSide()) {
            PolymerEntityHelper.addEntityHolderModel(this);
        }

    }

    @Override
    public void onCreated() {
        var entity = this.getEntity();
        var x = new ItemDisplayElement();
        var holder = new ElementHolder();
        x.setInvisible(true);
        x.setTeleportDuration(3);
        x.setScale(new Vector3f(0.5f));
        holder.addElement(x);
        EntityAttachment.of(holder, entity);
        VirtualEntityUtils.addVirtualPassenger(entity, x.getEntityId());
        PolymerEntityHelper.POLYMER_PLAYER_ELEMENTS.put(entity, x);
    }

    @Override
    public LivingEntity getEntity() {
        return this.source;
    }

    @Override
    public Property getSkin() {
        return this.source.getSkin();
    }
}
