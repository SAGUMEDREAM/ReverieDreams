package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import com.opryshok.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class BorukvaFoodCompatImpl {
    public static Block BETTER_FARMLAND = Blocks.AIR;
    public static boolean HAS_LOADED = false;
    public static void bootstrap() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            BETTER_FARMLAND = BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath("borukva-food","better_farmland"));
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
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(RegistryHandlers.FOOD_PROPERTY)) {
                return;
            }
            RegistryHandler<FoodProperty> registry = (RegistryHandler<FoodProperty>) simpleRegistry;
            Stream<Map.Entry<ResourceLocation, FoodProperty>> stream = registry.streamIdToValue();
            stream.forEach(mapEntry -> {
                FoodProperty property = mapEntry.getValue();
                Set<Item> items = property.getItems();
                if (property.equals(FoodProperties.SWEET)) {
                    items.add(ModItems.GRAPE);
                }
                if (property.equals(FoodProperties.FRUITY)) {
                    items.add(ModItems.LEMON);
                    items.add(ModItems.AVOCADO);
                    items.add(ModItems.GRAPE);
                    items.add(ModItems.BLACKCURRANTS);
                    items.add(ModItems.GOOSEBERRY);
                }
                if (property.equals(FoodProperties.FILLING)) {
                    items.add(ModItems.CORN);
                }
                if (property.equals(FoodProperties.VEGETARIAN)) {
                    items.add(ModItems.ONION);
                    items.add(ModItems.ENDER_INFECTED_ONION);
                    items.add(ModItems.TOMATO);
                    items.add(ModItems.CABBAGE);
                    items.add(ModItems.CORN);
                    items.add(ModItems.CUCUMBER);
                    items.add(ModItems.LETTUCE);
                }
                if (property.equals(FoodProperties.UMAMI)) {
                    items.add(ModItems.ONION);
                    items.add(ModItems.ENDER_INFECTED_ONION);
                }
                if (property.equals(FoodProperties.FIERY)) {
                    items.add(ModItems.CHILLI_PEPPER);
                }
            });
        });
    }


    public static boolean hasBorukvaFood() {
        if (BETTER_FARMLAND == null) {
            return false;
        }
        return BETTER_FARMLAND != Blocks.AIR;
    }
}
