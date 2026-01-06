package cc.thonly.polymer.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.reverie_dreams.entity.YouseiEntity;
import cc.thonly.reverie_dreams.entity.holder.WingHolder;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public record YouseiImpl(YouseiEntity source) implements PlayerPolymerEntity {
    public YouseiImpl {
        PolymerEntityHelper.addEntityHolderModel(this);
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
