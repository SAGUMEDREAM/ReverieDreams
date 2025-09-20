package cc.thonly.reverie_dreams.server;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;

public class CookingInputRecipeManager {
    private static final CookingInputRecipeManager INSTANCE = new CookingInputRecipeManager();
    private final Set<Item> items = new HashSet<>();
    private final Set<Item> smelting = new HashSet<>();
    private final Set<Item> blast = new HashSet<>();
    private final Set<Item> smoker = new HashSet<>();

    private CookingInputRecipeManager() {
    }

    public static CookingInputRecipeManager getInstance() {
        return INSTANCE;
    }

    public static boolean isFuel(ItemStack itemStack) {
        return itemStack.isIn(ItemTags.COALS) || itemStack.getItem() == Items.DRIED_KELP_BLOCK;
    }

    public void clearItems() {
        this.items.clear();
        this.smelting.clear();
        this.blast.clear();
        this.smoker.clear();
    }

    public void load(MinecraftServer server) {
        this.clearItems();
        ServerRecipeManager recipeManager = server.getRecipeManager();
        for (RecipeEntry<?> recipeEntry : recipeManager.values()) {
            Recipe<?> recipe = recipeEntry.value();
            if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
                Ingredient ingredient = cookingRecipe.ingredient();
                for (RegistryEntry<Item> entry : ingredient.entries) {
                    Item value = entry.value();
                    this.items.add(value);
                    if (recipe instanceof SmeltingRecipe) {
                        this.smelting.add(value);
                    }
                    if (recipe instanceof SmokingRecipe) {
                        this.smoker.add(value);
                    }
                    if (recipe instanceof BlastingRecipe) {
                        this.blast.add(value);
                    }
                }
            }
        }
    }

    public boolean contains(Item item) {
        return this.items.contains(item);
    }

    public boolean isSmelting(Item item) {
        return this.smelting.contains(item);
    }
    public boolean isSmoker(Item item) {
        return this.smoker.contains(item);
    }
    public boolean isBlast(Item item) {
        return this.blast.contains(item);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public Set<Item> items() {
        return new HashSet<>(this.items);
    }
}
