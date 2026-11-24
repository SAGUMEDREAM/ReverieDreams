package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.world.gensokyo.understory.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

/** Synthetic flat-ground density audit using the registered placement modifiers and actual block writes. */
final class GensokyoUnderstoryLandscape {
    record Result(GensokyoTreeFeatureTest.MemoryWorld world, int size,
                  EnumMap<GensokyoUnderstoryConfiguration.Kind, Integer> patches, long woodyColumns, long floorColumns, int largestOpenArea) {}

    static Result generate(GensokyoForestType theme, long seed, int chunks) {
        var registry = GensokyoForestPreviewExporter.registries();
        var world = new GensokyoTreeFeatureTest.MemoryWorld();
        world.maxY = 112;
        world.seed = seed;
        world.biome = registry.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestBootstrap.biome(theme));
        var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(world.biome),
                registry.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        var counts = new EnumMap<GensokyoUnderstoryConfiguration.Kind, Integer>(GensokyoUnderstoryConfiguration.Kind.class);
        for (var kind : GensokyoUnderstoryConfiguration.Kind.values()) counts.put(kind, 0);
        for (int z = 0; z < chunks; z++) for (int x = 0; x < chunks; x++) {
            for (var profile : GensokyoUnderstoryBootstrap.PROFILES) {
                if (profile.theme() != theme) continue;
                var placed = registry.lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(profile.placed()).value();
                var random = RandomSource.create(seed ^ x * 341873128712L ^ z * 132897987541L ^ profile.kind().ordinal() * 92821L);
                var context = new PlacementContext(world.level, generator, Optional.of(placed));
                Stream<BlockPos> positions = Stream.of(new BlockPos(x * 16, 64, z * 16));
                for (var modifier : placed.placement()) positions = positions.flatMap(pos -> modifier.getPositions(context, random, pos));
                for (var pos : positions.toList()) {
                    if (GensokyoUnderstoryFeature.placeNaturalPatch(world.level, pos, GensokyoUnderstoryBootstrap.configuration(profile), random.nextLong()))
                        counts.compute(profile.kind(), (key, count) -> count + 1);
                }
            }
        }
        int size = chunks * 16;
        var within = world.states.entrySet().stream().filter(e -> e.getKey().getX() >= 0 && e.getKey().getX() < size
                && e.getKey().getZ() >= 0 && e.getKey().getZ() < size).toList();
        long woody = within.stream().filter(e -> e.getValue().is(BlockTags.LOGS) || e.getValue().is(BlockTags.LEAVES))
                .map(e -> new BlockPos(e.getKey().getX(), 0, e.getKey().getZ())).distinct().count();
        long floor = within.stream().filter(e -> !e.getValue().is(BlockTags.LOGS) && !e.getValue().is(BlockTags.LEAVES))
                .map(e -> new BlockPos(e.getKey().getX(), 0, e.getKey().getZ())).distinct().count();
        boolean[] blocked = new boolean[size * size];
        within.stream().filter(e -> e.getValue().is(BlockTags.LOGS) || e.getValue().is(BlockTags.LEAVES))
                .forEach(e -> blocked[e.getKey().getZ() * size + e.getKey().getX()] = true);
        int largest = 0;
        int[] queue = new int[blocked.length];
        for (int start = 0; start < blocked.length; start++) {
            if (blocked[start]) continue;
            int head = 0, tail = 0;
            queue[tail++] = start; blocked[start] = true;
            while (head < tail) {
                int p = queue[head++], x = p % size, z = p / size;
                for (int next : new int[]{x > 0 ? p - 1 : -1, x < size - 1 ? p + 1 : -1, z > 0 ? p - size : -1, z < size - 1 ? p + size : -1}) {
                    if (next < 0 || blocked[next]) continue;
                    blocked[next] = true; queue[tail++] = next;
                }
            }
            largest = Math.max(largest, tail);
        }
        return new Result(world, size, counts, woody, floor, largest);
    }
    private GensokyoUnderstoryLandscape() {}
}
