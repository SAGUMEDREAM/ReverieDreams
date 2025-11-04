package cc.thonly.polymer.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.entity.holder.MagicBroomHolder;
import cc.thonly.reverie_dreams.entity.misc.MagicBroomEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.WeakHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record MagicBroomImpl(MagicBroomEntity magicBroomEntity) implements PolymerEntity, PolymerHolderEntity {
    public static final WeakHashMap<Entity, ItemDisplayElement> ELEMENTS = new WeakHashMap<>();

    public MagicBroomImpl {
        PolymerEntityHelper.addEntityHolderModel(this);
    }

    @Override
    public void onCreated() {
        this.magicBroomEntity.setNoGravity(true);
        var x = new ItemDisplayElement();
        var holder = new MagicBroomHolder(this.magicBroomEntity);
        var stack = new ItemStack(ModEntityHolders.MAGIC_BROOM_DISPLAY);
        if (this.magicBroomEntity.itemWrapper.getItemStack().hasFoil()) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        x.setItem(stack);
        x.setItemDisplayContext(ItemDisplayContext.HEAD);
        x.setInvisible(true);
        x.setTeleportDuration(3);
        x.setScale(new Vector3f(1.2f));
        holder.setElement(x);
        holder.addElement(x);
        EntityAttachment.ofTicking(holder, this.magicBroomEntity);
        VirtualEntityUtils.addVirtualPassenger(this.magicBroomEntity, x.getEntityId());
        ELEMENTS.put(this.magicBroomEntity, x);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PIG;
    }

    public void onTrackingStopped(ServerPlayer player) {
        ItemDisplayElement element = ELEMENTS.get(this.magicBroomEntity);
        if (element != null) {
            ElementHolder holder = element.getHolder();
            if (holder != null) {
                holder.destroy();
            }
        }
        ELEMENTS.remove(this.magicBroomEntity);
    }
}
