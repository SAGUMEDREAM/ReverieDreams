package cc.thonly.reverie_dreams.datagen.generator;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class PointOfInterestTypeProvider extends FabricTagProvider<PointOfInterestType> {
    private final Function<PointOfInterestType, RegistryKey<PointOfInterestType>> valueToKey;

    public PointOfInterestTypeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.POINT_OF_INTEREST_TYPE, registriesFuture);
        this.valueToKey = value -> {
            Optional<RegistryKey<PointOfInterestType>> key = Registries.POINT_OF_INTEREST_TYPE.getKey(value);
            return key.orElseGet(() -> RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, Identifier.of("none")));
        };
    }

    protected ProvidedTagBuilder<PointOfInterestType, PointOfInterestType> valueLookupBuilder(TagKey<PointOfInterestType> tag) {
        TagBuilder tagBuilder = this.getTagBuilder(tag);
        return ProvidedTagBuilder.<PointOfInterestType>of(tagBuilder).mapped(this.valueToKey);
    }
}
