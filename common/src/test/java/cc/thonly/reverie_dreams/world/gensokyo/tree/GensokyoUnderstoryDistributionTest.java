package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.world.gensokyo.understory.*;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.EnumMap;

import static cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryConfiguration.Kind.*;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoUnderstoryDistributionTest {
    @BeforeAll static void setup() { GensokyoForestPreviewExporter.registries(); VanillaTreeTags.bind(); }

    @Test void scalesShareClearingsAndSeededHabitatIsSmoothAcrossChunkAndNegativeBoundaries() {
        int clear = 0, occupied = 0, changedSeed = 0;
        for (int x = -256; x < 256; x += 3) for (int z = -256; z < 256; z += 3) {
            double value = GensokyoUnderstoryDistribution.habitat(42, x, z);
            assertTrue(value >= 0 && value <= 1);
            assertTrue(Math.abs(value - GensokyoUnderstoryDistribution.habitat(42, x + 1, z)) < .06);
            boolean low = GensokyoUnderstoryDistribution.allows(GensokyoForestType.FOREST, LEAF_PATCH, 42, x, z);
            boolean shrub = GensokyoUnderstoryDistribution.allows(GensokyoForestType.FOREST, SHRUB, 42, x, z);
            boolean thicket = GensokyoUnderstoryDistribution.allows(GensokyoForestType.FOREST, THICKET, 42, x, z);
            assertFalse(shrub && !low); assertFalse(thicket && !shrub);
            if (low) occupied++; else clear++;
            if (low != GensokyoUnderstoryDistribution.allows(GensokyoForestType.FOREST, LEAF_PATCH, 43, x, z)) changedSeed++;
        }
        assertTrue(clear > 5000 && occupied > 5000 && changedSeed > 5000);
    }

    @Test void naturalBudgetsLeaveMostGroundOpenAndAiryBiomesAreSparser() {
        var coverage = new EnumMap<GensokyoForestType, Double>(GensokyoForestType.class);
        for (var theme : GensokyoForestType.values()) {
            var result = GensokyoUnderstoryLandscape.generate(theme, 20261105, 8);
            double cover = (double) result.woodyColumns() / (result.size() * result.size());
            coverage.put(theme, cover);
            assertTrue(cover > .003 && cover < .18, theme + " woody coverage=" + cover);
            assertTrue(result.floorColumns() < result.size() * result.size() * .09, theme.toString());
            assertTrue(result.largestOpenArea() > (result.size() * result.size() - result.woodyColumns()) * .9,
                    theme + " open areas should stay connected");
            assertTrue(result.patches().get(LEAF_PATCH) > result.patches().get(THICKET));
            assertTrue(result.patches().get(SHRUB) >= result.patches().get(THICKET));
        }
        assertTrue(coverage.get(GensokyoForestType.MAGIC_FOREST) > coverage.get(GensokyoForestType.BIRCH_FOREST) * 2);
        assertTrue(coverage.get(GensokyoForestType.DARK_FOREST) > coverage.get(GensokyoForestType.CHERRY_GROVE) * 2);
    }

    @Test void openGroundDoesNotReceiveLeafLitterAndMapleLitterMatchesTheOverheadLeaves() {
        var profile = new GensokyoUnderstoryBootstrap.Profile(GensokyoForestType.MAPLE_FOREST, FLOOR_PATCH);
        var config = GensokyoUnderstoryBootstrap.configuration(profile);
        BlockPos pos = allowed(profile);
        var open = new GensokyoTreeFeatureTest.MemoryWorld();
        assertTrue(GensokyoUnderstoryFeature.placeNaturalPatch(open.level, pos, config, 5));
        assertTrue(open.states.values().stream().noneMatch(s -> config.litter().stream().anyMatch(l -> l.is(s.getBlock()))));
        for (int palette = 0; palette < 3; palette++) {
            var shade = new GensokyoTreeFeatureTest.MemoryWorld();
            for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++) shade.states.put(pos.offset(x, 12, z), config.leaves().get(palette));
            assertTrue(GensokyoUnderstoryFeature.placeNaturalPatch(shade.level, pos, config, 5));
            var expected = config.litter().get(palette);
            assertTrue(shade.states.values().stream().anyMatch(s -> s.is(expected.getBlock())));
            assertTrue(shade.states.values().stream().filter(s -> config.litter().stream().anyMatch(l -> l.is(s.getBlock())))
                    .allMatch(s -> s.is(expected.getBlock())));
        }
    }

    @Test void naturalShrubsLeaveGapsAroundLowVegetationAndAvoidSteepBanks() {
        var profile = new GensokyoUnderstoryBootstrap.Profile(GensokyoForestType.FOREST, SHRUB);
        BlockPos pos = allowed(profile);
        var config = GensokyoUnderstoryBootstrap.configuration(profile);
        var adjacent = new GensokyoTreeFeatureTest.MemoryWorld();
        adjacent.states.put(pos.offset(3, 0, 0), Blocks.OAK_LEAVES.defaultBlockState());
        assertFalse(GensokyoUnderstoryFeature.placeNaturalPatch(adjacent.level, pos, config, 1));
        assertEquals(0, adjacent.writes);
        var steep = new GensokyoTreeFeatureTest.MemoryWorld();
        steep.terrain = (x, z) -> x >= pos.getX() + 2 ? 66 : 63;
        assertFalse(GensokyoUnderstoryFeature.placeNaturalPatch(steep.level, pos, config, 1));
        assertEquals(0, steep.writes);
        var shaded = new GensokyoTreeFeatureTest.MemoryWorld();
        for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++) shaded.states.put(pos.offset(x, 12, z), Blocks.OAK_LEAVES.defaultBlockState());
        assertTrue(GensokyoUnderstoryFeature.placeNaturalPatch(shaded.level, pos, config, 1), "High crowns allow understory growth");
        assertTrue(shaded.states.entrySet().stream().filter(e -> e.getKey().getY() < 70).anyMatch(e -> e.getValue().is(BlockTags.LOGS)));
    }

    private static BlockPos allowed(GensokyoUnderstoryBootstrap.Profile profile) {
        for (int x = 0; x < 1024; x += 8) for (int z = 0; z < 1024; z += 8)
            if (GensokyoUnderstoryDistribution.allows(profile.theme(), profile.kind(), 20261105, x, z)) return new BlockPos(x, 64, z);
        throw new AssertionError("No habitat");
    }
}
