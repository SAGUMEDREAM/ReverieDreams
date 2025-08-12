package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ModServerReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Touhou.id("data");
    }

    @Override
    public void reload(ResourceManager manager) {
        RecipeManager.onReload(manager);
        for (var entry : RegistryManager.REGISTRIES.entrySet()) {
            StandaloneRegistry<?> registry = entry.getValue();
            if (registry.isReloadable() && !registry.getReloadableBuilders().isEmpty()) {
                registry.reset();
                registry.getReloadableBuilders().forEach(boot -> boot.reload(manager));
                RegistryManagerReloadCallback.EVENT.invoker().onLoad(registry);
            }
            registry.verify();
        }
        this.onLoad(manager);
    }

    public void onLoad(ResourceManager manager) {

    }
}
