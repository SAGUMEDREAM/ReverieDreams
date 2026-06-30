package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.api.recipe.PatchBuilder;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatches;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.fabric.mixin.accessor.CookingPotRecipeAccessor;
import cc.thonly.reverie_dreams.fabric.mixin.accessor.CuttingBoardRecipeAccessor;
import cc.thonly.reverie_dreams.api.recipe.RecipeIngredientItems;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.*;

@SuppressWarnings({"deprecation", "ALL"})
public class FarmersdelightCompatImpl {
    public static void bootstrap() {
        RecipeIngredientItems.create(context -> {
            context.add(ModItems.TOMATO.get().builtInRegistryHolder(), RDIngredientItems.TOMATO.asItem());
            context.add(ModItems.ONION.get().builtInRegistryHolder(), RDIngredientItems.ONION.asItem());
            context.add(Items.BEEF.builtInRegistryHolder(), RDIngredientItems.WAGYU_BEEF.asItem());
            context.addProcessor(ModRecipeTypes.COOKING.get(), recipe -> {
                CookingPotRecipeAccessor accessor = (CookingPotRecipeAccessor) recipe;
                List<Ingredient> ingredients = accessor.reverie_dreams$getInputItems();
                List<Ingredient> copy = new ArrayList<>();
                for (Ingredient ingredient : ingredients) {
                    copy.add(context.modify(ingredient, (ctx, modifier) -> {
                        return modifier.appendIf(ingredient, targetIngredient -> true);
                    }));
                }
                accessor.reverie_dreams$setInputItems(copy);
            });
            context.addProcessor(ModRecipeTypes.CUTTING.get(), recipe -> {
                CuttingBoardRecipeAccessor accessor = (CuttingBoardRecipeAccessor) recipe;
                Ingredient ingredient = accessor.reverie_dreams$getInput();
                Ingredient modify = context.modify(ingredient, (ctx, modifier) -> {
                    return modifier.appendIf(ingredient, targetIngredient -> true);
                });
                accessor.reverie_dreams$setInput(modify);
            });
        });
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            PatchBuilder<KitchenRecipe> builder = RecipeCompatPatches.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(RDIngredientItems.TOMATO, ModItems.TOMATO.get());
            builder.add(RDIngredientItems.ONION, ModItems.ONION.get());
            builder.add(Items.PORKCHOP, ModItems.BACON.get());
            builder.add(Items.PORKCHOP, ModItems.COOKED_BACON.get());
            builder.add(Items.PUMPKIN, ModItems.PUMPKIN_SLICE.get());
            builder.add(Items.MUTTON, ModItems.MUTTON_CHOPS.get());
            builder.add(Items.MUTTON, ModItems.COOKED_MUTTON_CHOPS.get());
            builder.add(Items.COD, ModItems.COD_SLICE.get());
            builder.add(Items.COD, ModItems.COOKED_COD_SLICE.get());
            builder.add(Items.SALMON, ModItems.SALMON_SLICE.get());
            builder.add(Items.SALMON, ModItems.COOKED_SALMON_SLICE.get());
            builder.add(Items.BEEF, ModItems.MINCED_BEEF.get());
            builder.add(RDIngredientItems.WAGYU_BEEF, ModItems.MINCED_BEEF.get());
        });
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.is(FoodProperties.AQUATIC_PRODUCTS)) {
                items.add(ModItems.COD_SLICE.get());
                items.add(ModItems.COOKED_COD_SLICE.get());
                items.add(ModItems.SALMON_SLICE.get());
                items.add(ModItems.COOKED_SALMON_SLICE.get());
            }
            if (property.is(FoodProperties.MEAT)) {
                items.add(ModItems.BACON.get());
                items.add(ModItems.COOKED_BACON.get());
                items.add(ModItems.PUMPKIN_SLICE.get());
                items.add(ModItems.MUTTON_CHOPS.get());
                items.add(ModItems.COOKED_MUTTON_CHOPS.get());
                items.add(ModItems.MINCED_BEEF.get());
            }
            if (property.is(FoodProperties.VEGETARIAN)) {
                items.add(ModItems.CABBAGE.get());
                items.add(ModItems.CABBAGE_LEAF.get());
                items.add(ModItems.TOMATO.get());
                items.add(ModItems.ONION.get());
            }
            if (property.is(FoodProperties.UMAMI)) {
                items.add(ModItems.ONION.get());
            }
            if (property.is(FoodProperties.AQUATIC_PRODUCTS)) {
                items.add(ModItems.COD_SLICE.get());
                items.add(ModItems.SALMON_SLICE.get());
            }
        });
    }
}
