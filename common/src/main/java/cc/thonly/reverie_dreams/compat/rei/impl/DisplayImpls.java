//package cc.thonly.reverie_dreams.compat.rei.impl;
//
//import cc.thonly.reverie_dreams.compat.rei.IDisplayRegisterView;
//import cc.thonly.reverie_dreams.compat.rei.display.*;
//import cc.thonly.reverie_dreams.recipe.RecipeManager;
//import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
//
//public class DisplayImpls {
//    public static void register(IDisplayRegisterView view) {
//        view.registerType(
//                RecipeManager.DANMAKU,
//                DanmakuCraftingTableDisplay::new
//        );
//        view.registerType(
//                RecipeManager.DANMAKU_SHAPE_DRAW,
//                DanmakuShapeDrawDisplay::new
//        );
//        view.registerType(
//                RecipeManager.GENSOKYO_ALTAR,
//                GensokyoAltarRecipeDisplay::new
//        );
//        view.register(
//                StrengthTableRecipe.createRecipeList(),
//                StrengthTableDisplay::new
//        );
//        view.registerType(
//                RecipeManager.KITCHEN_TYPE,
//                KitchenDisplay::new
//        );
//    }
//}
