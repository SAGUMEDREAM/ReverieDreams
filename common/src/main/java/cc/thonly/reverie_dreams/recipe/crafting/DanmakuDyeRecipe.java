package cc.thonly.reverie_dreams.recipe.crafting;


import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DanmakuDyeRecipe extends CustomRecipe {
    public DanmakuDyeRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() < 2) {
            return false;
        }
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.getItem() instanceof DanmakuItem) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (itemStack.getItem() instanceof DyeItem) {
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider registries) {
        ArrayList<DyeItem> list = new ArrayList<>();
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack2 = craftingInput.getItem(i);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.getItem() instanceof DanmakuItem) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2.copy();
                continue;
            }
            Item item = itemStack2.getItem();
            if (item instanceof DyeItem dyeItem) {
                list.add(dyeItem);
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (itemStack.isEmpty() || list.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return applyDyes(itemStack, list);
    }

    public static ItemStack applyDyes(ItemStack stack, List<DyeItem> dyes) {
        int s;
        int p;
        int o;
        if (!(stack.getItem() instanceof DanmakuItem)) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = stack.copyWithCount(1);
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        int m = 0;
        DyedItemColor dyedItemColor = itemStack.get(DataComponents.DYED_COLOR);
        if (dyedItemColor != null) {
            int n = ARGB.red(dyedItemColor.rgb());
            o = ARGB.green(dyedItemColor.rgb());
            p = ARGB.blue(dyedItemColor.rgb());
            l += Math.max(n, Math.max(o, p));
            i += n;
            j += o;
            k += p;
            ++m;
        }
        for (DyeItem dyeItem : dyes) {
            p = dyeItem.getDyeColor().getTextureDiffuseColor();
            int q = ARGB.red(p);
            int r = ARGB.green(p);
            s = ARGB.blue(p);
            l += Math.max(q, Math.max(r, s));
            i += q;
            j += r;
            k += s;
            ++m;
        }
        int n = i / m;
        o = j / m;
        p = k / m;
        float f = (float) l / (float) m;
        float g = Math.max(n, Math.max(o, p));
        n = (int) ((float) n * f / g);
        o = (int) ((float) o * f / g);
        p = (int) ((float) p * f / g);
        s = ARGB.color(0, n, o, p);
        Component hoverName = stack.getHoverName();
        Style style = hoverName.getStyle().withColor(s);
        Component colored = hoverName.copy().setStyle(style);
        itemStack.set(DataComponents.ITEM_NAME, colored);
        itemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(s));
        return itemStack;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return RecipeManager.DANMAKU_DYE_RECIPE.value();
    }
}