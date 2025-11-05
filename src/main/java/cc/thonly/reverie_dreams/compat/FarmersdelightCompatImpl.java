package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@SuppressWarnings("unchecked")
public class FarmersdelightCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(RDIngredientItems.TOMATO, ModItems.TOMATO.get());
            builder.add(RDIngredientItems.ONION, ModItems.ONION.get());
            builder.add(Items.PORKCHOP, ModItems.BACON.get());
            builder.add(Items.PUMPKIN, ModItems.PUMPKIN_SLICE.get());
            builder.add(Items.MUTTON, ModItems.MUTTON_CHOPS.get());
            builder.add(Items.COD, ModItems.COD_SLICE.get());
            builder.add(Items.SALMON, ModItems.SALMON_SLICE.get());
            builder.add(Items.BEEF, ModItems.MINCED_BEEF.get());
            builder.add(RDIngredientItems.WAGYU_BEEF, ModItems.MINCED_BEEF.get());
        });
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(RegistryHandlers.FOOD_PROPERTY)) {
                return;
            }
            RegistryHandler<FoodProperty> registry = (RegistryHandler<FoodProperty>) simpleRegistry;
            Stream<? extends Map.Entry<ResourceLocation, FoodProperty>> stream = registry.streamIdToValue();
            stream.forEach((Consumer<Map.Entry<ResourceLocation, FoodProperty>>) mapEntry -> {
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
