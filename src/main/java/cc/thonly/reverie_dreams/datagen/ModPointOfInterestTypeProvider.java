package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.datagen.generator.PointOfInterestTypeProvider;
import cc.thonly.reverie_dreams.entity.villager.ModPointOfInterestTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.concurrent.CompletableFuture;

public class ModPointOfInterestTypeProvider extends PointOfInterestTypeProvider {


    public ModPointOfInterestTypeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        RegistryWrapper.Impl<PointOfInterestType> registryWrapper = wrapperLookup.getOrThrow(RegistryKeys.POINT_OF_INTEREST_TYPE);
        ProvidedTagBuilder<PointOfInterestType, PointOfInterestType> builder = valueLookupBuilder(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE);
        builder.add(registryWrapper.getOrThrow(ModPointOfInterestTypes.HAWKERS).value());
        builder.add(registryWrapper.getOrThrow(ModPointOfInterestTypes.PRIEST).value());
        builder.add(registryWrapper.getOrThrow(ModPointOfInterestTypes.MONEY_SHOP_CLERK).value());
    }
}
