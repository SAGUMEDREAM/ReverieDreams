package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.api.entity.entry.RoleGoalEntry;
import cc.thonly.reverie_dreams.api.player.PlayerComponentContextRegistry;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatContext;
import cc.thonly.reverie_dreams.api.registry.RawIdTypeRegistry;
import cc.thonly.reverie_dreams.api.registry.RegistryImplContext;
import cc.thonly.reverie_dreams.api.registry.callback.BeveragePropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public interface ReverieDreamsPlugin {

    String getModId();

    default Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(this.getModId(), path);
    }

    // 通用初始化（逻辑端）
    default void onCommonSetup() {

    }

    // 注册伙伴 AI Goal（向自定义目标注册表中添加行为）
    default void registerRoleGoals(RawIdTypeRegistry<RoleGoalEntry> registry) {

    }

    // 注册食物属性兼容（用于适配其他模组的食物TAG或属性）
    default void registerFoodPropertyCompat(FoodPropertiesLoaderCallback.Context ctx) {

    }

    // 注册酒水属性兼容（用于适配其他模组的饮品TAG或属性）
    default void registerBeveragePropertyCompat(BeveragePropertiesLoaderCallback.Context ctx) {

    }

    // 注册注册表重载监听（当数据包或资源重载时触发）
    default void registerRecipeLoadCallback(RegistryProvider<?> registry) {

    }

    // 注册指定配方类型的重载监听（针对某类配方进行处理）
    default void registerRecipeLoadCallback(BaseRecipeType<?> type) {

    }

    // 食物使用时触发（可添加额外效果或逻辑）
    default void registerUseItemFoodProperty(ServerLevel world, LivingEntity user, ItemStack itemStack, FoodProperty property) {

    }

    // 酒水使用时触发（可添加额外效果或逻辑）
    default void registerUseItemDrinkProperty(ServerLevel world, LivingEntity user, ItemStack itemStack, BeverageProperty property, List<MobEffectInstance> effectInstances, List<MobEffectInstance> negativeEffectInstances) {

    }

    // 注册可替换配方（用于兼容或替换其他模组的配方）
    default void registerReplaceableItemRecipe(RecipeCompatContext context) {

    }

    // 注册玩家组件（为玩家附加自定义数据或能力）
    default void registerPlayerComponent(PlayerComponentContextRegistry registry) {

    }

    // 注册完成后调用（向注册表追加或调整内容）
    default void registerPost(RegistryImplContext context) {

    }

    // 注册自定义皮肤旁加载 Jar 包
    default void registerSkinClasses(Consumer<Class<?>> ctx) {

    }

    // 设置插件启用条件
    default boolean isEnabled() {
        return this.isModLoaded(this.getModId());
    }

    // 返回某模组是否已加载
    default boolean isModLoaded(String modId) {
        return PlatformContext.isModLoaded(modId);
    }

    // 返回是否有本模组的 Polymer 兼容补丁
    default boolean hasPolymer() {
        return PlatformContext.hasPolymer();
    }
}
