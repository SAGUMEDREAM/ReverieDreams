package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.packettweaker.PacketContext;

@ApiStatus.Experimental
public class DanmakuScriptEntity extends AbstractArrow implements PolymerEntity {
    private ItemStackWrapper itemStackWrapper;

    public DanmakuScriptEntity(EntityType<? extends AbstractArrow> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ITEM_DISPLAY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        ItemStack itemStack = Items.COMMAND_BLOCK.getDefaultInstance();
        if (this.itemStackWrapper == null) {
            return itemStack;
        }
        if (this.itemStackWrapper.isEmpty()) {
            return itemStack;
        }
        itemStack = this.itemStackWrapper.getItemStack();
        return itemStack;
    }
}
