package cc.thonly.reverie_dreams.api.plugin;

import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.registry.RawIdTypeRegistry;
import cc.thonly.reverie_dreams.api.registry.callback.DrinkPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.entity.entry.RoleGoalEntry;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface ReverieDreamsPlugin {

    String getModId();

    default Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(this.getModId(), path);
    }

    default void onCommonSetup() {

    }

    // 注册伙伴 Goal
    default void registerRoleGoals(RawIdTypeRegistry<RoleGoalEntry> registry) {

    }

    // 注册食物TAG兼容
    default void registerFoodPropertyCompat(FoodPropertiesLoaderCallback.Context ctx) {

    }

    // 注册酒水TAG兼容
    default void registerDrinkPropertyCompat(DrinkPropertiesLoaderCallback.Context ctx) {

    }

    // 注册模组注册表重载监听
    default void registerRecipeLoadCallback(RegistryImpl<?> registry) {

    }

    // 注册自定义模组配方项编辑
    default void registerRecipeLoadCallback(BaseRecipeType<?> type) {

    }

    // 注册食物使用效果
    default void registerUseItemFoodProperty(ServerLevel world, LivingEntity user, FoodProperty property) {

    }

    // 注册酒水使用效果
    default void registerUseItemDrinkProperty(ServerLevel world, LivingEntity user, DrinkProperty property) {

    }

    // 注册配方平替兼容
    default void registerReplaceableRecipe(RecipeCompatPatchesCallback.Helper helper) {

    }

    // 设置启用条件
    default boolean isEnabled() {
        return true;
    }
}
