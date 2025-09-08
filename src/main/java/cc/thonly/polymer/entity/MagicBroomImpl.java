package cc.thonly.polymer.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.entity.holder.MagicBroomHolder;
import cc.thonly.reverie_dreams.entity.misc.MagicBroomEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.WeakHashMap;

public record MagicBroomImpl(MagicBroomEntity magicBroomEntity) implements PolymerEntity, PolymerHolderEntity {
    public static final WeakHashMap<Entity, ItemDisplayElement> ELEMENTS = new WeakHashMap<>();

    public MagicBroomImpl {
        PolymerEntityHelper.NEXT.add(this);
    }
    @Override
    public void onCreated() {
        this.magicBroomEntity.setNoGravity(true);
        var x = new ItemDisplayElement();
        var holder = new MagicBroomHolder(this.magicBroomEntity);
        var stack = new ItemStack(ModEntityHolders.MAGIC_BROOM_DISPLAY);
        if (this.magicBroomEntity.summonItem.hasGlint()) {
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
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

}
