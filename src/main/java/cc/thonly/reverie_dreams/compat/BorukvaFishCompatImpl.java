package cc.thonly.reverie_dreams.compat;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.recipe.MiRecipeManager;
import cc.thonly.mystias_izakaya.recipe.entry.KitchenRecipe;
import cc.thonly.mystias_izakaya.registry.FoodProperties;
import cc.thonly.mystias_izakaya.registry.MIRegistryManager;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import com.phoen1x.borukvafish.item.ModItems;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@SuppressWarnings("unchecked")
public class BorukvaFishCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(MiRecipeManager.KITCHEN_RECIPE);
            builder.add(Items.COD, ModItems.RAW_ASP);
            builder.add(Items.COD, ModItems.RAW_BIGHEAD);
            builder.add(Items.COD, ModItems.RAW_CARP);
            builder.add(Items.COD, ModItems.RAW_CATFISH);
            builder.add(Items.COD, ModItems.RAW_CRUCIAN);
            builder.add(Items.COD, ModItems.RAW_PIKE);
            builder.add(Items.COD, ModItems.RAW_PERCH);

            builder.add(Items.COD, ModItems.ASP_FILLET);
            builder.add(Items.COD, ModItems.COOKED_ASP_FILLET);
            builder.add(Items.COD, ModItems.BIGHEAD_FILLET);
            builder.add(Items.COD, ModItems.COOKED_BIGHEAD_FILLET);
            builder.add(Items.COD, ModItems.CARP_FILLET);
            builder.add(Items.COD, ModItems.COOKED_CARP_FILLET);
            builder.add(Items.COD, ModItems.CRUCIAN_FILLET);
            builder.add(Items.COD, ModItems.COOKED_CRUCIAN_FILLET);
            builder.add(Items.COD, ModItems.PIKE_FILLET);
            builder.add(Items.COD, ModItems.COOKED_PIKE_FILLET);
            builder.add(Items.COD, ModItems.PERCH_FILLET);
            builder.add(Items.COD, ModItems.COOKED_PERCH_FILLET);

            builder.add(Items.COD, ModItems.CATFISH_FILLET);
            builder.add(Items.COD, ModItems.COOKED_CATFISH_FILLET);
        });

        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(MIRegistryManager.FOOD_PROPERTY)) {
                return;
            }
            IntrinsicalRegister<FoodProperty> registry = (IntrinsicalRegister<FoodProperty>) simpleRegistry;
            Stream<Map.Entry<ResourceLocation, FoodProperty>> stream = registry.streamIdToValue();
            stream.forEach(mapEntry->{
                FoodProperty property = mapEntry.getValue();
                Set<Item> items = property.getItems();
                if (property.equals(FoodProperties.AQUATIC_PRODUCTS) || property.equals(FoodProperties.OCEAN_FLAVOR) || property.equals(FoodProperties.UMAMI)) {
                    items.add(ModItems.RAW_ASP);
                    items.add(ModItems.RAW_BIGHEAD);
                    items.add(ModItems.RAW_CARP);
                    items.add(ModItems.RAW_CATFISH);
                    items.add(ModItems.RAW_CRUCIAN);
                    items.add(ModItems.RAW_PIKE);
                    items.add(ModItems.RAW_PERCH);

                    items.add(ModItems.ASP_FILLET);
                    items.add(ModItems.COOKED_ASP_FILLET);
                    items.add(ModItems.BIGHEAD_FILLET);
                    items.add(ModItems.COOKED_BIGHEAD_FILLET);
                    items.add(ModItems.CARP_FILLET);
                    items.add(ModItems.COOKED_CARP_FILLET);
                    items.add(ModItems.CRUCIAN_FILLET);
                    items.add(ModItems.COOKED_CRUCIAN_FILLET);
                    items.add(ModItems.PIKE_FILLET);
                    items.add(ModItems.COOKED_PIKE_FILLET);
                    items.add(ModItems.PERCH_FILLET);
                    items.add(ModItems.COOKED_PERCH_FILLET);

                    items.add(ModItems.CATFISH_FILLET);
                    items.add(ModItems.COOKED_CATFISH_FILLET);
                }
            });

        });
    }
}
