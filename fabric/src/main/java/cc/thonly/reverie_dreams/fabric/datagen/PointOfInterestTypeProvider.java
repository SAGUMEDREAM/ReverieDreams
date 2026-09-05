package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.villager.RDPointOfInterestTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.concurrent.CompletableFuture;

public class PointOfInterestTypeProvider extends cc.thonly.reverie_dreams.fabric.datagen.generator.PointOfInterestTypeProvider {


    public PointOfInterestTypeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        HolderLookup.RegistryLookup<PoiType> registryWrapper = wrapperLookup.lookupOrThrow(Registries.POINT_OF_INTEREST_TYPE);
        TagAppender<PoiType, PoiType> builder = valueLookupBuilder(PoiTypeTags.ACQUIRABLE_JOB_SITE);
        builder.add(registryWrapper.getOrThrow(RDPointOfInterestTypes.HAWKERS_KEY).value());
        builder.add(registryWrapper.getOrThrow(RDPointOfInterestTypes.PRIEST_KEY).value());
        builder.add(registryWrapper.getOrThrow(RDPointOfInterestTypes.MONEY_SHOP_CLERK_KEY).value());
    }
}
