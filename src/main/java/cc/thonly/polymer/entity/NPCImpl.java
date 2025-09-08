package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.LivingEntity;

public record NPCImpl(NPCEntityImpl npcEntity) implements PlayerPolymerEntity {

    public NPCImpl {
        PolymerEntityHelper.NEXT.add(this);
    }

    @Override
    public void onCreated() {
        var x = new ItemDisplayElement();
        var holder = new WingHolder(this.source);
        x.setItem(new ItemStack(ModEntityHolders.YOUSEI_WINGS));
        x.setInvisible(true);
        x.setTeleportDuration(3);
        x.setScale(new Vector3f(1.2f));
        holder.setElement(x);
        holder.addElement(x);
        EntityAttachment.ofTicking(holder, this.source);
        VirtualEntityUtils.addVirtualPassenger(this.source, x.getEntityId());
        ELEMENTS.put(this.source, x);
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
