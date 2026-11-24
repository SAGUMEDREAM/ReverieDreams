package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

/**
 * Gives the Overworld variant patches of humid, temperate inland forest.
 */
public final class MagicForestRegion extends Region {
    public MagicForestRegion() {
        super(RDBuiltinBiomes.MAGIC_FOREST.identifier(), RegionType.OVERWORLD, 2);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        var overlay = new VanillaParameterOverlayBuilder();
        new ParameterUtils.ParameterPointListBuilder()
                .temperature(ParameterUtils.Temperature.NEUTRAL)
                .humidity(ParameterUtils.Humidity.WET, ParameterUtils.Humidity.HUMID)
                .continentalness(ParameterUtils.Continentalness.NEAR_INLAND, ParameterUtils.Continentalness.INLAND)
                .erosion(ParameterUtils.Erosion.EROSION_3, ParameterUtils.Erosion.EROSION_4,
                        ParameterUtils.Erosion.EROSION_5)
                .weirdness(ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING,
                        ParameterUtils.Weirdness.MID_SLICE_NORMAL_DESCENDING)
                .depth(ParameterUtils.Depth.SURFACE)
                .build()
                .forEach(point -> overlay.add(point, RDBuiltinBiomes.MAGIC_FOREST));
        overlay.build().forEach(mapper);
    }
}
