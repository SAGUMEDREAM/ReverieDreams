package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.polymer.entity.PlayerPolymerEntity;
import cc.thonly.reverie_dreams.entity.holder.WingHolder;
import cc.thonly.reverie_dreams.entity.interfaces.Yousei;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public abstract class AbstractNPCEntity extends TamableAnimal implements PlayerPolymerEntity {

    protected AbstractNPCEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public abstract @Nullable LivingEntity getOwner();

    public abstract Property getSkin();

    @Override
    public void onCreated() {
        if (this instanceof Yousei) {
            var x = new ItemDisplayElement();
            var holder = new WingHolder(this);
            x.setItem(new ItemStack(RDEntityHolderItems.YOUSEI_WINGS));
            x.setInvisible(true);
            x.setTeleportDuration(3);
            x.setScale(new Vector3f(1.2f));
            holder.setElement(x);
            holder.addElement(x);
            EntityAttachment.ofTicking(holder, this);
            VirtualEntityUtils.addVirtualPassenger(this, x.getEntityId());
            PolymerEntityHelper.POLYMER_PLAYER_ELEMENTS.put(this, x);
        } else {
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
    }

    @Override
    public LivingEntity getEntity() {
        return this;
    }

}
