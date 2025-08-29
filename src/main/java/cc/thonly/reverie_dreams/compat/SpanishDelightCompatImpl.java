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
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import io.github.macuguita.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class SpanishDelightCompatImpl {
    public static void bootstrap(
    ) {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(MiRecipeManager.KITCHEN_RECIPE);
            builder.add(MIItems.CHILI, ModItems.PAPRIKA);
            builder.add(Items.POTATO, ModItems.SLICED_POTATO);
            builder.add(MIItems.ONION, ModItems.SLICED_ONION);
            builder.add(MIItems.CHILI, ModItems.RED_PEPPER);
            builder.add(MIItems.CHILI, ModItems.GREEN_PEPPER);
        });
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(MIRegistryManager.FOOD_PROPERTY)) {
                return;
            }
            IntrinsicalRegister<FoodProperty> registry = (IntrinsicalRegister<FoodProperty>) simpleRegistry;
            Stream<? extends Map.Entry<Identifier, FoodProperty>> stream = registry.streamIdToValue();
            stream.forEach((Consumer<Map.Entry<Identifier, FoodProperty>>) mapEntry -> {
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
