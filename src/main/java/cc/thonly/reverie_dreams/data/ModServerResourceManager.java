package cc.thonly.reverie_dreams.data;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;


public class ModServerResourceManager {
    public static void init() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        helper.registerReloadListener(new ModServerReloadListener());
    }
}
