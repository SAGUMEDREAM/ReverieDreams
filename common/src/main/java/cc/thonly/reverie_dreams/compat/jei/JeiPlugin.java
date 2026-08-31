package cc.thonly.reverie_dreams.compat.jei;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.cooking.*;
import cc.thonly.reverie_dreams.compat.ItemViewItemInfo;
import cc.thonly.reverie_dreams.compat.jei.category.*;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.recipe.IClientRecipes;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    public static final Identifier ID = ReverieDreams.id("jei_plugin");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new DanmakuCraftingTableRecipeCategory(guiHelper));
        registry.addRecipeCategories(new DanmakuShapeDrawRecipeCategory(guiHelper));
        registry.addRecipeCategories(new GensokyoAltarRecipeCategory(guiHelper));
        registry.addRecipeCategories(new StrengthTableRecipeCategory(guiHelper));
        registry.addRecipeCategories(new BaseKitchenRecipeCategory.CookingPotImpl(guiHelper));
        registry.addRecipeCategories(new BaseKitchenRecipeCategory.CuttingBoardImpl(guiHelper));
        registry.addRecipeCategories(new BaseKitchenRecipeCategory.FryingPanImpl(guiHelper));
        registry.addRecipeCategories(new BaseKitchenRecipeCategory.GrillImpl(guiHelper));
        registry.addRecipeCategories(new BaseKitchenRecipeCategory.SteamerImpl(guiHelper));
        registry.addRecipeCategories(new BrewingBarrelRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        IClientRecipes recipes = new IClientRecipes();
        registry.addRecipes(JeiRecipeTypes.DANMAKU, recipes.getRecipeTypeList(RecipeManager.DANMAKU));
        registry.addRecipes(JeiRecipeTypes.DANMAKU_SHAPE_DRAW, recipes.getRecipeTypeList(RecipeManager.DANMAKU_SHAPE_DRAW));
        registry.addRecipes(JeiRecipeTypes.GENSOKYO_ALTAR, recipes.getRecipeTypeList(RecipeManager.GENSOKYO_ALTAR));
        registry.addRecipes(JeiRecipeTypes.STRENGTH_TABLE, StrengthTableRecipe.createRecipeList());
        registry.addRecipes(JeiRecipeTypes.COOKING_POT, recipes.getKitchenRecipeTypeList(KitchenRecipeType.TypeInstance.COOKING_POT));
        registry.addRecipes(JeiRecipeTypes.CUTTING_BOARD, recipes.getKitchenRecipeTypeList(KitchenRecipeType.TypeInstance.CUTTING_BOARD));
        registry.addRecipes(JeiRecipeTypes.FRYING_PAN, recipes.getKitchenRecipeTypeList(KitchenRecipeType.TypeInstance.FRYING_PAN));
        registry.addRecipes(JeiRecipeTypes.GRILL, recipes.getKitchenRecipeTypeList(KitchenRecipeType.TypeInstance.GRILL));
        registry.addRecipes(JeiRecipeTypes.STEAMER, recipes.getKitchenRecipeTypeList(KitchenRecipeType.TypeInstance.STEAMER));
        registry.addRecipes(JeiRecipeTypes.BREWING, recipes.getRecipeTypeList(RecipeManager.BREWING_BARREL));


        ItemViewItemInfo.registerItemInfo((items, component) -> {
            registry.addIngredientInfo(items.stream().map(Item::getDefaultInstance).toList(), VanillaTypes.ITEM_STACK, component);
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        registry.addCraftingStation(JeiRecipeTypes.DANMAKU, RDBlocks.DANMAKU_CRAFTING_TABLE.createStack());
        registry.addCraftingStation(JeiRecipeTypes.DANMAKU_SHAPE_DRAW, RDItems.DANMAKU_SHAPE_CREATOR.createStack());
        registry.addCraftingStation(JeiRecipeTypes.GENSOKYO_ALTAR, RDBlocks.GENSOKYO_ALTAR.createStack());
        registry.addCraftingStation(JeiRecipeTypes.STRENGTH_TABLE, RDBlocks.STRENGTH_TABLE.createStack());
        for (AbstractKitchenwareBlock block : AbstractKitchenwareBlock.KITCHENWARE_BLOCKS) {
            if (block instanceof CookingPot) {
                registry.addCraftingStation(JeiRecipeTypes.COOKING_POT, block);
            }
            if (block instanceof CuttingBoard) {
                registry.addCraftingStation(JeiRecipeTypes.CUTTING_BOARD, block);
            }
            if (block instanceof FryingPan) {
                registry.addCraftingStation(JeiRecipeTypes.FRYING_PAN, block);
            }
            if (block instanceof Grill) {
                registry.addCraftingStation(JeiRecipeTypes.GRILL, block);
            }
            if (block instanceof Steamer) {
                registry.addCraftingStation(JeiRecipeTypes.STEAMER, block);
            }
        }
        registry.addCraftingStation(JeiRecipeTypes.BREWING, RDBlocks.BREWING_BARREL.createStack());
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        for (DanmakuType danmakuType : BuiltInRegistryProviders.DANMAKU_TYPE) {
            ItemDelegate itemHolder = danmakuType.getItemHolder();
            registry.registerSubtypeInterpreter(itemHolder.asItem(), (stack, context) -> {
                var color = stack.get(DataComponents.DYED_COLOR);
                if (color == null) {
                    return "default";
                }
                return color.rgb();

            });
        }
        registry.registerSubtypeInterpreter(RDItems.ROLE_CARD.asItem(), (stack, context) -> {
            var id = stack.get(RDDataComponentTypes.ROLE_CARD_ID.value());
            if (id == null) {
                return "default";
            }
            return id;
        });
        registry.registerSubtypeInterpreter(RDItems.DANMAKU_SHAPE_CREATOR.asItem(), (stack, context) -> {
            var shape = stack.get(RDDataComponentTypes.DANMAKU_SHAPE.value());
            if (shape == null) {
                return "default";
            }
            return shape;
        });
    }

    @Override
    public Identifier getPluginUid() {
        return ID;
    }
}
