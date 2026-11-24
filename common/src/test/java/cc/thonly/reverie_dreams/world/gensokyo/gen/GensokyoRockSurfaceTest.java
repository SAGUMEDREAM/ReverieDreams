package cc.thonly.reverie_dreams.world.gensokyo.gen;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoRockSurfaceTest {
    @BeforeAll static void bootstrap() throws Exception { GensokyoOreGenerationTest.bootstrap(); }

    @Test void exposedRockDoesNotContainRepeatingDirtBandsButGrassRetainsSoil() throws Exception {
        var original = GensokyoOreGenerationTest.map(1084, 0, Map.of());
        var field = GensokyoChunkGenerator.class.getDeclaredField("mapData");
        field.setAccessible(true);
        for (var surface : List.of(Blocks.STONE, Blocks.GRASS_BLOCK)) {
            var map = new GensokyoMapData(
                    original.formatVersion(), original.mapVersion(), original.id(), original.width(), original.depth(),
                    original.originX(), original.originZ(), original.minY(), original.worldHeight(), original.seaLevel(),
                    original.dirtDepth(), original.biomeTransition(), original.spawn(), original.biomePalette(),
                    List.of(surface.defaultBlockState()), original.heights(), original.biomes(), original.surfaces(), original.anchors(), original.underground());
            var generator = new GensokyoChunkGenerator(new FixedBiomeSource(GensokyoOreGenerationTest.plains), map.id());
            field.set(generator, map);
            var column = generator.getBaseColumn(64, 64, LevelHeightAccessor.create(0, 1536), null);
            for (int depth = 1; depth <= 3; depth++)
                assertTrue(column.getBlock(1084 - depth).is(surface == Blocks.STONE ? Blocks.STONE : Blocks.DIRT));
        }
    }
}
