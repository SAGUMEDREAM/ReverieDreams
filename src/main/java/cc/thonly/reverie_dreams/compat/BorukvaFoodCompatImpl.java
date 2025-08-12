package cc.thonly.reverie_dreams.compat;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.recipe.MiRecipeManager;
import cc.thonly.mystias_izakaya.recipe.entry.KitchenRecipe;
import cc.thonly.mystias_izakaya.registry.FoodProperties;
import cc.thonly.mystias_izakaya.registry.MIRegistryManager;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import com.opryshok.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class BorukvaFoodCompatImpl {
    public static Block BETTER_FARMLAND = Blocks.AIR;
    public static boolean HAS_LOADED = false;
    public static void bootstrap() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            BETTER_FARMLAND = Registries.BLOCK.get(Identifier.of("borukva-food","better_farmland"));
            HAS_LOADED = true;
        });
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(MiRecipeManager.KITCHEN_RECIPE);
            builder.add(MIItems.TOMATO, ModItems.TOMATO);
            builder.add(MIItems.CHILI, ModItems.CHILLI_PEPPER);
            builder.add(MIItems.CUCUMBER, ModItems.CUCUMBER);
            builder.add(MIItems.ONION, ModItems.ONION);
            builder.add(MIItems.LEMON, ModItems.LEMON);
            builder.add(MIItems.GRAPE, ModItems.GRAPE);
            builder.add(MIItems.BUTTER, ModItems.BUTTER);
            builder.add(MIItems.BLACK_SALT, ModItems.SALT);
            builder.add(MIItems.CHEESE, ModItems.CHEESE);
        });
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(MIRegistryManager.FOOD_PROPERTY)) {
                return;
            }
            StandaloneRegistry<FoodProperty> registry = (StandaloneRegistry<FoodProperty>) simpleRegistry;
            Stream<Map.Entry<Identifier, FoodProperty>> stream = registry.stream();
            stream.forEach(mapEntry -> {
                FoodProperty property = mapEntry.getValue();
                Set<Item> tags = property.getItems();
                if (property.equals(FoodProperties.SWEET)) {
                    tags.add(ModItems.GRAPE);
                }
                if (property.equals(FoodProperties.FRUITY)) {
                    tags.add(ModItems.LEMON);
                    tags.add(ModItems.AVOCADO);
                    tags.add(ModItems.GRAPE);
                    tags.add(ModItems.BLACKCURRANTS);
                    tags.add(ModItems.GOOSEBERRY);
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
