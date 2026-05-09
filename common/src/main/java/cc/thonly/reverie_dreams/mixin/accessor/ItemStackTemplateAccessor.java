package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackTemplate.class)
public interface ItemStackTemplateAccessor {
    @Accessor("item")
    void reverie_dreams$setItem(Holder<Item> item);

    @Accessor("count")
    void reverie_dreams$setCount(int patch);

    @Accessor("components")
    void reverie_dreams$setComponents(DataComponentPatch patch);
}
