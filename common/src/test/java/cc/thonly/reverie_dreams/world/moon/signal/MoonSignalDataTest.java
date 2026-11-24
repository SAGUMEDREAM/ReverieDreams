package cc.thonly.reverie_dreams.world.moon.signal;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MoonSignalDataTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }
    @Test void progressAndTargetsSurviveTheActualSavedDataCodec() {
        var data = new MoonSignalData();
        data.put(-1, new MoonSignalData.Tower(3, 12345, List.of(new MoonSignalData.Target("institute", new BlockPos(-1600, 54, 247)))));
        data.put(7, new MoonSignalData.Tower(1, -1200, List.of()));
        var encoded = MoonSignalData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        var loaded = MoonSignalData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(data.tower(-1), loaded.tower(-1));
        assertEquals(data.tower(7), loaded.tower(7));
        assertTrue(loaded.tower(-1).repaired());
        assertFalse(loaded.tower(7).repaired());
        assertEquals(MoonSignalData.Tower.BROKEN, loaded.tower(999));
    }
    @Test void threeStagesArePersistentIndependentAndCannotAdvanceBeyondRepair() {
        var data = new MoonSignalData();
        for (int stage = 0; stage < 3; stage++) {
            assertEquals(stage, data.tower(10).stage());
            data.put(10, data.tower(10).advance());
            assertEquals(0, data.tower(20).stage());
        }
        assertEquals(data.tower(10), data.tower(10).advance());
        assertTrue(data.isDirty());
    }
    @Test void corruptedRepairStagesAreRejectedRatherThanGrantingAWorkingTower() {
        for (int stage : new int[]{-1, 4, Integer.MAX_VALUE}) {
            var json = JsonParser.parseString("{\"stage\":" + stage + "}");
            assertTrue(MoonSignalData.Tower.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent());
        }
    }
    @Test void scansHaveBoundedDeterministicUniqueCandidatesAtNegativeCoordinates() {
        var placement = new RandomSpreadStructurePlacement(50, 40, RandomSpreadType.LINEAR, 123456789);
        for (var origin : List.of(BlockPos.ZERO, new BlockPos(-801, 40, -1), new BlockPos(799, 10, -1601))) {
            var candidates = MoonSignalScan.candidates(placement, 20261105, origin);
            assertEquals(candidates, MoonSignalScan.candidates(placement, 20261105, origin));
            assertTrue(candidates.size() > 40 && candidates.size() < 160);
            assertEquals(candidates.size(), new HashSet<>(candidates).size());
            long previous = -1;
            for (var candidate : candidates) {
                long distance = MoonSignalScan.horizontalDistanceSquared(origin, candidate.getMiddleBlockPosition(0));
                assertTrue(distance >= previous);
                assertTrue(distance <= (long) (MoonSignalScan.RANGE + 256) * (MoonSignalScan.RANGE + 256));
                previous = distance;
            }
        }
    }
}
