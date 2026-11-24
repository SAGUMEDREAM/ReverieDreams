package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.ReverieDreams;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Integer steps keep ten cakes exactly at 2.0, including old/edited item data.
 */
public final class FumoGrowth {
    public static final int MAX_CAKES = 10;
    public static final String ITEM_KEY = ReverieDreams.MOD_ID + ":fumo_cakes";

    private FumoGrowth() {
    }

    public static int clamp(int cakes) {
        return Math.clamp(cakes, 0, MAX_CAKES);
    }

    public static float scale(int cakes) {
        return 1f + clamp(cakes) / 10f;
    }

    public static int fromItem(ItemStack stack) {
        return clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(ITEM_KEY, 0));
    }

    public static void writeItem(ItemStack stack, int cakes) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (clamp(cakes) == 0)
                tag.remove(ITEM_KEY);
            else
                tag.putInt(ITEM_KEY, clamp(cakes));
        });
    }
}
