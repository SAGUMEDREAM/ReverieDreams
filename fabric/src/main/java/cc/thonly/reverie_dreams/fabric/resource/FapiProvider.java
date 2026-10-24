package cc.thonly.reverie_dreams.fabric.resource;

import cc.thonly.reverie_dreams.api.resource.impl.CompositeResourcePack;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

public class FapiProvider {
    @SuppressWarnings("UnstableApiUsage")
    public static void addPacks(CompositeResourcePack pack) {
        ModResourcePackCreator creator = ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER;
        creator.loadPacks(pack::append);
    }
}
