package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoLodSamplerTest {
    @BeforeAll static void bootstrap() throws Exception { GensokyoOreGenerationTest.bootstrap(); }

    private static GensokyoChunkGenerator generator(GensokyoMapData map) throws Exception {
        var generator = new GensokyoChunkGenerator(new FixedBiomeSource(GensokyoOreGenerationTest.plains), map.id());
        setMapData(generator, map);
        return generator;
    }

    private static void setMapData(Object target, GensokyoMapData map) throws Exception {
        var field = target.getClass().getDeclaredField("mapData");
        field.setAccessible(true);
        field.set(target, map);
    }

    @Test void spansCoverFullHeightAndMatchGeneratedChunks() throws Exception {
        var map = GensokyoOreGenerationTest.map(1050, 0, Map.of());
        map.surfaces()[map.index(50, 50)] = 1;
        map.surfaces()[map.index(60, 60)] = 2;
        map.heights()[map.index(70, 70)] = 1010;
        map.heights()[map.index(90, 90)] = GensokyoMapData.NO_SURFACE;
        var generator = generator(map);
        var sampler = new GensokyoLodSampler(generator);
        var accessor = LevelHeightAccessor.create(generator.getMinY(), generator.getGenDepth());
        assertEquals(accessor.getMinY(), sampler.minY());
        assertEquals(accessor.getHeight(), sampler.height());
        var factory = PalettedContainerFactory.create(GensokyoOreGenerationTest.registries);
        for (var position : List.of(new ChunkPos(3, 3), new ChunkPos(5, 5), new ChunkPos(-1, 4))) {
            var chunk = chunk(position, accessor, factory);
            generator.fillFromNoise(null, null, null, chunk).join();
            for (int localX = 0; localX < 16; localX += 5) {
                for (int localZ = 0; localZ < 16; localZ += 5) {
                    int x = position.getMinBlockX() + localX;
                    int z = position.getMinBlockZ() + localZ;
                    var column = sampler.sample(x, z);
                    int expectedBottom = accessor.getMinY();
                    for (var span : column.spans()) {
                        assertEquals(expectedBottom, span.bottomY(), "contiguous spans at " + x + "," + z);
                        assertTrue(span.topY() > span.bottomY(), "empty span at " + x + "," + z);
                        assertTrue(span.topY() - span.bottomY() <= 6095, "DH height limit at " + x + "," + z);
                        for (int y = span.bottomY(); y < span.topY(); y++) {
                            assertEquals(chunk.getBlockState(new BlockPos(x, y, z)), span.state(),
                                    "block at " + x + "," + y + "," + z);
                        }
                        expectedBottom = span.topY();
                    }
                    assertEquals(accessor.getMinY() + accessor.getHeight(), expectedBottom, "coverage at " + x + "," + z);
                }
            }
        }
    }

    @Test void voidColumnsStayAirAndWaterColumnsKeepFluidSpans() throws Exception {
        var map = GensokyoOreGenerationTest.map(1050, 0, Map.of());
        map.heights()[map.index(90, 90)] = GensokyoMapData.NO_SURFACE;
        map.surfaces()[map.index(50, 50)] = 1;
        var sampler = new GensokyoLodSampler(generator(map));

        var voidColumn = sampler.sample(90, 90);
        assertEquals(1, voidColumn.spans().size());
        assertTrue(voidColumn.spans().getFirst().state().isAir());
        assertEquals(0, voidColumn.spans().getFirst().bottomY());
        assertEquals(1536, voidColumn.spans().getFirst().topY());

        var waterColumn = sampler.sample(50, 50);
        assertTrue(waterColumn.spans().stream().anyMatch(span ->
                span.state().is(Blocks.WATER) && span.bottomY() == 1050 && span.topY() == 1051));
    }

    @Test void biomeFollowsMapInteriorAndBorderBands() throws Exception {
        var original = GensokyoOreGenerationTest.map(1050, 0, Map.of());
        var names = List.of("minecraft:river", "reverie_dreams:gensokyo_forest", "minecraft:windswept_forest",
                "minecraft:meadow", "minecraft:stony_peaks", "minecraft:frozen_peaks");
        var indices = new byte[128 * 128];
        Arrays.fill(indices, (byte) 1);
        var map = new GensokyoMapData(1, 2, original.id(), 128, 128, 0, 0, 0, 1536, 1024, 3, 0,
                original.spawn(), names.stream().map(Identifier::parse).toList(), original.surfacePalette(),
                original.heights(), indices, original.surfaces(), List.of(), Map.of());
        var holders = names.stream()
                .map(name -> Holder.direct(GensokyoOreGenerationTest.biome(BiomeGenerationSettings.EMPTY))).toList();
        var source = new GensokyoBiomeSource(map.id(), holders);
        setMapData(source, map);
        var generator = new GensokyoChunkGenerator(source, map.id());
        setMapData(generator, map);
        var sampler = new GensokyoLodSampler(generator);

        assertSame(holders.get(1), sampler.sample(32, 32).biome());
        assertSame(holders.get(5), sampler.sample(-600, 32).biome(),
                "border column at quart " + QuartPos.fromBlock(-600));
    }

    private static ProtoChunk chunk(ChunkPos position, LevelHeightAccessor accessor, PalettedContainerFactory factory) {
        var sections = new LevelChunkSection[accessor.getSectionsCount()];
        for (int i = 0; i < sections.length; i++) sections[i] = new LevelChunkSection(
                new PalettedContainer<>(Blocks.AIR.defaultBlockState(), factory.blockStatesStrategy()), factory.createForBiomes());
        return new ProtoChunk(position, UpgradeData.EMPTY, sections, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(),
                accessor, factory, null);
    }
}
