package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.api.entity.callback.CompatGoalAddedCallback;
import cc.thonly.reverie_dreams.api.item.callback.DrinkPropertyItemUseCallback;
import cc.thonly.reverie_dreams.api.item.callback.FoodPropertyItemUseCallback;
import cc.thonly.reverie_dreams.api.plugin.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeInjectCallback;
import cc.thonly.reverie_dreams.api.registry.callback.DrinkPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.registry.callback.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.api.entity.entry.RoleGoalEntry;
import cc.thonly.reverie_dreams.registry.impl.RawIdTypeRegistryImpl;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.platform.event.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.*;

@Slf4j
public class ReverieDreamsPluginLoader {
    private static final Set<ReverieDreamsPlugin> PLUGINS = new LinkedHashSet<>();
    private static final Map<String, List<Runnable>> DEF_ACTIONS = new HashMap<>();
    public static final String COMMON_SETUP = "COMMON_SETUP";
    public static final String ROLE_GOAL = "ROLE_GOAL";
    public static final String FOOD_PROPERTY_COMPAT = "FOOD_PROPERTY_COMPAT";
    public static final String DRINK_PROPERTY_COMPAT = "DRINK_PROPERTY_COMPAT";
    public static final String REGISTRY_IMPL_RELOAD = "REGISTRY_IMPL_RELOAD";
    public static final String RECIPE_REGISTER = "RECIPE_REGISTER";
    public static final String USE_ITEM_FOOD_PROPERTY = "USE_ITEM_FOOD_PROPERTY";
    public static final String USE_ITEM_DRINK_PROPERTY = "USE_ITEM_DRINK_PROPERTY";
    public static final String REPLACEABLE_RECIPE = "REPLACEABLE_RECIPE";

    public static void registerPlugin(ReverieDreamsPlugin plugin) {
        PLUGINS.add(plugin);
    }

    public static void run() {
        ReverieDreamsExtensionEvents.EVENT.invoker().registerPlugin();
        List<Runnable> common = getDeferAction(COMMON_SETUP);
        List<Runnable> role_goal = getDeferAction(ROLE_GOAL);
        List<Runnable> food_property_compat = getDeferAction(FOOD_PROPERTY_COMPAT);
        List<Runnable> drink_property_compat = getDeferAction(DRINK_PROPERTY_COMPAT);
        List<Runnable> registry_impl_reload = getDeferAction(REGISTRY_IMPL_RELOAD);
        List<Runnable> recipe_register = getDeferAction(RECIPE_REGISTER);
        List<Runnable> use_item_food_property = getDeferAction(USE_ITEM_FOOD_PROPERTY);
        List<Runnable> use_item_drink_property = getDeferAction(USE_ITEM_DRINK_PROPERTY);
        List<Runnable> replaceable_recipe = getDeferAction(REPLACEABLE_RECIPE);

        for (ReverieDreamsPlugin plugin : PLUGINS) {
            if (!plugin.isEnabled()) {
                continue;
            }
            common.add(() -> {
                plugin.onCommonSetup();
                log.info("Loaded plugin extensions {}", plugin.getModId());
            });
            role_goal.add(() -> {
                RawIdTypeRegistryImpl<RoleGoalEntry> roleGoalEntry = new RawIdTypeRegistryImpl<>(plugin.getModId());
                plugin.registerRoleGoals(roleGoalEntry);
                CompatGoalAddedCallback.EVENT.register(entity -> {
                    List<Tuple<Integer, Goal>> goals = new ArrayList<>();
                    for (Map.Entry<Identifier, RoleGoalEntry> mapEntry : roleGoalEntry.getRegistry().entries()) {
                        RoleGoalEntry entry = mapEntry.getValue();
                        goals.add(new Tuple<>(entry.prio(), entry.function().apply(entity)));
                    }
                    return goals;
                });
            });
            food_property_compat.add(() -> {
                FoodPropertiesLoaderCallback.EVENT.register(plugin::registerFoodPropertyCompat);
            });
            drink_property_compat.add(() -> {
                DrinkPropertiesLoaderCallback.EVENT.register(plugin::registerDrinkPropertyCompat);
            });
            registry_impl_reload.add(() -> {
                RegistryManagerReloadCallback.EVENT.register(plugin::registerRecipeLoadCallback);
            });
            recipe_register.add(() -> {
                RecipeInjectCallback.EVENT.register(plugin::registerRecipeLoadCallback);
            });
            use_item_food_property.add(() -> {
                FoodPropertyItemUseCallback.EVENT.register(plugin::registerUseItemFoodProperty);
            });
            use_item_drink_property.add(() -> {
                DrinkPropertyItemUseCallback.EVENT.register(plugin::registerUseItemDrinkProperty);
            });
            replaceable_recipe.add(() -> {
                RecipeCompatPatchesCallback.EVENT.register(() -> {
                    plugin.registerReplaceableRecipe(new RecipeCompatPatchesCallback.Helper());
                });
            });
        }
        common.forEach(Runnable::run);
        role_goal.forEach(Runnable::run);
        food_property_compat.forEach(Runnable::run);
        drink_property_compat.forEach(Runnable::run);
        registry_impl_reload.forEach(Runnable::run);
        recipe_register.forEach(Runnable::run);
        use_item_food_property.forEach(Runnable::run);
        use_item_drink_property.forEach(Runnable::run);
        replaceable_recipe.forEach(Runnable::run);

    }

    public static Event<ReverieDreamsExtensionEvents.EntryPoint> getEvent() {
        return ReverieDreamsExtensionEvents.EVENT;
    }

    public static List<Runnable> getDeferAction(String type) {
        return DEF_ACTIONS.computeIfAbsent(type, _ -> new ArrayList<>());
    }

    public static Optional<ReverieDreamsPlugin> getPlugin(String modId) {
        return PLUGINS.stream().filter(plugin -> plugin.getModId().equals(modId)).findFirst();
    }
}
