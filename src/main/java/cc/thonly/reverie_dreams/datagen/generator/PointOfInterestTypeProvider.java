package cc.thonly.reverie_dreams.datagen.generator;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class PointOfInterestTypeProvider extends FabricTagProvider<PoiType> {
    private final Function<PoiType, ResourceKey<PoiType>> valueToKey;

    public PointOfInterestTypeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.POINT_OF_INTEREST_TYPE, registriesFuture);
        this.valueToKey = value -> {
            Optional<ResourceKey<PoiType>> key = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getResourceKey(value);
            return key.orElseGet(() -> ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.parse("none")));
        };
    }
}
