package cc.thonly.reverie_dreams.compat.jei;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.*;
import mezz.jei.api.recipe.types.IRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.WorkshopProcessRecipe;

public class JeiRecipeTypes {
    public static final IRecipeType<WorkshopProcessRecipe> GRINDING = IRecipeType.create(ReverieDreams.id("grinding"), WorkshopProcessRecipe.class);
    public static final IRecipeType<WorkshopProcessRecipe> HERB_DRYING = IRecipeType.create(ReverieDreams.id("herb_drying"), WorkshopProcessRecipe.class);
    public static final IRecipeType<DanmakuRecipe> DANMAKU = IRecipeType.create(RecipeManager.DANMAKU.getId(), DanmakuRecipe.class);
    public static final IRecipeType<DanmakuShapeDrawRecipe> DANMAKU_SHAPE_DRAW = IRecipeType.create(RecipeManager.DANMAKU_SHAPE_DRAW.getId(), DanmakuShapeDrawRecipe.class);
    ;
    public static final IRecipeType<GensokyoAltarRecipe> GENSOKYO_ALTAR = IRecipeType.create(RecipeManager.GENSOKYO_ALTAR.getId(), GensokyoAltarRecipe.class);
    public static final IRecipeType<StrengthTableRecipe> STRENGTH_TABLE = IRecipeType.create(RecipeManager.STRENGTH_TABLE.getId(), StrengthTableRecipe.class);
    public static final IRecipeType<KitchenRecipe> COOKING_POT = IRecipeType.create(ReverieDreams.id("cooking_top"), KitchenRecipe.class);
    public static final IRecipeType<KitchenRecipe> CUTTING_BOARD = IRecipeType.create(ReverieDreams.id("cutting_board"), KitchenRecipe.class);
    public static final IRecipeType<KitchenRecipe> FRYING_PAN = IRecipeType.create(ReverieDreams.id("frying_pan"), KitchenRecipe.class);
    public static final IRecipeType<KitchenRecipe> GRILL = IRecipeType.create(ReverieDreams.id("grill"), KitchenRecipe.class);
    public static final IRecipeType<KitchenRecipe> STEAMER = IRecipeType.create(ReverieDreams.id("streamer"), KitchenRecipe.class);
    public static final IRecipeType<BrewingBarrelRecipe> BREWING = IRecipeType.create(ReverieDreams.id("brewing_barrel"), BrewingBarrelRecipe.class);
}
