package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.ReverieDreams;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

/**
 * TerraBlender region that adds {@code reverie_dreams:maple_forest} to the
 * Overworld using only new parameter points. No vanilla biome is replaced:
 * points outside the maple ranges fall back to the vanilla region via
 * DEFERRED_PLACEHOLDER.
 */
public class MapleForestRegion extends Region {
    public static final ResourceKey<Biome> MAPLE_FOREST = ResourceKey.create(
            Registries.BIOME,
            ReverieDreams.id("maple_forest")
    );

    public MapleForestRegion() {
        super(ReverieDreams.id("maple_forest"), RegionType.OVERWORLD, 4);
    }

    @Override
    public void addBiomes(
            Registry<Biome> registry,
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper
    ) {
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();
        new ParameterUtils.ParameterPointListBuilder()
                .temperature(ParameterUtils.Temperature.NEUTRAL)
                .humidity(ParameterUtils.Humidity.WET)
                .continentalness(ParameterUtils.Continentalness.NEAR_INLAND, ParameterUtils.Continentalness.INLAND)
                .erosion(ParameterUtils.Erosion.EROSION_2)
                .weirdness(
                        ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING,
                        ParameterUtils.Weirdness.MID_SLICE_NORMAL_DESCENDING
                )
                .depth(ParameterUtils.Depth.SURFACE)
                .build()
                .forEach(point -> builder.add(point, MAPLE_FOREST));
        builder.build().forEach(mapper);
    }
}