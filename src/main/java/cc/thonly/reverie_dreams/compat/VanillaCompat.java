package cc.thonly.reverie_dreams.compat;

import cc.thonly.mystias_izakaya.api.FoodPropertyLoaderCallback;
import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.recipe.MiRecipeManager;
import cc.thonly.mystias_izakaya.recipe.entry.KitchenRecipe;
import cc.thonly.mystias_izakaya.registry.FoodProperties;
import cc.thonly.mystias_izakaya.registry.MIRegistryManager;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class VanillaCompat {
    // 修补模组内配方兼容性
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(MiRecipeManager.KITCHEN_RECIPE);
            builder.add(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
            builder.add(Items.EGG, Items.BROWN_EGG);
            builder.add(Items.EGG, Items.BLUE_EGG);
        });
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(MIRegistryManager.FOOD_PROPERTY)) {
                return;
            }
            IntrinsicalRegister<FoodProperty> registry = (IntrinsicalRegister<FoodProperty>) simpleRegistry;
            Stream<? extends Map.Entry<ResourceLocation, FoodProperty>> stream = registry.streamIdToValue();
            stream.forEach((Consumer<Map.Entry<ResourceLocation, FoodProperty>>) mapEntry -> {
                FoodProperty property = mapEntry.getValue();
                Set<Item> tags = property.getItems();
                if (property.equals(FoodProperties.RAW)) {
                    tags.add(Items.BROWN_EGG);
                    tags.add(Items.BLUE_EGG);
                }
                if (property.equals(FoodProperties.VEGETARIAN)) {
                    tags.add(Items.CARROT);
                    tags.add(Items.BEETROOT);
                }
                if (property.equals(FoodProperties.SALTY)) {
                    tags.add(Items.SEA_PICKLE);
                }
                if (property.equals(FoodProperties.MEAT)) {
                    tags.add(Items.CHICKEN);
                    tags.add(Items.RABBIT);
                    tags.add(Items.MUTTON);
                }
                if (property.equals(FoodProperties.FRUITY)) {
                    tags.add(Items.APPLE);
                    tags.add(Items.GOLDEN_APPLE);
                    tags.add(Items.ENCHANTED_GOLDEN_APPLE);
                    tags.add(Items.MELON);
                    tags.add(Items.SWEET_BERRIES);
                    tags.add(Items.GLOW_BERRIES);
                }
                if (property.equals(FoodProperties.SWEET)) {
                    tags.add(Items.SWEET_BERRIES);
                    tags.add(Items.GLOW_BERRIES);
                }
                if (property.equals(FoodProperties.DREAMLIKE)) {
                    tags.add(Items.GLOW_BERRIES);
                    tags.add(Items.GOLDEN_APPLE);
                    tags.add(Items.ENCHANTED_GOLDEN_APPLE);
                }
            });
        });
    }
}
