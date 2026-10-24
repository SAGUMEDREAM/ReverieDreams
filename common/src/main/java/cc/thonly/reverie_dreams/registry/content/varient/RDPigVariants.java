package cc.thonly.reverie_dreams.registry.content.varient;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.mixin.accessor.PigVariantAccessor;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.variant.ModelAndTexture;

public class RDPigVariants {
    public static final ResourceKey<PigVariant> WILD_PIG = getOrCreateKey("wild_pig");

    public static void bootstrap(BootstrapContext<PigVariant> context) {
        context.register(WILD_PIG, PigVariantAccessor.invokeStaticInit(
                new ModelAndTexture<>(PigVariant.ModelType.NORMAL, ReverieDreams.id("entity/pig/wild_pig")),
                new ClientAsset.ResourceTexture(ReverieDreams.id("entity/pig/wild_pig"), ReverieDreams.id("entity/pig/wild_pig"))
        ));
    }

    public static ResourceKey<PigVariant> getOrCreateKey(String name) {
        return ResourceKey.create(Registries.PIG_VARIANT, ReverieDreams.id(name));
    }
}
