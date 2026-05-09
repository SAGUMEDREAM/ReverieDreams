package cc.thonly.reverie_dreams.fabric.datagen.generator;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class PointOfInterestTypeProvider extends FabricTagsProvider<PoiType> {
    private final Function<PoiType, ResourceKey<PoiType>> valueToKey;

    public PointOfInterestTypeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.POINT_OF_INTEREST_TYPE, registriesFuture);
        this.valueToKey = value -> {
            Optional<ResourceKey<PoiType>> key = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getResourceKey(value);
            return key.orElseGet(() -> ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Identifier.parse("none")));
        };
    }

    protected TagAppender<PoiType, PoiType> valueLookupBuilder(TagKey<PoiType> tag) {
        TagBuilder tagBuilder = this.getOrCreateRawBuilder(tag);
        return TagAppender.<PoiType>forBuilder(tagBuilder).map(this.valueToKey);
    }
}
