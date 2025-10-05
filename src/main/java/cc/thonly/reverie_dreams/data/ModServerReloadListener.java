package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import cc.thonly.reverie_dreams.server.CookingInputRecipeManager;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.server.ItemTagManager;
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
        for (var entry : RegistryManager.ROOT.entrySet()) {
            IntrinsicalRegister<?> registry = entry.getValue();
            if (registry.isReloadable()) {
                registry.reload(manager);
                RegistryManagerReloadCallback.EVENT.invoker().onLoad(registry);
            }
            registry.validate();
        }
        this.onLoad(manager);
        CookingInputRecipeManager.getInstance().clearItems();
        ItemTagManager.getInstance().clearTags();
        DialogInit.initRegistriesDialog();
    }

    public void onLoad(ResourceManager manager) {

    }
}
