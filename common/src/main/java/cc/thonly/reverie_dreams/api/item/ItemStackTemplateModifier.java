package cc.thonly.reverie_dreams.api.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

public interface ItemStackTemplateModifier {
    void reverie_dreams$setItem(Holder<Item> item);

    void reverie_dreams$setCount(int count);

    void reverie_dreams$setComponents(DataComponentPatch patch);

    static ItemStackTemplateModifier of(ItemStackTemplate template) {
        return (ItemStackTemplateModifier) (Object) template;
    }
}
