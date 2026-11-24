package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.world.gensokyo.rock.*;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockState;

/** Actual block writes and native placement modifiers on synthetic flat ground, never a game screenshot. */
final class GensokyoRockPreview {
    record Landscape(GensokyoTreeFeatureTest.MemoryWorld world, EnumMap<GensokyoRockConfiguration.Kind, Integer> counts, long area, long rockColumns) {}
    static Landscape landscape(GensokyoForestType theme, long seed, int chunks) {
        var registry = GensokyoForestPreviewExporter.registries();
        var world = new GensokyoTreeFeatureTest.MemoryWorld(); world.maxY = 96; world.seed = seed;
        world.biome = registry.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestBootstrap.biome(theme));
        var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(world.biome), registry.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        var counts = new EnumMap<GensokyoRockConfiguration.Kind, Integer>(GensokyoRockConfiguration.Kind.class);
        for (var kind : GensokyoRockConfiguration.Kind.values()) counts.put(kind, 0);
        for (int z = 0; z < chunks; z++) for (int x = 0; x < chunks; x++) for (var profile : GensokyoRockBootstrap.PROFILES) {
            if (profile.theme() != theme) continue;
            var placed = registry.lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(profile.placed()).value();
            var random = RandomSource.create(seed ^ x * 341873128712L ^ z * 132897987541L ^ profile.kind().ordinal() * 92821L);
            var context = new PlacementContext(world.level, generator, Optional.of(placed));
            Stream<BlockPos> positions = Stream.of(new BlockPos(x * 16, 64, z * 16));
            for (var modifier : placed.placement()) positions = positions.flatMap(pos -> modifier.getPositions(context, random, pos));
            for (var pos : positions.toList()) if (GensokyoRockFeature.placeRock(world.level, pos, GensokyoRockBootstrap.configuration(profile), random.nextLong(), true)) counts.compute(profile.kind(), (k, n) -> n + 1);
        }
        int size = chunks * 16;
        long columns = world.states.entrySet().stream().filter(e -> GensokyoRockTest.isRock(e.getValue()) && e.getKey().getX() >= 0 && e.getKey().getX() < size && e.getKey().getZ() >= 0 && e.getKey().getZ() < size)
                .map(e -> new BlockPos(e.getKey().getX(), 0, e.getKey().getZ())).distinct().count();
        return new Landscape(world, counts, (long) size * size, columns);
    }
    static void export(Path directory) throws IOException {
        Files.createDirectories(directory);
        var samples = new JsonArray();
        for (var profile : GensokyoRockBootstrap.PROFILES) for (int n = 0; n < 3; n++) {
            long seed = 20261105L + 104729L * n;
            var world = new GensokyoTreeFeatureTest.MemoryWorld(); world.maxY = 96;
            if (!GensokyoRockFeature.placeRock(world.level, new BlockPos(0, 64, 0), GensokyoRockBootstrap.configuration(profile), seed, false)) throw new IllegalStateException("Rock preview failed: " + profile);
            var sample = new JsonObject(); sample.addProperty("theme", profile.theme().name()); sample.addProperty("kind", profile.kind().name()); sample.addProperty("seed", seed); sample.addProperty("rarity", profile.rarity());
            var voxels = new JsonArray();
            world.states.entrySet().stream().sorted(Comparator.comparingInt((Map.Entry<BlockPos, BlockState> e) -> e.getKey().getY()).thenComparingInt(e -> e.getKey().getX()).thenComparingInt(e -> e.getKey().getZ())).forEach(e -> {
                var cell = new JsonArray(); cell.add(e.getKey().getX()); cell.add(e.getKey().getY() - 64); cell.add(e.getKey().getZ()); cell.add(BuiltInRegistries.BLOCK.getKey(e.getValue().getBlock()).toString()); cell.add(GensokyoRockTest.isRock(e.getValue())); voxels.add(cell);
            });
            sample.add("voxels", voxels); samples.add(sample);
        }
        write(directory.resolve("rock-geometry.json"), samples);
        var density = new JsonArray();
        for (var theme : GensokyoForestType.values()) {
            var result = landscape(theme, 20261105L, 16);
            var sample = new JsonObject(); sample.addProperty("theme", theme.name()); sample.addProperty("area", result.area()); sample.addProperty("rock_columns", result.rockColumns());
            var counts = new JsonObject(); result.counts().forEach((k, n) -> counts.addProperty(k.name(), n)); sample.add("counts", counts); density.add(sample);
        }
        write(directory.resolve("rock-density.json"), density);
    }
    private static void write(Path path, JsonElement json) throws IOException { Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(json) + "\n"); }
}
