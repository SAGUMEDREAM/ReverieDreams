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
import com.phoen1x.borukvafoodexotic.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class BorukvaFoodExoticCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(MiRecipeManager.KITCHEN_RECIPE);
            builder.add(MIItems.CHILI, ModItems.PEPPER);
            builder.add(MIItems.VEGETABLE_SPECIAL, ModItems.BROCCOLI);
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
                if (property.equals(FoodProperties.FRUITY)) {
                    tags.add(ModItems.APRICOT);
                    tags.add(ModItems.PEAR);
                    tags.add(ModItems.ORANGE);
                    tags.add(ModItems.PLUM);
                    tags.add(ModItems.KIWI);
                    tags.add(ModItems.STRAWBERRY);
                }
                if (property.equals(FoodProperties.VEGETARIAN)) {
                    tags.add(ModItems.PEAS);
                    tags.add(ModItems.SPINACH);
                    tags.add(ModItems.GREEN_BEAN);
                    tags.add(ModItems.BROCCOLI);
                }
                if (property.equals(FoodProperties.SPICY)) {
                    tags.add(ModItems.GARLIC);
                    tags.add(ModItems.PEPPER);
                }
            });
        });
    }
}
