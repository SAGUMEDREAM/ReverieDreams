package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.api.registry.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import com.opryshok.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

@SuppressWarnings("unchecked")
public class BorukvaFoodCompatImpl {
    public static Block BETTER_FARMLAND = Blocks.AIR;
    public static boolean HAS_LOADED = false;

    public static void bootstrap() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            BETTER_FARMLAND = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("borukva-food", "better_farmland"));
            HAS_LOADED = true;
        });
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(RDIngredientItems.TOMATO, ModItems.TOMATO);
            builder.add(RDIngredientItems.CHILI, ModItems.CHILLI_PEPPER);
            builder.add(RDIngredientItems.CUCUMBER, ModItems.CUCUMBER);
            builder.add(RDIngredientItems.ONION, ModItems.ONION);
            builder.add(RDIngredientItems.LEMON, ModItems.LEMON);
            builder.add(RDIngredientItems.GRAPE, ModItems.GRAPE);
            builder.add(RDIngredientItems.BUTTER, ModItems.BUTTER);
            builder.add(RDIngredientItems.BLACK_SALT, ModItems.SALT);
            builder.add(RDIngredientItems.CHEESE, ModItems.CHEESE);
        });
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.is(FoodProperties.SWEET)) {
                items.add(ModItems.GRAPE);
            }
            if (property.is(FoodProperties.FRUITY)) {
                items.add(ModItems.LEMON);
                items.add(ModItems.AVOCADO);
                items.add(ModItems.GRAPE);
                items.add(ModItems.BLACKCURRANTS);
                items.add(ModItems.GOOSEBERRY);
            }
            if (property.is(FoodProperties.FILLING)) {
                items.add(ModItems.CORN);
            }
            if (property.is(FoodProperties.VEGETARIAN)) {
                items.add(ModItems.ONION);
                items.add(ModItems.ENDER_INFECTED_ONION);
                items.add(ModItems.TOMATO);
                items.add(ModItems.CABBAGE);
                items.add(ModItems.CORN);
                items.add(ModItems.CUCUMBER);
                items.add(ModItems.LETTUCE);
            }
            if (property.is(FoodProperties.UMAMI)) {
                items.add(ModItems.ONION);
                items.add(ModItems.ENDER_INFECTED_ONION);
            }
            if (property.is(FoodProperties.FIERY)) {
                items.add(ModItems.CHILLI_PEPPER);
            }
        });
    }


    public static boolean hasBorukvaFood() {
        if (BETTER_FARMLAND == null) {
            return false;
        }
        return BETTER_FARMLAND != Blocks.AIR;
    }
}
