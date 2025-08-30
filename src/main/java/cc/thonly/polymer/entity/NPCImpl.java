package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.LivingEntity;

public record NPCImpl(NPCEntityImpl npcEntity) implements PlayerPolymerEntity {

    public NPCImpl {
        this.onCreated();
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
