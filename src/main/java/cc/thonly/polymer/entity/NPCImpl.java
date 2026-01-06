package cc.thonly.polymer.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

public record NPCImpl(BaseNPCLikeEntity npcEntity) implements PlayerPolymerEntity {

    public NPCImpl {
        PolymerEntityHelper.addEntityHolderModel(this);
    }



    @Override
    public LivingEntity getEntity() {
        return this.npcEntity;
    }

    @Override
    public Property getSkin() {
        return this.npcEntity.getSkin();
    }
}
