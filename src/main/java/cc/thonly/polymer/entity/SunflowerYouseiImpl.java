package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.entity.SunflowerYouseiEntity;
import cc.thonly.reverie_dreams.entity.holder.WingHolder;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;

public record SunflowerYouseiImpl(SunflowerYouseiEntity source) implements PlayerPolymerEntity {
    public SunflowerYouseiImpl {
        this.onCreated(source);
    }

    @Override
    public void onCreated(Entity entity) {
        PlayerPolymerEntity.super.onCreated(entity);
        var x = new ItemDisplayElement();
        var holder = new WingHolder(source);
        x.setItem(new ItemStack(ModEntityHolders.YOUSEI_WINGS));
        x.setInvisible(true);
        x.setTeleportDuration(3);
        x.setScale(new Vector3f(1.2f));
        holder.setElement(x);
        holder.addElement(x);
        EntityAttachment.ofTicking(holder, entity);
        VirtualEntityUtils.addVirtualPassenger(entity, x.getEntityId());
        ELEMENTS.put(entity, x);
    }

    @Override
    public Property getSkin() {
        return this.source.getSkin();
    }
}
