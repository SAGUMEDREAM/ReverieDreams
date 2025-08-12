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
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class FarmersdelightCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(MiRecipeManager.KITCHEN_RECIPE);
            builder.add(MIItems.TOMATO, ModItems.TOMATO.get());
            builder.add(MIItems.ONION, ModItems.ONION.get());
            builder.add(Items.PORKCHOP, ModItems.BACON.get());
            builder.add(Items.PUMPKIN, ModItems.PUMPKIN_SLICE.get());
            builder.add(Items.MUTTON, ModItems.MUTTON_CHOPS.get());
            builder.add(Items.COD, ModItems.COD_SLICE.get());
            builder.add(Items.SALMON, ModItems.SALMON_SLICE.get());
            builder.add(Items.BEEF, ModItems.MINCED_BEEF.get());
            builder.add(MIItems.WAGYU_BEEF, ModItems.MINCED_BEEF.get());
        });
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(MIRegistryManager.FOOD_PROPERTY)) {
                return;
            }
            StandaloneRegistry<FoodProperty> registry = (StandaloneRegistry<FoodProperty>) simpleRegistry;
            Stream<? extends Map.Entry<Identifier, FoodProperty>> stream = registry.stream();
            stream.forEach((Consumer<Map.Entry<Identifier, FoodProperty>>) mapEntry -> {
                FoodProperty property = mapEntry.getValue();
                Set<Item> tags = property.getItems();
                if (property.equals(FoodProperties.VEGETARIAN)) {
                    tags.add(ModItems.CABBAGE.get());
                    tags.add(ModItems.CABBAGE_LEAF.get());
                    tags.add(ModItems.TOMATO.get());
                    tags.add(ModItems.ONION.get());
                }
                if (property.equals(FoodProperties.UMAMI)) {
                    tags.add(ModItems.ONION.get());
                }
                if (property.equals(FoodProperties.AQUATIC_PRODUCTS)) {
                    tags.add(ModItems.COD_SLICE.get());
                    tags.add(ModItems.SALMON_SLICE.get());
                }
            });
        });
    }
}
