package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.api.registry.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import com.phoen1x.borukvafish.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Set;

@SuppressWarnings("unchecked")
public class BorukvaFishCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
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
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
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
    }
}
