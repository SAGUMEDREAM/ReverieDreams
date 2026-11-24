package cc.thonly.reverie_dreams.world.dream;

import cc.thonly.reverie_dreams.world.dream.scene.DreamScene;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DreamChestTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    private static Map<BlockPos, BlockState> fixture() {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        blocks.put(BlockPos.ZERO, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        blocks.put(BlockPos.ZERO.below(), Blocks.STONE.defaultBlockState());
        blocks.put(BlockPos.ZERO.south().below(), Blocks.STONE.defaultBlockState());
        return blocks;
    }

    @Test void lootMarkersAreImmutableSnapshotsOfPositionsAndTables() {
        var blocks = fixture();
        var mutable = new BlockPos.MutableBlockPos();
        var markers = new HashMap<BlockPos, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>>();
        markers.put(mutable, DreamChestLoot.SUPPLY);
        var scene = new DreamScene("luggage", blocks, new BlockPos(0, 0, 1), Map.of(), markers);
        mutable.set(9, 9, 9);
        markers.clear(); blocks.clear();
        assertEquals(Map.of(BlockPos.ZERO, DreamChestLoot.SUPPLY), scene.lootChests());
        assertThrows(UnsupportedOperationException.class, () -> scene.lootChests().clear());
    }

    @Test void invalidOrBlockedChestLocationsAreRejected() {
        for (BlockPos blocked : new BlockPos[]{BlockPos.ZERO.above(), BlockPos.ZERO.south(), BlockPos.ZERO.south().above()}) {
            var blocks = fixture(); blocks.put(blocked, Blocks.STONE.defaultBlockState());
            assertThrows(IllegalArgumentException.class, () -> new DreamScene("blocked", blocks, BlockPos.ZERO,
                    Map.of(), Map.of(BlockPos.ZERO, DreamChestLoot.SUPPLY)));
        }
        for (BlockPos missing : new BlockPos[]{BlockPos.ZERO, BlockPos.ZERO.below(), BlockPos.ZERO.south().below()}) {
            var blocks = fixture(); blocks.remove(missing);
            assertThrows(IllegalArgumentException.class, () -> new DreamScene("unsupported", blocks, BlockPos.ZERO,
                    Map.of(), Map.of(BlockPos.ZERO, DreamChestLoot.SUPPLY)));
        }
    }

    @Test void seededLootVariesByWorldSignedPositionAndThemeWithoutZeroSentinel() {
        var seeds = new HashSet<Long>();
        for (long world : new long[]{0, 1, -1, Long.MIN_VALUE, Long.MAX_VALUE})
            for (int x : new int[]{-513, -512, -17, -16, -1, 0, 15, 16, 511, 512})
                for (var table : DreamChestLoot.TABLES) {
                    BlockPos pos = new BlockPos(x, 129, -x);
                    long seed = DreamChestLoot.seed(world, pos, table);
                    assertNotEquals(0, seed);
                    assertTrue(seeds.add(seed));
                    assertEquals(seed, DreamChestLoot.seed(world, pos.immutable(), table));
                }
        assertEquals(250, seeds.size());
    }

    @Test void oldScenesNeedNoLootAndNeverAcquireAnImplicitReward() {
        var scene = new DreamScene("legacy", Map.of(BlockPos.ZERO, Blocks.STONE.defaultBlockState()), BlockPos.ZERO, Map.of());
        assertTrue(scene.lootChests().isEmpty());
    }

    @Test void rotatingScenesKeepsLootWithTheChestAndItsApproach() {
        BlockPos chest = new BlockPos(-17, 3, 16);
        Map<BlockPos, BlockState> shifted = new HashMap<>();
        fixture().forEach((pos, state) -> shifted.put(pos.offset(chest), state));
        var scene = new DreamScene("rotation", shifted, chest.south(), Map.of(Direction.SOUTH, chest.south()),
                Map.of(chest, DreamChestLoot.ARCHIVE));
        for (var rotation : Rotation.values()) {
            var rotated = DreamArchipelagoLayout.rotate(scene, rotation);
            assertEquals(1, rotated.lootChests().size());
            var marker = rotated.lootChests().entrySet().iterator().next();
            assertEquals(DreamChestLoot.ARCHIVE, marker.getValue());
            var state = rotated.blocks().get(marker.getKey());
            assertTrue(state.is(Blocks.CHEST));
            assertEquals(rotation.rotate(Direction.SOUTH), state.getValue(ChestBlock.FACING));
            assertEquals(marker.getKey().relative(state.getValue(ChestBlock.FACING)), rotated.landing());
            assertEquals(rotated.landing(), rotated.exits().get(rotation.rotate(Direction.SOUTH)));
        }
    }
}
