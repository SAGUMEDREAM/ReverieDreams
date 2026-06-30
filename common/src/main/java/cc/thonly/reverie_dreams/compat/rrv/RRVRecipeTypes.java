package cc.thonly.reverie_dreams.compat.rrv;

import cc.thonly.reverie_dreams.compat.rrv.danmaku_crafting_table.DanmakuCraftingTableRecipeType;
import cc.thonly.reverie_dreams.compat.rrv.danmaku_shape_draw.DanmakuShapeDrawRecipeType;
import cc.thonly.reverie_dreams.compat.rrv.gensokyo_altar.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.compat.rrv.kitchen.BaseKitchenClientRecipeType;
import cc.thonly.reverie_dreams.compat.rrv.strength_table.StrengthTableRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;

public class RRVRecipeTypes {
    public static final BaseKitchenClientRecipeType COOKING_POT = new BaseKitchenClientRecipeType.CookingPotImpl();
    public static final BaseKitchenClientRecipeType CUTTING_BOARD = new BaseKitchenClientRecipeType.CuttingBoardImpl();
    public static final BaseKitchenClientRecipeType FRYING_PAN = new BaseKitchenClientRecipeType.FryingPanImpl();
    public static final BaseKitchenClientRecipeType GRILL = new BaseKitchenClientRecipeType.GrillImpl();
    public static final BaseKitchenClientRecipeType STEAMER = new BaseKitchenClientRecipeType.SteamerImpl();
    public static final DanmakuCraftingTableRecipeType DANMAKU_CRAFTING_TABLE = new DanmakuCraftingTableRecipeType();
    public static final DanmakuShapeDrawRecipeType DANMAKU_SHAPE_DRAW_RECIPE_TYPE = new DanmakuShapeDrawRecipeType();
    public static final GensokyoAltarRecipeType GENSOKYO_ALTAR_RECIPE_TYPE = new GensokyoAltarRecipeType();
    public static final StrengthTableRecipeType STRENGTH_TABLE_RECIPE_TYPE = new StrengthTableRecipeType();

    public static BaseKitchenClientRecipeType getTypeByRecipe(KitchenRecipe recipe) {
        KitchenRecipeType.TypeInstance typeInstance = recipe.getTypeInstance();
        if (typeInstance.is(KitchenRecipeType.TypeInstance.COOKING_POT)) {
            return COOKING_POT;
        }
        if (typeInstance.is(KitchenRecipeType.TypeInstance.CUTTING_BOARD)) {
            return CUTTING_BOARD;
        }
        if (typeInstance.is(KitchenRecipeType.TypeInstance.FRYING_PAN)) {
            return FRYING_PAN;
        }
        if (typeInstance.is(KitchenRecipeType.TypeInstance.GRILL)) {
            return GRILL;
        }
        if (typeInstance.is(KitchenRecipeType.TypeInstance.STEAMER)) {
            return STEAMER;
        }
        return null;
    }
}
