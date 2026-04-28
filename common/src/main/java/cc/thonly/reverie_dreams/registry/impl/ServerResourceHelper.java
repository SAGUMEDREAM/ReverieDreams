package cc.thonly.reverie_dreams.registry.impl;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.data.CustomCharacterLoader;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.server.CookingInputRecipeManager;
import cc.thonly.reverie_dreams.server.ItemCateManager;
import net.blay09.mods.balm.Balm;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ServerResourceHelper {
    public static void init() {
        Balm.getRuntime().resourceReloadListeners(ReverieDreams.MOD_ID, registrar -> {
            registrar.register("data_reload", ServerResourceHelper::reload);
        });
    }

    public static CompletableFuture<Void> reload(
            PreparableReloadListener.SharedState sharedState,
            Executor prepareExecutor,
            PreparableReloadListener.PreparationBarrier barrier,
            Executor applyExecutor
    ) {
        ResourceManager manager = sharedState.resourceManager();

        return CompletableFuture
                .completedFuture(manager)
                .thenCompose(barrier::wait)
                .thenAcceptAsync((rm) -> {
                    RecipeManager.onReload(rm);
                    for (var entry : RegistryHandlers.ROOT.entrySet()) {
                        RegistryHandler<?> registry = entry.getValue();

                        if (registry.isReloadable()) {
                            registry.reload(rm);
                            RegistryManagerReloadCallback.EVENT.invoker().onLoad(registry);
                        }

                        registry.validate();
                    }
                    CookingInputRecipeManager.getInstance().clearItems();
                    ItemCateManager.getInstance().clearTags();
                    CustomCharacterLoader.reload();

                }, applyExecutor);
    }
}
