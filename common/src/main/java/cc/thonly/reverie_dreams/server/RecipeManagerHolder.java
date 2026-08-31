package cc.thonly.reverie_dreams.server;

import net.minecraft.world.item.crafting.RecipeManager;

public class RecipeManagerHolder {
    private static RecipeManager INSTANCE;

    public static void set(RecipeManager manager) {
        INSTANCE = manager;
    }

    public static RecipeManager get() {
        return INSTANCE;
    }
}