package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class REIItemUtils {
    public static EntryIngredient getItem(Holder<Item> holder) {
        return EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, holder.value().getDefaultInstance()));
    }

    public static EntryIngredient getItem(ItemLike item) {
        return EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, item.asItem().getDefaultInstance()));
    }

    public static EntryIngredient getItem(DeferredItem item) {
        return EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, item.asItem().getDefaultInstance()));
    }

    public static EntryIngredient getItem(ItemStack itemStack) {
        return EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, itemStack));
    }

    public static EntryIngredient getItem(ItemStackWrapper wrapper) {
        return getItem(wrapper.getItemStack());
    }
}
