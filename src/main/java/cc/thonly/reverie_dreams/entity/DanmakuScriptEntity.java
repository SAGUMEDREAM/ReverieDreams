package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.recipe.ItemStackRecipeWrapper;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.packettweaker.PacketContext;

@ApiStatus.Experimental
public class DanmakuScriptEntity extends PersistentProjectileEntity implements PolymerEntity {
    private ItemStackRecipeWrapper itemStackWrapper;

    public DanmakuScriptEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ITEM_DISPLAY;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        ItemStack itemStack = Items.COMMAND_BLOCK.getDefaultStack();
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
