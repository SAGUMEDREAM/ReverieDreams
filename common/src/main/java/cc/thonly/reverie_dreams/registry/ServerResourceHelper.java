package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.BookPageManager;
import cc.thonly.reverie_dreams.api.registry.callback.RegistryImplReloadCallback;
import cc.thonly.reverie_dreams.data.CustomSkinLoader;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.server.CookingInputRecipeManager;
import cc.thonly.reverie_dreams.server.ItemCateManager;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ServerResourceHelper {
    public static void init() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, ServerResourceHelper::reload, ReverieDreams.id("data_reload"));
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
                .thenAccept((rm) -> {
                    RecipeManager.onReload(rm);
                    for (var entry : RegistryImpls.ROOT.entrySet()) {
                        RegistryImpl<?> registry = entry.getValue();

                        if (registry.isReloadable()) {
                            registry.reload(rm);
                            RegistryImplReloadCallback.EVENT.invoker().onLoad(registry);
                        }

                        registry.validate();
                    }
                    CookingInputRecipeManager.getInstance().clearItems();
                    ItemCateManager.getInstance().clearTags();
                    BookPageManager.getInstance().reload();
                });
    }
}
