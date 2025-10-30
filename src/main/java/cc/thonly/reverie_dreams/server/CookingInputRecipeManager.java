package cc.thonly.reverie_dreams.server;

import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
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
        return itemStack.is(ItemTags.COALS) || itemStack.getItem() == Items.DRIED_KELP_BLOCK;
    }

    public void clearItems() {
        this.items.clear();
        this.smelting.clear();
        this.blast.clear();
        this.smoker.clear();
    }

    public void load(MinecraftServer server) {
        this.clearItems();
        RecipeManager recipeManager = server.getRecipeManager();
        for (RecipeHolder<?> recipeEntry : recipeManager.getRecipes()) {
            Recipe<?> recipe = recipeEntry.value();
            if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
                Ingredient ingredient = cookingRecipe.input();
                for (Holder<Item> entry : ingredient.values) {
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
