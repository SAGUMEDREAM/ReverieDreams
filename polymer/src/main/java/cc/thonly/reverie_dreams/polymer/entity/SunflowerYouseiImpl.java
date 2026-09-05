package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.polymer.entity.inf.PlayerPolymerEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import cc.thonly.reverie_dreams.entity.SunflowerYousei;
import cc.thonly.reverie_dreams.polymer.entity.holder.WingHolder;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public record SunflowerYouseiImpl(SunflowerYousei source) implements PlayerPolymerEntity {
    public SunflowerYouseiImpl {
        if (!source.level().isClientSide()) {
            PolymerEntityHelper.addEntityHolderModel(this);
        }
    }

    @Override
    public void onCreated() {
        var entity = this.getEntity();
        PlayerPolymerEntity.super.onCreated();
        var x = new ItemDisplayElement();
        var holder = new WingHolder(this.source);
        x.setItem(new ItemStack(RDEntityHolderItems.YOUSEI_WINGS.asItem()));
        x.setInvisible(true);
        x.setTeleportDuration(3);
        x.setScale(new Vector3f(1.2f));
        holder.setElement(x);
        holder.addElement(x);
        EntityAttachment.ofTicking(holder, entity);
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
