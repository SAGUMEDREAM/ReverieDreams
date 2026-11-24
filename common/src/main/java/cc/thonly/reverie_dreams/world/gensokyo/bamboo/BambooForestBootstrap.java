package cc.thonly.reverie_dreams.world.gensokyo.bamboo;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.*;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public final class BambooForestBootstrap {
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, ReverieDreams.id("gensokyo_bamboo_forest"));

    public static String path(BambooForestConfiguration.Mode mode) {
        return "gensokyo/bamboo_forest/" + mode.getSerializedName();
    }

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context, Feature<BambooForestConfiguration> feature) {
        for (var mode : BambooForestConfiguration.Mode.values())
            context.register(GensokyoForestBootstrap.configured(path(mode)), new ConfiguredFeature<>(feature, new BambooForestConfiguration(mode)));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        var lookup = context.lookup(Registries.CONFIGURED_FEATURE);
        for (var mode : BambooForestConfiguration.Mode.values())
            context.register(GensokyoForestBootstrap.placed(path(mode)), new PlacedFeature(
                    lookup.getOrThrow(GensokyoForestBootstrap.configured(path(mode))), List.of(InSquarePlacement.spread(), BiomeFilter.biome())));
    }

    public static void biomes(BootstrapContext<Biome> context) {
        var features = context.lookup(Registries.PLACED_FEATURE);
        var carvers = context.lookup(Registries.CONFIGURED_CARVER);
        Biome template = OverworldBiomes.plains(features, carvers, false, false, false);
        var generation = new BiomeGenerationSettings.PlainBuilder();
        template.getGenerationSettings().getCarvers().forEach(generation::addCarver);
        var steps = template.getGenerationSettings().features();
        for (int step = 0; step < steps.size(); step++)
            if (step != GenerationStep.Decoration.VEGETAL_DECORATION.ordinal())
                for (var feature : steps.get(step))
                    generation.addFeature(step, feature);
        for (var mode : BambooForestConfiguration.Mode.values())
            generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal(), features.getOrThrow(GensokyoForestBootstrap.placed(path(mode))));
        var mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(mobs);
        mobs.addSpawn(MobCategory.CREATURE, 6, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 3));
        context.register(BIOME, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(.85F).downfall(.85F).putAttributes(template.getAttributes())
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xB8CCC0).setAttribute(EnvironmentAttributes.FOG_START_DISTANCE, 6F)
                .setAttribute(EnvironmentAttributes.FOG_END_DISTANCE, 44F).setAttribute(EnvironmentAttributes.SKY_FOG_END_DISTANCE, 64F)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0x527B78).grassColorOverride(0x718456).foliageColorOverride(0x57714C).build())
                .mobSpawnSettings(mobs.build()).generationSettings(generation.build()).build());
    }

    private BambooForestBootstrap() {
    }
}
