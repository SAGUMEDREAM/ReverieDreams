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
import io.github.macuguita.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class SpanishDelightCompatImpl {
    public static void bootstrap(
    ) {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(RDIngredientItems.CHILI, ModItems.PAPRIKA);
            builder.add(Items.POTATO, ModItems.SLICED_POTATO);
            builder.add(RDIngredientItems.ONION, ModItems.SLICED_ONION);
            builder.add(RDIngredientItems.CHILI, ModItems.RED_PEPPER);
            builder.add(RDIngredientItems.CHILI, ModItems.GREEN_PEPPER);
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
                if (property.equals(FoodProperties.SPICY)) {
                    tags.add(ModItems.GARLIC);
                    tags.add(ModItems.RED_PEPPER);
                    tags.add(ModItems.GREEN_PEPPER);
                    tags.add(ModItems.PAPRIKA);
                }
                if (property.equals(FoodProperties.FIERY)) {
                    tags.add(ModItems.RED_PEPPER);
                    tags.add(ModItems.GREEN_PEPPER);
                    tags.add(ModItems.PAPRIKA);
                }
                if (property.equals(FoodProperties.VEGETARIAN)) {
                    tags.add(ModItems.GREEN_BEAN);
                    tags.add(ModItems.SLICED_ONION);
                    tags.add(ModItems.SLICED_POTATO);
                }
                if (property.equals(FoodProperties.UMAMI)) {
                    tags.add(ModItems.SLICED_ONION);
                }
                if (property.equals(FoodProperties.AQUATIC_PRODUCTS)) {
                    tags.add(ModItems.SQUID_RING);
                }
            });
        });
    }
}
