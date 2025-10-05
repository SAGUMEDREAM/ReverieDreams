package cc.thonly.polymer.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.entity.holder.MagicBroomHolder;
import cc.thonly.reverie_dreams.entity.holder.WheelChairHolder;
import cc.thonly.reverie_dreams.entity.misc.WheelchairEntity;
import cc.thonly.reverie_dreams.item.ModItems;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.WeakHashMap;

public record WheelChairImpl(WheelchairEntity wheelchairEntity) implements PolymerEntity, PolymerHolderEntity {
    public static final WeakHashMap<Entity, ItemDisplayElement> ELEMENTS = new WeakHashMap<>();

    public WheelChairImpl {
        PolymerEntityHelper.NEXT.add(this);
    }

    @Override
    public void onCreated() {
        this.wheelchairEntity.setNoGravity(true);
        var x = new ItemDisplayElement();
        var holder = new WheelChairHolder(this.wheelchairEntity);
        var stack = new ItemStack(ModBlocks.WHEEL_CHAIR);
        x.setItem(stack);
        x.setItemDisplayContext(ItemDisplayContext.HEAD);
        x.setInvisible(true);
        x.setTeleportDuration(3);
        holder.setElement(x);
        holder.addElement(x);
        EntityAttachment.ofTicking(holder, this.wheelchairEntity);
        VirtualEntityUtils.addVirtualPassenger(this.wheelchairEntity, x.getEntityId());
        ELEMENTS.put(this.wheelchairEntity, x);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PIG;
    }

    public void onTrackingStopped(ServerPlayerEntity player) {
        ItemDisplayElement element = ELEMENTS.get(this.wheelchairEntity);
        if (element != null) {
            ElementHolder holder = element.getHolder();
            if (holder != null) {
                holder.destroy();
            }
        }
        ELEMENTS.remove(this.wheelchairEntity);
    }
}
