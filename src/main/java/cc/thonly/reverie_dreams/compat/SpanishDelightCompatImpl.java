package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.api.FoodPropertiesLoaderCallback;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.equals(FoodProperties.SPICY)) {
                items.add(ModItems.GARLIC);
                items.add(ModItems.RED_PEPPER);
                items.add(ModItems.GREEN_PEPPER);
                items.add(ModItems.PAPRIKA);
            }
            if (property.equals(FoodProperties.FIERY)) {
                items.add(ModItems.RED_PEPPER);
                items.add(ModItems.GREEN_PEPPER);
                items.add(ModItems.PAPRIKA);
            }
            if (property.equals(FoodProperties.VEGETARIAN)) {
                items.add(ModItems.GREEN_BEAN);
                items.add(ModItems.SLICED_ONION);
                items.add(ModItems.SLICED_POTATO);
            }
            if (property.equals(FoodProperties.UMAMI)) {
                items.add(ModItems.SLICED_ONION);
            }
            if (property.equals(FoodProperties.AQUATIC_PRODUCTS)) {
                items.add(ModItems.SQUID_RING);
            }
        });
    }
}
