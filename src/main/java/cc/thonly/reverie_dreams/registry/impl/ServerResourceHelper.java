package cc.thonly.reverie_dreams.registry.impl;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.data.CustomCharacterLoader;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.server.CookingInputRecipeManager;
import cc.thonly.reverie_dreams.server.ItemCateManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;


public class ServerResourceHelper {
    public static void init() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);
        helper.registerReloadListener(new Listener());
    }

    public static class Listener implements SimpleSynchronousResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return ReverieDreams.id("data");
        }

        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            RecipeManager.onReload(manager);
            for (var entry : RegistryHandlers.ROOT.entrySet()) {
                RegistryHandler<?> registry = entry.getValue();
                if (registry.isReloadable()) {
                    registry.reload(manager);
                    RegistryManagerReloadCallback.EVENT.invoker().onLoad(registry);
                }
                registry.validate();
            }
            this.onLoad(manager);
            CookingInputRecipeManager.getInstance().clearItems();
            ItemCateManager.getInstance().clearTags();
            CustomCharacterLoader.reload();
        }

        public void onLoad(ResourceManager manager) {

        }
    }
}
