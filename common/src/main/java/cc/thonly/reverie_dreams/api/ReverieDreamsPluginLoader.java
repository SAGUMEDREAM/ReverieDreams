package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.api.entity.callback.CompatGoalAddedCallback;
import cc.thonly.reverie_dreams.api.entity.entry.RoleGoalEntry;
import cc.thonly.reverie_dreams.api.item.callback.ItemEvent;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatContext;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeInjectCallback;
import cc.thonly.reverie_dreams.api.registry.RegistryProviderContext;
import cc.thonly.reverie_dreams.api.registry.callback.BeveragePropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.registry.callback.RegistryProviderReloadCallback;
import cc.thonly.reverie_dreams.registry.content.PlayerComponentRegistry;
import cc.thonly.reverie_dreams.registry.impl.RawIdTypeRegistryImpl;
import cc.thonly.reverie_dreams.util.skin.SkinFetcher;
import dev.architectury.event.Event;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.*;

@Slf4j
public class ReverieDreamsPluginLoader {
    private static final Set<ReverieDreamsPlugin> PLUGINS = new LinkedHashSet<>();

    public enum PluginHook {
        COMMON_SETUP,
        CLIENT_SETUP,
        ROLE_GOAL,
        FOOD_PROPERTY_COMPAT,
        BEVERAGE_PROPERTY_COMPAT,
        REGISTRY_IMPL_RELOAD,
        RECIPE_REGISTER,
        USE_ITEM_FOOD_PROPERTY,
        USE_ITEM_BEVERAGE_PROPERTY,
        REPLACEABLE_RECIPE,
        PLAYER_COMPONENT_REGISTRY,
        REGISTRY_IMPL_REGISTER,
        SKIN_CLASSES_REGISTER;
    }

    private static final Map<PluginHook, List<Runnable>> ACTIONS = new EnumMap<>(PluginHook.class);

    public static void registerPlugin(ReverieDreamsPlugin plugin) {
        PLUGINS.add(plugin);
    }

    public static Optional<ReverieDreamsPlugin> getPlugin(String modId) {
        return PLUGINS.stream()
                .filter(p -> p.getModId().equals(modId))
                .findFirst();
    }

    public static Event<ReverieDreamsExtensionEvents.EntryPoint> getEvent() {
        return ReverieDreamsExtensionEvents.ADD_EVENT;
    }

    public static void run() {
        ReverieDreamsExtensionEvents.ADD_EVENT.invoker().registerPlugin();
        ReverieDreamsExtensionEvents.SCAN_EVENT.invoker().registerPlugin();

        for (ReverieDreamsPlugin plugin : PLUGINS) {
            if (!plugin.isEnabled())
                continue;
            registerPluginHooks(plugin);
        }

        for (Map.Entry<PluginHook, List<Runnable>> entry : ACTIONS.entrySet()) {
            if (entry.getKey() == PluginHook.CLIENT_SETUP)
                continue;
            entry.getValue().forEach(Runnable::run);
        }
    }

    public static void runClientSetup() {
        List<Runnable> clientActions = ACTIONS.get(PluginHook.CLIENT_SETUP);
        log.info("Loaded {} client plugin extensions", clientActions == null ? 0 : clientActions.size());
        if (clientActions != null) {
            clientActions.forEach(Runnable::run);
        }
    }

    private static List<Runnable> actions(PluginHook hook) {
        return ACTIONS.computeIfAbsent(hook, _ -> new ArrayList<>());
    }

    private static void register(PluginHook hook, Runnable action) {
        actions(hook).add(action);
    }

    private static void registerPluginHooks(ReverieDreamsPlugin plugin) {

        register(PluginHook.COMMON_SETUP, () -> {
            plugin.onCommonSetup();
            log.info("Loaded plugin extensions {}", plugin.getModId());
        });

        register(PluginHook.CLIENT_SETUP, () -> {
            plugin.onClientSetup();
            log.info("Loaded client plugin extensions {}", plugin.getModId());
        });

        register(PluginHook.ROLE_GOAL, () -> {
            RawIdTypeRegistryImpl<RoleGoalEntry> registry =
                    new RawIdTypeRegistryImpl<>(plugin.getModId());

            plugin.registerRoleGoals(registry);

            CompatGoalAddedCallback.EVENT.register(entity -> {
                List<Tuple<Integer, Goal>> goals = new ArrayList<>();

                for (Map.Entry<Identifier, RoleGoalEntry> entry :
                        registry.getRegistry().entries()) {

                    RoleGoalEntry e = entry.getValue();
                    goals.add(new Tuple<>(e.prio(), e.function().apply(entity)));
                }

                return goals;
            });
        });

        register(PluginHook.FOOD_PROPERTY_COMPAT,
                () -> FoodPropertiesLoaderCallback.EVENT.register(plugin::registerFoodPropertyCompat));

        register(PluginHook.BEVERAGE_PROPERTY_COMPAT,
                () -> BeveragePropertiesLoaderCallback.EVENT.register(plugin::registerBeveragePropertyCompat));

        register(PluginHook.REGISTRY_IMPL_RELOAD,
                () -> RegistryProviderReloadCallback.EVENT.register(plugin::registerRecipeLoadCallback));

        register(PluginHook.RECIPE_REGISTER,
                () -> RecipeInjectCallback.EVENT.register(plugin::registerRecipeLoadCallback));

        register(PluginHook.USE_ITEM_FOOD_PROPERTY,
                () -> ItemEvent.FOOD_PROPERTY_USE.register(plugin::registerUseItemFoodProperty));

        register(PluginHook.USE_ITEM_BEVERAGE_PROPERTY,
                () -> ItemEvent.BEVERAGE_PROPERTY_USE.register(plugin::registerUseItemDrinkProperty));

        register(PluginHook.REPLACEABLE_RECIPE,
                () -> RecipeCompatPatchesCallback.EVENT.register(() ->
                        plugin.registerReplaceableItemRecipe(
                                new RecipeCompatContext()
                        )
                ));

        register(PluginHook.PLAYER_COMPONENT_REGISTRY,
                () -> plugin.registerPlayerComponent(PlayerComponentRegistry::registerComponentType));

        register(PluginHook.REGISTRY_IMPL_REGISTER,
                () -> plugin.registerPost(RegistryProviderContext.getContext()));

        register(PluginHook.SKIN_CLASSES_REGISTER,
                () -> plugin.registerSkinClasses(SkinFetcher::registerScanClasses));

    }
}
