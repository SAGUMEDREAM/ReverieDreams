package cc.thonly.reverie_dreams.registry.content.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

public final class RDCowVariants {
    public static final ResourceKey<CowVariant> WAGYU = ResourceKey.create(Registries.COW_VARIANT, ReverieDreams.id("wagyu"));

    public static void bootstrap(BootstrapContext<CowVariant> context) {
        context.register(WAGYU, new CowVariant(
                new ModelAndTexture<>(CowVariant.ModelType.NORMAL, ReverieDreams.id("entity/cow/wagyu")),
                new ClientAsset.ResourceTexture(ReverieDreams.id("entity/cow/wagyu_baby")),
                SpawnPrioritySelectors.EMPTY
        ));
    }

    private RDCowVariants() {
    }
}
