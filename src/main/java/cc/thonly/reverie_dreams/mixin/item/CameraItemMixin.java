package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import de.tomalbrc.cameraobscura.item.CameraItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(CameraItem.class)
public abstract class CameraItemMixin extends SimplePolymerItem {

    public CameraItemMixin(Properties settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        super.inventoryTick(itemStack, level, entity, i, bl);

        if (!(entity instanceof ServerPlayer player)) return;

        ItemStack newItem = new ItemStack(RDItems.HIMEKAIDOU_HATATES_PHONE.builtInRegistryHolder(), itemStack.getCount(), itemStack.getComponentsPatch());
        Inventory inventory = player.getInventory();
        if (inventory.items.size() > i) {
            inventory.setItem(i, newItem);
        }
    }
}
