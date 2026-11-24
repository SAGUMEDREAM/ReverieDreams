package cc.thonly.reverie_dreams.world.dream;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DreamBlueGridMaskTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void islandsAboveOrBelowAPlaneNeverPunchProjectedHoles() {
        var occupancy = new DreamBlueGridMask.Occupancy(-256, -256);
        for (int y = 80; y <= 184; y++) occupancy.add(new BlockPos(0, y, 0), Blocks.STONE.defaultBlockState());
        assertFalse(occupancy.contains(new BlockPos(0, 64, 0)), "Island overhead leaves lower blue intact");
        assertFalse(occupancy.contains(new BlockPos(0, 224, 0)), "Island below leaves upper blue intact");
    }

    @Test void onlyExactNonAirVoxelsExcludeTheirOwnBlueLayerWithoutWideningTheCut() {
        var occupancy = new DreamBlueGridMask.Occupancy(-256, -256);
        var lower = new BlockPos(0, 64, 0);
        occupancy.add(lower, Blocks.STONE.defaultBlockState());
        occupancy.add(new BlockPos(0, 224, 0), Blocks.AIR.defaultBlockState());
        assertTrue(occupancy.contains(lower));
        assertFalse(occupancy.contains(lower.above()), "No vertical padding");
        assertFalse(occupancy.contains(lower.below()), "No vertical padding");
        assertFalse(occupancy.contains(lower.east()), "No horizontal buffer");
        assertFalse(occupancy.contains(lower.north()), "No short-line cleanup outside real overlap");
        assertFalse(occupancy.contains(new BlockPos(0, 224, 0)), "Lower overlap cannot mask upper blue");
        occupancy.add(new BlockPos(16, 224, -16), Blocks.STONE_SLAB.defaultBlockState());
        assertTrue(occupancy.contains(new BlockPos(16, 224, -16)), "A partial solid block still intersects a full grid cube");
        assertFalse(occupancy.contains(new BlockPos(16, 64, -16)));
    }

    @Test void verticalGapsAndSignedCellEdgesDoNotBecomeSolidBoundingBoxes() {
        var a = new DreamBlueGridMask.Occupancy(-768, -256);
        var b = new DreamBlueGridMask.Occupancy(-768, -256);
        var bottom = new BlockPos(-512, 63, 0);
        var roof = new BlockPos(-512, 65, 0);
        a.add(bottom, Blocks.STONE.defaultBlockState());
        a.add(roof, Blocks.STONE.defaultBlockState());
        b.add(roof, Blocks.STONE.defaultBlockState());
        b.add(bottom, Blocks.STONE.defaultBlockState());
        assertFalse(a.contains(new BlockPos(-512, 64, 0)), "Air gap inside a structure's height bounds remains traversable grid");
        var boundary = new BlockPos(-768, 64, -256);
        a.add(boundary, Blocks.STONE.defaultBlockState());
        b.add(boundary, Blocks.STONE.defaultBlockState());
        for (int x : new int[]{-769, -768, -767, -513, -512, -511, -257, -256}) {
            assertEquals(a.contains(new BlockPos(x, 64, 0)), b.contains(new BlockPos(x, 64, 0)), "Insertion order cannot change a signed slice");
        }
        assertTrue(a.contains(boundary));
        assertFalse(a.contains(boundary.west()));
        assertFalse(a.contains(new BlockPos(-256, 64, -256)), "Neighbor-cell slices stay separate");
    }
}
