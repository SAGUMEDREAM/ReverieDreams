package cc.thonly.reverie_dreams.inf;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

public interface IItemStackTemplateModifier {
    void reverie_dreams$setItem(Holder<Item> item);

    void reverie_dreams$setCount(int count);

    void reverie_dreams$setComponents(DataComponentPatch patch);

    static IItemStackTemplateModifier of(ItemStackTemplate template) {
        return (IItemStackTemplateModifier) (Object) template;
    }
}
