package cc.thonly.reverie_dreams.data;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;


public class ModServerResourceManager {
    public static void init() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);
        helper.registerReloadListener(new ModServerReloadListener());
    }
}
