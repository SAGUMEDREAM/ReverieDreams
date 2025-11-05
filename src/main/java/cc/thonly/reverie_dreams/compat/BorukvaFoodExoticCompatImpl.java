package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import com.phoen1x.borukvafoodexotic.item.ModItems;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@SuppressWarnings("unchecked")
public class BorukvaFoodExoticCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(RDIngredientItems.CHILI, ModItems.PEPPER);
            builder.add(RDFoodItems.VEGETABLE_SPECIAL, ModItems.BROCCOLI);
        });
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(RegistryHandlers.FOOD_PROPERTY)) {
                return;
            }
            RegistryHandler<FoodProperty> registry = (RegistryHandler<FoodProperty>) simpleRegistry;
            Stream<Map.Entry<ResourceLocation, FoodProperty>> stream = registry.streamIdToValue();
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
