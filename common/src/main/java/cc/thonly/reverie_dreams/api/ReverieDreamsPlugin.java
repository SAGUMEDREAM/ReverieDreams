package cc.thonly.reverie_dreams.api;

import cc.thonly.keine.api.loot.LootTableSource;
import cc.thonly.keine.api.registry.DynamicRegistryView;
import cc.thonly.reverie_dreams.api.entity.entry.RoleGoalEntry;
import cc.thonly.reverie_dreams.api.player.PlayerComponentContextRegistry;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatContext;
import cc.thonly.reverie_dreams.api.registry.RawIdTypeRegistry;
import cc.thonly.reverie_dreams.api.registry.RegistryImplContext;
import cc.thonly.reverie_dreams.api.registry.callback.DrinkPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

import java.util.List;

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
    default void registerDrinkPropertyCompat(DrinkPropertiesLoaderCallback.Context ctx) {

    }

    // 注册注册表重载监听（当数据包或资源重载时触发）
    default void registerRecipeLoadCallback(RegistryImpl<?> registry) {

    }

    // 注册指定配方类型的重载监听（针对某类配方进行处理）
    default void registerRecipeLoadCallback(BaseRecipeType<?> type) {

    }

    // 食物使用时触发（可添加额外效果或逻辑）
    default void registerUseItemFoodProperty(ServerLevel world, LivingEntity user, FoodProperty property) {

    }

    // 酒水使用时触发（可添加额外效果或逻辑）
    default void registerUseItemDrinkProperty(ServerLevel world, LivingEntity user, DrinkProperty property) {

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

    // 注册动态数据包注册表（用于运行时注入动态内容）
    default void registerDynamicRegistries(DynamicRegistryView view) {

    }

    // 当战利品表加载完成后调用（可用于缓存或预处理）
    default void onLootTablesLoaded(ResourceManager resourceManager, Registry<LootTable> registry) {

    }

    // 替换指定战利品表（返回新表，返回 null 表示不替换）
    default @Nullable LootTable replaceLootTable(ResourceKey<LootTable> key, LootTable builder, LootTableSource source, HolderLookup.Provider provider) {
        return null;
    }

    // 修改战利品表构建器（在生成前对表内容进行调整）
    default void modifyLootTable(ResourceKey<LootTable> key, LootTable.Builder builder, LootTableSource source, HolderLookup.Provider provider) {

    }

    // 修改最终掉落物（在生成掉落后对结果列表进行编辑）
    default void modifyLootTableDrops(Holder<LootTable> key, LootContext context, List<ItemStack> drops) {

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
