package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import de.tomalbrc.cameraobscura.item.CameraItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(CameraItem.class)
public abstract class CameraItemMixin extends SimplePolymerItem {

    public CameraItemMixin(Properties settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, serverLevel, entity, slot);
        if (!(entity instanceof LivingEntity living)) return;

        ItemStack newItem = new ItemStack(RDItems.HIMEKAIDOU_HATATES_PHONE.builtInRegistryHolder(), itemStack.getCount(), itemStack.getComponentsPatch());
        if (slot != null) {
            living.setItemSlot(slot, newItem);
        }
    }
}
