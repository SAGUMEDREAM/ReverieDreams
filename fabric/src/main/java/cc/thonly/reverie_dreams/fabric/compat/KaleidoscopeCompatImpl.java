package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.recipe.PatchBuilder;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatches;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.component.tooltip.InitTooltips;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.fabric.mixin.accessor.PotRecipeAccessor;
import cc.thonly.reverie_dreams.fabric.mixin.accessor.SingleItemRecipeAccessor;
import cc.thonly.reverie_dreams.fabric.mixin.accessor.StockpotRecipeAccessor;
import cc.thonly.reverie_dreams.api.recipe.RecipeIngredientItems;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;

import java.util.Set;

@SuppressWarnings("deprecation")
public class KaleidoscopeCompatImpl {
    public static void bootstrap() {
        RecipeIngredientItems.create(context -> {
            context.add(ModItems.TOMATO.builtInRegistryHolder(), RDIngredientItems.TOMATO);
            context.add(ModItems.RED_CHILI.builtInRegistryHolder(), RDIngredientItems.CHILI);
            context.add(ModItems.GREEN_CHILI.builtInRegistryHolder(), RDIngredientItems.CHILI);
            context.add(ModItems.FLOUR.builtInRegistryHolder(), RDIngredientItems.FLOUR);

            context.addProcessor(ModRecipes.POT_RECIPE, recipe -> {
                Object object = recipe;
                if (object instanceof PotRecipeAccessor potRecipeAccessor) {
                    NonNullList<Ingredient> ingredients = potRecipeAccessor.reverie_dreams$getIngredients();
                    NonNullList<Ingredient> copy = NonNullList.create();
                    for (Ingredient ingredient : ingredients) {
                        copy.add(context.modify(ingredient, (ctx, modifier) -> {
                            return modifier.appendIf(ingredient, targetIngredient -> true);
                        }));
                    }
                    potRecipeAccessor.reverie_dreams$setIngredients(copy);
                }
            });
            context.addProcessor(ModRecipes.CHOPPING_BOARD_RECIPE, recipe -> {
                if (!(recipe instanceof SingleItemRecipe singleItemRecipe)) {
                    return;
                }
                if(singleItemRecipe instanceof SingleItemRecipeAccessor accessor) {
                    Ingredient ingredient = accessor.reverie_dreams$getInput();
                    Ingredient modify = context.modify(ingredient, (ctx, modifier) -> {
                        return modifier.appendIf(ingredient, targetIngredient -> true);
                    });
                    accessor.reverie_dreams$setInput(modify);
                }
            });
            context.addProcessor(ModRecipes.STOCKPOT_RECIPE, recipe -> {
                Object object = recipe;
                if (object instanceof StockpotRecipeAccessor stockpotRecipeAccessor) {
                    NonNullList<Ingredient> ingredients = stockpotRecipeAccessor.reverie_dreams$getIngredients();
                    NonNullList<Ingredient> copy = NonNullList.create();
                    for (Ingredient ingredient : ingredients) {
                        copy.add(context.modify(ingredient, (ctx, modifier) -> {
                            return modifier.appendIf(ingredient, targetIngredient -> true);
                        }));
                    }
                    stockpotRecipeAccessor.reverie_dreams$setIngredients(copy);
                }
            });
            context.addProcessor(ModRecipes.STEAMER_RECIPE, recipe -> {
                if (recipe instanceof SingleItemRecipeAccessor accessor) {
                    Ingredient ingredient = accessor.reverie_dreams$getInput();
                    Ingredient modify = context.modify(ingredient, (ctx, modifier) -> {
                        return modifier.appendIf(ingredient, targetIngredient -> true);
                    });
                    accessor.reverie_dreams$setInput(modify);
                }
            });
            context.addProcessor(ModRecipes.MILLSTONE_RECIPE, recipe -> {
                if (recipe instanceof SingleItemRecipeAccessor accessor) {
                    Ingredient ingredient = accessor.reverie_dreams$getInput();
                    Ingredient modify = context.modify(ingredient, (ctx, modifier) -> {
                        return modifier.appendIf(ingredient, targetIngredient -> true);
                    });
                    accessor.reverie_dreams$setInput(modify);
                }
            });
//            context.addProcessor(ModRecipes.TEAPOT_RECIPE, recipe -> {
//                TeapotRecipeAccessor accessor = (TeapotRecipeAccessor) (Object) recipe;
//                assert accessor != null;
//                Ingredient ingredient = accessor.reverie_dreams$getIngredient();
//                Ingredient modify = context.modify(ingredient, (ctx, modifier) -> {
//                    return modifier.appendIf(ingredient, targetIngredient -> true);
//                });
//                accessor.reverie_dreams$setIngredients(modify);
//            });

        });
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            PatchBuilder<KitchenRecipe> builder = RecipeCompatPatches.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(RDIngredientItems.TOMATO, ModItems.TOMATO);
            builder.add(RDIngredientItems.CHILI, ModItems.RED_CHILI);
            builder.add(RDIngredientItems.CHILI, ModItems.GREEN_CHILI);
            builder.add(RDIngredientItems.CHILI, ModItems.CHILI_RISTRA);
            builder.add(RDIngredientItems.STICKY_RICE, ModItems.RICE_PANICLE);
            builder.add(RDIngredientItems.FLOUR, ModItems.RICE_PANICLE);
            builder.add(Items.WHEAT, ModItems.RICE_PANICLE);
            builder.add(RDIngredientItems.CICADA_SHELL, ModItems.CATERPILLAR);
            builder.add(Items.MUTTON, ModItems.RAW_LAMB_CHOPS);
            builder.add(Items.MUTTON, ModItems.COOKED_LAMB_CHOPS);
            builder.add(Items.BEEF, ModItems.RAW_COW_OFFAL);
            builder.add(Items.BEEF, ModItems.COOKED_COW_OFFAL);
            builder.add(Items.PORKCHOP, ModItems.RAW_PORK_BELLY);
            builder.add(Items.PORKCHOP, ModItems.COOKED_PORK_BELLY);
            builder.add(Items.PORKCHOP, ModItems.RAW_DONKEY_MEAT);
            builder.add(Items.PORKCHOP, ModItems.COOKED_DONKEY_MEAT);
            builder.add(Items.PORKCHOP, ModItems.RAW_MEATBALL);
            builder.add(Items.PORKCHOP, ModItems.COOKED_MEATBALL);
            builder.add(Items.CHICKEN, ModItems.RAW_CUT_SMALL_MEATS);
            builder.add(Items.CHICKEN, ModItems.COOKED_CUT_SMALL_MEATS);
            builder.add(Items.EGG, ModItems.FRIED_EGG);
        });

        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.is(FoodProperties.VEGETARIAN)) {
                items.add(ModItems.TOMATO);
            }
            if (property.is(FoodProperties.MEAT)) {
                items.add(ModItems.RAW_LAMB_CHOPS);
                items.add(ModItems.COOKED_LAMB_CHOPS);
                items.add(ModItems.RAW_COW_OFFAL);
                items.add(ModItems.COOKED_COW_OFFAL);
                items.add(ModItems.RAW_PORK_BELLY);
                items.add(ModItems.COOKED_PORK_BELLY);
                items.add(ModItems.RAW_DONKEY_MEAT);
                items.add(ModItems.COOKED_DONKEY_MEAT);
                items.add(ModItems.RAW_MEATBALL);
                items.add(ModItems.COOKED_MEATBALL);
                items.add(ModItems.RAW_CUT_SMALL_MEATS);
                items.add(ModItems.COOKED_CUT_SMALL_MEATS);
            }
            if (property.is(FoodProperties.SPICY)) {
                items.add(ModItems.RED_CHILI);
                items.add(ModItems.GREEN_CHILI);
                items.add(ModItems.CHILI_RISTRA);
            }
            if (property.is(FoodProperties.FILLING)) {
                items.add(ModItems.RICE_PANICLE);
            }
        });
        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            InitTooltips.copyItemTooltip(RDIngredientItems.TOMATO, ModItems.TOMATO);
            InitTooltips.copyItemTooltip(RDIngredientItems.CHILI, ModItems.RED_CHILI);
            InitTooltips.copyItemTooltip(RDIngredientItems.CHILI, ModItems.GREEN_CHILI);
            InitTooltips.copyItemTooltip(RDIngredientItems.CHILI, ModItems.CHILI_RISTRA);
            InitTooltips.copyItemTooltip(RDIngredientItems.STICKY_RICE, ModItems.RICE_PANICLE);
            InitTooltips.copyItemTooltip(RDIngredientItems.FLOUR, ModItems.RICE_PANICLE);
            InitTooltips.copyItemTooltip(Items.WHEAT, ModItems.RICE_PANICLE);
            InitTooltips.copyItemTooltip(RDIngredientItems.CICADA_SHELL, ModItems.CATERPILLAR);
            InitTooltips.copyItemTooltip(Items.MUTTON, ModItems.RAW_LAMB_CHOPS);
            InitTooltips.copyItemTooltip(Items.MUTTON, ModItems.COOKED_LAMB_CHOPS);
            InitTooltips.copyItemTooltip(Items.BEEF, ModItems.RAW_COW_OFFAL);
            InitTooltips.copyItemTooltip(Items.BEEF, ModItems.COOKED_COW_OFFAL);
            InitTooltips.copyItemTooltip(Items.PORKCHOP, ModItems.RAW_PORK_BELLY);
            InitTooltips.copyItemTooltip(Items.PORKCHOP, ModItems.COOKED_PORK_BELLY);
            InitTooltips.copyItemTooltip(Items.PORKCHOP, ModItems.RAW_DONKEY_MEAT);
            InitTooltips.copyItemTooltip(Items.PORKCHOP, ModItems.COOKED_DONKEY_MEAT);
            InitTooltips.copyItemTooltip(Items.PORKCHOP, ModItems.RAW_MEATBALL);
            InitTooltips.copyItemTooltip(Items.PORKCHOP, ModItems.COOKED_MEATBALL);
            InitTooltips.copyItemTooltip(Items.CHICKEN, ModItems.RAW_CUT_SMALL_MEATS);
            InitTooltips.copyItemTooltip(Items.CHICKEN, ModItems.COOKED_CUT_SMALL_MEATS);
            InitTooltips.copyItemTooltip(Items.EGG, ModItems.FRIED_EGG);
        });
    }
}
