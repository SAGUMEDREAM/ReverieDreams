package cc.thonly.reverie_dreams.registry.content.painting;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

public class RDPaintingVariants {
    public static void bootstrap(BootstrapContext<PaintingVariant> context) {

    }

    public static ResourceKey<PaintingVariant> getOrCreateKey(String name) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, ReverieDreams.id(name));
    }
}
