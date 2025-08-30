package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.npc.AbstractNPCEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntityImpl;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.LivingEntity;

public record RoleImpl(AbstractNPCEntity npcEntity) implements PlayerPolymerEntity{

    public RoleImpl {
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
