package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {
    @Accessor("builtInRegistryHolder")
    Holder.Reference<Item> reverie_dreams$builtInRegistryHolder();
    @Accessor("components")
    void reverie_dreams$setComponents(DataComponentMap components);
}
