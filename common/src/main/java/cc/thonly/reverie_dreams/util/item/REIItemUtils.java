package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class REIItemUtils {
    public static EntryIngredient getItem(Holder<Item> holder) {
        return EntryIngredient.of(EntryStacks.ofItemHolder(holder));
    }

    public static EntryIngredient getItem(ItemLike item) {
        return EntryIngredient.of(EntryStacks.of(item.asItem()));
    }

    public static EntryIngredient getItem(DeferredItem item) {
        return EntryIngredient.of(EntryStacks.of(item.asItem()));
    }

    public static EntryIngredient getItem(ItemStack itemStack) {
        return EntryIngredient.of(EntryStacks.of(itemStack));
    }

    public static EntryIngredient getItem(ItemStackWrapper wrapper) {
        return getItem(wrapper.getItemStack());
    }
}
