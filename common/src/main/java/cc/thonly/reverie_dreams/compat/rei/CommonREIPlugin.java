//package cc.thonly.reverie_dreams.compat.rei;
//
//import cc.thonly.reverie_dreams.ReverieDreams;
//import cc.thonly.reverie_dreams.compat.rei.display.*;
//import cc.thonly.reverie_dreams.compat.rei.impl.DisplayImpls;
//import cc.thonly.reverie_dreams.recipe.RecipeManager;
//import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
//import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
//import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
//import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
//
//public class CommonREIPlugin implements REICommonPlugin {
//    @Override
//    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
//        registry.register(ReverieDreams.id("danmaku_crafting_table"), DanmakuCraftingTableDisplay.SERIALIZER);
//        registry.register(ReverieDreams.id("danmaku_shape_draw"), DanmakuShapeDrawDisplay.SERIALIZER);
//        registry.register(ReverieDreams.id("gensokyo_altar"), GensokyoAltarRecipeDisplay.SERIALIZER);
//        registry.register(ReverieDreams.id("strength_table"), StrengthTableDisplay.SERIALIZER);
//        registry.register(ReverieDreams.id("kitchen"), KitchenDisplay.SERIALIZER);
//    }
//
//    @Override
//    public void registerDisplays(ServerDisplayRegistry registry) {
//        IDisplayRegisterView view = IDisplayRegisterView.getRecipeRegisters(registry);
//        DisplayImpls.register(view);
//
//    }
//
//}
