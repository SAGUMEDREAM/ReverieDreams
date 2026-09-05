package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.polymer.entity.inf.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import cc.thonly.reverie_dreams.polymer.entity.holder.MagicBroomHolder;
import cc.thonly.reverie_dreams.entity.misc.MagicBroom;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.WeakHashMap;

@SuppressWarnings("resource")
public record MagicBroomImpl(MagicBroom source) implements PolymerEntity, PolymerHolderEntity {
    public static final WeakHashMap<Entity, ItemDisplayElement> ELEMENTS = new WeakHashMap<>();

    public MagicBroomImpl {
        if (!source.level().isClientSide()) {
            PolymerEntityHelper.addEntityHolderModel(this);
        }

    }

    @Override
    public void onCreated() {
        this.source.setNoGravity(true);
        var x = new ItemDisplayElement();
        var holder = new MagicBroomHolder(this.source);
        var stack = new ItemStack(RDEntityHolderItems.MAGIC_BROOM_DISPLAY.asItem());
        if (this.source.getIngredientStack().build().hasFoil()) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        x.setItem(stack);
        x.setItemDisplayContext(ItemDisplayContext.HEAD);
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
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PIG;
    }

    public void onTrackingStopped(ServerPlayer player) {
        ItemDisplayElement element = ELEMENTS.get(this.source);
        if (element != null) {
            ElementHolder holder = element.getHolder();
            if (holder != null) {
                holder.destroy();
            }
        }
        ELEMENTS.remove(this.source);
    }
}
