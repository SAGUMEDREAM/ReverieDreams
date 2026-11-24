package cc.thonly.reverie_dreams.world.dream.scene;

import cc.thonly.reverie_dreams.block.decor.ChabudaiBlock;
import cc.thonly.reverie_dreams.block.decor.TatamiBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.world.dream.DreamChestLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.EnumMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The authored first set of dream islands. No world, random source, entity or loot state is mutated
 * here: generation can clip the same block plans to each chunk, and previews use the exact plans.
 */
public final class DreamIslandScenes {
    private DreamIslandScenes() {
    }

    public static DreamScene station() {
        return Scenes.STATION;
    }

    public static DreamScene bamboo() {
        return Scenes.BAMBOO;
    }

    public static DreamScene teaGarden() {
        return Scenes.TEA;
    }

    public static DreamScene mirrorPond() {
        return Scenes.POND;
    }

    /**
     * Fresh terrain variants; callers may retain a bounded set, but no seed cache grows here.
     */
    public static DreamScene create(IslandType type, long seed) {
        return switch (Objects.requireNonNull(type)) {
            case BAMBOO -> createBamboo(seed);
            case TEA_GARDEN -> createTeaGarden(seed);
            case MIRROR_POND -> createMirrorPond(seed);
            case CRYSTAL_REEF -> createCrystalReef(seed);
            case CHERRY_ISLE -> createCherryIsle(seed);
            case ARCHIVE_RUIN -> createArchiveRuin(seed);
        };
    }

    /**
     * Lazy construction keeps custom block suppliers out of early class loading.
     */
    private static final class Scenes {
        private static final DreamScene STATION = createStation();
        private static final DreamScene BAMBOO = createBamboo(0);
        private static final DreamScene TEA = createTeaGarden(0);
        private static final DreamScene POND = createMirrorPond(0);
    }

    private static DreamScene createStation() {
        Builder b = new Builder("huaian_station", 14, false, 0);
        BlockState planks = RDWoodBlocks.GINKGO_BUNDLE.planks().defaultBlockState();
        BlockState frame = Blocks.DARK_OAK_LOG.defaultBlockState();
        BlockState brick = RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState();

        // A wide forecourt and three clear approaches lead to a roofed, open-front waiting house.
        b.fill(-13, 0, -13, 13, 0, 4, planks);
        b.fill(-1, 0, -1, 1, 0, 21, brick);
        b.fill(-24, 0, -1, 24, 0, 1, brick);
        b.fill(-5, 0, 5, 5, 0, 11, brick);
        for (int x : new int[]{-11, 11}) {
            for (int z : new int[]{-12, 2})
                b.fill(x, 1, z, x, 5, z, frame);
            b.fill(x, 5, -12, x, 5, 2, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }
        b.fill(-11, 5, -12, 11, 5, -12, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(-11, 5, 2, 11, 5, 2, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(-10, 1, -12, 10, 1, -12, planks);
        b.fill(-10, 2, -12, 10, 4, -12, Blocks.WHITE_STAINED_GLASS.defaultBlockState());
        // Side windows stop before the east/west route, leaving a full three-block opening.
        for (int x : new int[]{-11, 11}) {
            b.fill(x, 1, -11, x, 1, -3, planks);
            b.fill(x, 2, -11, x, 4, -3, Blocks.WHITE_STAINED_GLASS.defaultBlockState());
        }
        roof(b, -13, 13, -14, 4, 6);

        // The bed is a real vanilla two-part bed. It uses the dream dimension's existing wake exit.
        b.fill(-9, 0, -10, -3, 0, -4, RDBlocks.TATAMI_BLOCK.defaultBlockState());
        BlockState bed = Blocks.PINK_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        b.set(-7, 1, -7, bed.setValue(BedBlock.PART, BedPart.FOOT));
        b.set(-7, 1, -8, bed.setValue(BedBlock.PART, BedPart.HEAD));
        b.set(-9, 1, -9, RDBlocks.ANDON.defaultBlockState());
        b.set(-3, 1, -10, facing(RDBlocks.BYOBU.defaultBlockState(), Direction.SOUTH));
        b.set(-3, 2, -10, facing(RDBlocks.BYOBU.defaultBlockState(), Direction.SOUTH));

        // A library/record desk and an actual usable low tea table form separate interior zones.
        b.fill(1, 1, -11, 8, 2, -11, Blocks.BOOKSHELF.defaultBlockState());
        b.set(5, 1, -9, Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
        teaTable(b, 5, -5, DyeColor.BLUE);
        b.set(9, 1, -9, RDBlocks.ANDON.defaultBlockState());
        b.set(-10, 1, 1, RDBlocks.ANDON.defaultBlockState());
        b.set(10, 1, 1, RDBlocks.ANDON.defaultBlockState());
        // Luggage beside the sleeping area, clear of the bed and all three through routes.
        b.chest(-9, 1, -4, Direction.SOUTH, DreamChestLoot.SUPPLY);

        // The forecourt has a readable arrival medallion and paired blue/pink route markers.
        b.fill(-2, 0, 7, 2, 0, 9, Blocks.SMOOTH_QUARTZ.defaultBlockState());
        b.set(0, 0, 8, Blocks.LODESTONE.defaultBlockState());
        b.set(-4, 1, 8, RDBlocks.ANDON.defaultBlockState());
        b.set(4, 1, 8, RDBlocks.ANDON.defaultBlockState());
        marker(b, -18, -3, Blocks.BLUE_TERRACOTTA.defaultBlockState());
        marker(b, 18, -3, Blocks.PINK_TERRACOTTA.defaultBlockState());
        marker(b, 3, 16, Blocks.PURPLE_TERRACOTTA.defaultBlockState());
        b.exits.put(Direction.WEST, new BlockPos(-24, 0, 0));
        b.exits.put(Direction.EAST, new BlockPos(24, 0, 0));
        b.exits.put(Direction.SOUTH, new BlockPos(0, 0, 21));
        b.railEdge();
        b.clearApproach(Direction.WEST, 24);
        b.clearApproach(Direction.EAST, 24);
        b.clearApproach(Direction.SOUTH, 21);
        return b.finish(new BlockPos(0, 1, 8));
    }

    private static DreamScene createBamboo(long seed) {
        Builder b = new Builder("bamboo_memory", 12, true, seed);
        BlockState path = Blocks.SMOOTH_STONE.defaultBlockState();
        b.fill(-11, 0, -1, 18, 0, 1, path);
        b.fill(-9, 0, -6, -7, 0, 6, path);
        b.fill(-9, 0, 4, 5, 0, 6, path);
        // An incomplete plaster wall and an empty gate make the grove a remembered place.
        for (int z : new int[]{-7, 7})
            b.fill(-12, 1, z, -12, 4, z, Blocks.STRIPPED_BAMBOO_BLOCK.defaultBlockState());
        b.fill(-12, 4, -7, -12, 4, 7, Blocks.BAMBOO_PLANKS.defaultBlockState());
        b.fill(-12, 1, -6, -12, 2, -4, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        b.fill(-12, 1, 4, -12, 3, 6, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        for (int[] clump : new int[][]{{-7, -9, 7}, {-3, -8, 6}, {2, -10, 8}, {7, -8, 6}, {11, -5, 7},
                {-4, 10, 7}, {1, 9, 6}, {7, 8, 8}, {11, 5, 5}, {-15, -3, 6}, {-15, 3, 7}}) {
            bamboo(b, clump[0], clump[1], clump[2]);
            if (b.surface(clump[0] + 1, clump[1] + 1))
                bamboo(b, clump[0] + 1, clump[1] + 1, clump[2] - 2);
        }
        b.set(-8, 1, -5, RDBlocks.STONE_LANTERN.defaultBlockState());
        b.set(-8, 1, 5, RDBlocks.STONE_LANTERN.defaultBlockState());
        b.set(9, 1, 3, RDBlocks.ANDON.defaultBlockState());
        b.set(4, 1, -4, Blocks.MOSS_CARPET.defaultBlockState());
        b.set(-4, 1, 4, Blocks.MOSS_CARPET.defaultBlockState());
        b.exits.put(Direction.EAST, new BlockPos(18, 0, 0));
        b.railEdge();
        b.clearApproach(Direction.EAST, 18);
        return b.finish(new BlockPos(0, 1, 0));
    }

    private static DreamScene createTeaGarden(long seed) {
        Builder b = new Builder("tea_garden_memory", 13, true, seed);
        BlockState wood = RDWoodBlocks.HAUCHIWA_MAPLE_BUNDLE.planks().defaultBlockState();
        b.fill(-10, 0, -8, 10, 0, 8, wood);
        b.fill(-1, 0, -16, 1, 0, 10, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
        // Only the western half of the room survives; its eastern edge is deliberately open.
        b.fill(-10, 1, -8, -10, 4, -8, Blocks.DARK_OAK_LOG.defaultBlockState());
        b.fill(-10, 1, 7, -10, 4, 7, Blocks.DARK_OAK_LOG.defaultBlockState());
        b.fill(-3, 1, -8, -3, 4, -8, Blocks.DARK_OAK_LOG.defaultBlockState());
        b.fill(-10, 4, -8, -3, 4, -8, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(-10, 4, 7, -3, 4, 7, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(-10, 1, -7, -10, 2, 6, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        for (int x = -11; x <= -2; x++) {
            int roofY = 5 + (4 - Math.abs(x + 7)) / 2;
            b.fill(x, roofY, -9, x, roofY, 8, Blocks.DEEPSLATE_TILES.defaultBlockState());
        }
        b.fill(-8, 0, -6, -4, 0, 5, RDBlocks.TATAMI_BLOCK.defaultBlockState());
        teaTable(b, -6, -2, DyeColor.PURPLE);
        teaTable(b, 5, 3, DyeColor.PINK);
        b.set(-9, 1, 5, RDBlocks.ANDON.defaultBlockState());
        b.set(-4, 1, -7, RDBlocks.ANDON.defaultBlockState());
        b.set(8, 1, 7, RDBlocks.STONE_LANTERN.defaultBlockState());
        // A single peach-coloured tree shelters the absent room, preserving a contrasting canopy.
        smallTree(b, 10, -7);
        b.set(-11, 0, 10, Blocks.MOSS_BLOCK.defaultBlockState());
        b.set(-11, 1, 10, Blocks.AZURE_BLUET.defaultBlockState());
        b.set(-7, 1, 12, Blocks.PINK_PETALS.defaultBlockState());
        b.set(5, 1, 11, Blocks.PINK_PETALS.defaultBlockState());
        b.exits.put(Direction.NORTH, new BlockPos(0, 0, -16));
        b.railEdge();
        b.clearApproach(Direction.NORTH, 16);
        return b.finish(new BlockPos(0, 1, 8));
    }

    private static DreamScene createMirrorPond(long seed) {
        Builder b = new Builder("mirror_pond_memory", 14, true, seed);
        BlockState basin = RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState();
        for (int x = -6; x <= 11; x++)
            for (int z = -8; z <= 8; z++) {
                double d = ((x - 2.0) * (x - 2.0)) / 81.0 + (z * z) / 64.0;
                if (d <= 1.0) {
                    b.set(x, -2, z, basin);
                    b.set(x, -1, z, basin);
                    b.set(x, 0, z, d < .76 ? Blocks.WATER.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState());
                }
            }
        // This bridge has a solid walking floor at the same height as all other cluster routes.
        b.fill(-18, 0, -1, 14, 0, 1, Blocks.BAMBOO_PLANKS.defaultBlockState());
        b.fill(-11, 0, -10, -9, 0, 10, Blocks.BAMBOO_PLANKS.defaultBlockState());
        b.fill(11, 0, -5, 14, 0, 5, Blocks.BAMBOO_PLANKS.defaultBlockState());
        b.fill(-11, 0, -10, 9, 0, -8, Blocks.SMOOTH_STONE.defaultBlockState());
        b.fill(-11, 0, 8, 9, 0, 10, Blocks.SMOOTH_STONE.defaultBlockState());
        for (int x = -6; x <= 10; x++)
            for (int z : new int[]{-2, 2}) {
                b.set(x, 1, z, connectedFence(Blocks.BAMBOO_FENCE.defaultBlockState(), Direction.EAST, Direction.WEST));
            }
        dreamGate(b, -8, Blocks.BLUE_TERRACOTTA.defaultBlockState());
        dreamGate(b, 12, Blocks.PURPLE_TERRACOTTA.defaultBlockState());
        b.set(-10, 1, -9, RDBlocks.STONE_LANTERN.defaultBlockState());
        b.set(-10, 1, 9, RDBlocks.STONE_LANTERN.defaultBlockState());
        b.set(13, 1, -4, RDBlocks.ANDON.defaultBlockState());
        b.set(13, 1, 4, RDBlocks.ANDON.defaultBlockState());
        b.set(1, 1, -4, Blocks.LILY_PAD.defaultBlockState());
        b.set(5, 1, 4, Blocks.LILY_PAD.defaultBlockState());
        b.set(-5, 1, -12, Blocks.CORNFLOWER.defaultBlockState());
        b.set(4, 1, 12, Blocks.CORNFLOWER.defaultBlockState());
        b.set(5, 1, 9, RDBlocks.FOOTSTEP_MOSS.defaultBlockState());
        b.set(6, 1, 9, RDBlocks.FOOTSTEP_MOSS.defaultBlockState());
        b.set(6, 1, 10, RDBlocks.FOOTSTEP_MOSS.defaultBlockState());
        b.exits.put(Direction.WEST, new BlockPos(-18, 0, 0));
        b.railEdge();
        b.clearApproach(Direction.WEST, 18);
        return b.finish(new BlockPos(-12, 1, 0));
    }

    private static DreamScene createCrystalReef(long seed) {
        Builder b = new Builder("crystal_reef_memory", 16, false, seed);
        BlockState path = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        // A crescent ridge wraps around open sky, with a walkable western landing and upper shelf.
        b.fill(-26, 0, -1, -7, 0, 1, path);
        b.fill(-15, 0, -6, -13, 0, 0, path);
        b.fill(-14, 0, -6, 17, 0, -4, path);
        pool(b, -17, 5, 3, 3);
        pool(b, 4, -9, 4, 3);
        crystalOutcrop(b, -20, -6, 7);
        crystalOutcrop(b, -8, -10, 10);
        crystalOutcrop(b, 12, -10, 8);
        crystalOutcrop(b, 20, 3, 6);
        b.set(-11, 1, -5, RDBlocks.ANDON.defaultBlockState());
        b.set(10, 1, -5, RDBlocks.ANDON.defaultBlockState());
        b.railX(-25, -18, -2);
        b.railX(-25, -18, 2);
        b.railX(-7, 6, -3);
        b.exits.put(Direction.WEST, new BlockPos(-26, 0, 0));
        b.clearApproach(Direction.WEST, 26);
        // The reward sits on the upper shelf beside the lit path, never below a floating bridge.
        b.fill(3, 0, -7, 6, 0, -4, RDWoodBlocks.GINKGO_BUNDLE.planks().defaultBlockState());
        b.chest(5, 1, -6, Direction.SOUTH, DreamChestLoot.CRYSTAL);
        return b.finish(new BlockPos(-13, 1, 0));
    }

    private static DreamScene createCherryIsle(long seed) {
        Builder b = new Builder("cherry_isle_memory", 16, true, seed);
        BlockState paving = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState wood = RDWoodBlocks.PEACH_BUNDLE.planks().defaultBlockState();
        b.fill(-24, 0, -1, 13, 0, 1, paving);
        b.fill(-7, 0, -5, 5, 0, 5, wood);
        b.fill(-1, 0, -5, 1, 0, 10, paving);
        b.fill(-7, 0, 8, 13, 0, 10, paving);
        pool(b, 10, 6, 4, 3);
        broadCherry(b, 8, -10, 5);
        broadCherry(b, -12, 9, 4);
        teaTable(b, -4, 2, DyeColor.PINK);
        b.set(-6, 1, -4, RDBlocks.ANDON.defaultBlockState());
        b.set(4, 1, -4, RDBlocks.ANDON.defaultBlockState());
        b.set(14, 1, 9, RDBlocks.STONE_LANTERN.defaultBlockState());
        for (int[] flower : new int[][]{{-9, -7}, {-14, 5}, {-8, 13}, {4, 13}, {13, -3}, {16, -8}}) {
            b.set(flower[0], 0, flower[1], Blocks.MOSS_BLOCK.defaultBlockState());
            b.set(flower[0], 1, flower[1], Blocks.PINK_PETALS.defaultBlockState());
        }
        b.railX(-23, -16, -2);
        b.railX(-23, -16, 2);
        b.railX(-6, 5, 11);
        b.exits.put(Direction.WEST, new BlockPos(-24, 0, 0));
        b.clearApproach(Direction.WEST, 24);
        b.chest(3, 1, 4, Direction.WEST, DreamChestLoot.GARDEN);
        return b.finish(new BlockPos(-14, 1, 0));
    }

    private static DreamScene createArchiveRuin(long seed) {
        Builder b = new Builder("archive_ruin_memory", 13, false, seed);
        BlockState floor = Blocks.CALCITE.defaultBlockState();
        BlockState wood = RDWoodBlocks.GINKGO_BUNDLE.planks().defaultBlockState();
        BlockState frame = Blocks.DARK_OAK_LOG.defaultBlockState();
        // Broken rectangular terraces retain the library's building footprint in their coastline.
        b.fill(-25, 0, -1, 17, 0, 1, floor);
        b.fill(-13, 0, -7, 13, 0, 9, wood);
        b.fill(-2, 0, -8, 0, 0, 12, floor);
        b.fill(-11, 1, -12, 11, 1, -8, wood);
        b.fill(-3, 1, -7, 1, 1, -7, Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        b.fill(-3, 0, -7, 1, 0, -7, wood);
        b.fill(-13, 1, -9, -13, 5, -9, frame);
        b.fill(-13, 1, 8, -13, 3, 8, frame);
        b.fill(12, 1, -9, 12, 6, -9, frame);
        b.fill(-13, 5, -9, -4, 5, -9, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(4, 6, -9, 12, 6, -9, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(-12, 2, -10, -6, 4, -10, Blocks.BOOKSHELF.defaultBlockState());
        b.fill(4, 2, -10, 10, 4, -10, Blocks.BOOKSHELF.defaultBlockState());
        b.fill(10, 1, 3, 10, 3, 8, Blocks.BOOKSHELF.defaultBlockState());
        b.fill(-12, 1, 5, -8, 2, 5, Blocks.BOOKSHELF.defaultBlockState());
        b.fill(-13, 1, -8, -13, 3, -2, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        b.fill(12, 1, -8, 12, 4, -4, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        b.set(-1, 2, -11, Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
        b.set(3, 1, 4, Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST));
        b.set(-10, 2, -8, RDBlocks.ANDON.defaultBlockState());
        b.set(9, 2, -8, RDBlocks.ANDON.defaultBlockState());
        b.set(-7, 1, 7, RDBlocks.ANDON.defaultBlockState());
        b.set(8, 1, 8, RDBlocks.ANDON.defaultBlockState());
        b.railX(-24, -17, -2);
        b.railX(-24, -17, 2);
        b.railX(-8, 4, 11);
        b.exits.put(Direction.WEST, new BlockPos(-25, 0, 0));
        b.clearApproach(Direction.WEST, 25);
        b.chest(6, 2, -9, Direction.SOUTH, DreamChestLoot.ARCHIVE);
        // A missing shelf section exposes a lit, walk-in alcove. Its floor is fully supported;
        // no blind digging or jump over the island rim is needed to discover the extra reward.
        b.fill(-12, 0, 6, -8, 0, 9, wood);
        b.fill(-10, 1, 5, -9, 2, 5, Blocks.AIR.defaultBlockState());
        b.fill(-10, 0, 3, -9, 0, 8, floor);
        b.chest(-11, 1, 8, Direction.EAST, DreamChestLoot.ARCHIVE_ALCOVE);
        b.set(-12, 1, 8, RDBlocks.ANDON.defaultBlockState());
        return b.finish(new BlockPos(-15, 1, 0));
    }

    private static void pool(Builder b, int centerX, int centerZ, int rx, int rz) {
        BlockState lining = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        for (int x = centerX - rx; x <= centerX + rx; x++)
            for (int z = centerZ - rz; z <= centerZ + rz; z++) {
                double shape = (x - centerX) * (x - centerX) / (double) (rx * rx)
                        + (z - centerZ) * (z - centerZ) / (double) (rz * rz);
                if (shape > 1)
                    continue;
                b.set(x, -2, z, RDBlocks.DREAM_STONE.block().defaultBlockState());
                b.set(x, -1, z, lining);
                b.set(x, 0, z, shape <= .62 ? Blocks.WATER.defaultBlockState() : lining);
            }
    }

    private static void crystalOutcrop(Builder b, int x, int z, int height) {
        b.fill(x - 2, 0, z - 2, x + 2, 0, z + 2, Blocks.CALCITE.defaultBlockState());
        b.fill(x - 1, 1, z - 1, x + 1, 2, z + 1, Blocks.AMETHYST_BLOCK.defaultBlockState());
        b.fill(x, 3, z, x, height - 1, z, Blocks.SMOOTH_QUARTZ.defaultBlockState());
        b.set(x, height, z, RDBlocks.DREAM_CRYSTAL_ORE.defaultBlockState());
        b.fill(x + 2, 1, z, x + 2, height - 3, z, Blocks.AMETHYST_BLOCK.defaultBlockState());
        b.set(x + 2, height - 2, z, Blocks.AMETHYST_CLUSTER.defaultBlockState());
        b.fill(x - 1, 1, z + 2, x - 1, height - 4, z + 2, Blocks.AMETHYST_BLOCK.defaultBlockState());
        b.set(x - 1, height - 3, z + 2, Blocks.AMETHYST_CLUSTER.defaultBlockState());
    }

    private static void broadCherry(Builder b, int x, int z, int radius) {
        b.set(x, 0, z, Blocks.MOSS_BLOCK.defaultBlockState());
        b.fill(x, 1, z, x, 6, z, Blocks.CHERRY_LOG.defaultBlockState());
        b.fill(x + 1, 4, z, x + 2, 5, z, Blocks.CHERRY_LOG.defaultBlockState());
        BlockState leaves = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++) {
                double shape = dx * dx / (double) (radius * radius) + dz * dz / (double) ((radius - 1) * (radius - 1));
                if (shape > 1)
                    continue;
                b.set(x + dx, 6, z + dz, leaves);
                if (shape < .88)
                    b.set(x + dx, 7, z + dz, leaves);
                if (shape < .50)
                    b.set(x + dx, 8, z + dz, leaves);
            }
        b.fill(x - 2, 9, z - 1, x + 2, 9, z + 1, leaves);
    }

    private static void roof(Builder b, int minX, int maxX, int minZ, int maxZ, int eaveY) {
        for (int x = minX; x <= maxX; x++) {
            int rise = (Math.min(x - minX, maxX - x)) / 2;
            BlockState state = Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,
                    x < 0 ? Direction.EAST : Direction.WEST);
            if (x == 0)
                state = Blocks.DEEPSLATE_TILES.defaultBlockState();
            b.fill(x, eaveY + rise, minZ, x, eaveY + rise, maxZ, state);
        }
        b.fill(0, eaveY + 7, minZ, 0, eaveY + 7, maxZ, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
    }

    private static void teaTable(Builder b, int x, int z, DyeColor cushionColor) {
        BlockState table = RDBlocks.CHABUDAI.defaultBlockState().setValue(ChabudaiBlock.HAS_TEA_SET, true);
        b.set(x, 1, z, table);
        BlockState cushion = RDBlocks.ZABUTONS.get(cushionColor).defaultBlockState();
        b.set(x - 2, 1, z, facing(cushion, Direction.EAST));
        b.set(x + 2, 1, z, facing(cushion, Direction.WEST));
        b.set(x, 1, z + 2, facing(cushion, Direction.NORTH));
    }

    private static void marker(Builder b, int x, int z, BlockState color) {
        b.set(x, 1, z, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
        b.set(x, 2, z, color);
        b.set(x, 3, z, RDBlocks.ANDON.defaultBlockState());
    }

    private static void bamboo(Builder b, int x, int z, int height) {
        for (int y = 1; y <= height; y++) {
            BambooLeaves leaves = y == height ? BambooLeaves.SMALL : y >= height - 2 ? BambooLeaves.LARGE : BambooLeaves.NONE;
            b.set(x, y, z, Blocks.BAMBOO.defaultBlockState().setValue(BambooStalkBlock.AGE, 1)
                    .setValue(BambooStalkBlock.LEAVES, leaves).setValue(BambooStalkBlock.STAGE, y == height ? 1 : 0));
        }
    }

    private static void smallTree(Builder b, int x, int z) {
        b.fill(x, 1, z, x, 4, z, Blocks.CHERRY_LOG.defaultBlockState());
        BlockState leaves = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 4)
                    continue;
                b.set(x + dx, 4, z + dz, leaves);
                if (Math.abs(dx) + Math.abs(dz) < 4)
                    b.set(x + dx, 5, z + dz, leaves);
            }
        b.fill(x - 1, 6, z - 1, x + 1, 6, z + 1, leaves);
    }

    private static void dreamGate(Builder b, int x, BlockState color) {
        for (int z : new int[]{-3, 3}) {
            b.fill(x, 1, z, x, 4, z, color);
            b.set(x, 0, z, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
        }
        b.fill(x, 5, -3, x, 5, 3, color);
        b.fill(x, 6, -2, x, 6, 2, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
    }

    private static BlockState facing(BlockState state, Direction direction) {
        return state.hasProperty(HorizontalDirectionalBlock.FACING)
                ? state.setValue(HorizontalDirectionalBlock.FACING, direction) : state;
    }

    private static BlockState connectedFence(BlockState state, Direction first, Direction second) {
        return state.setValue(switch (first) {
            case NORTH -> BlockStateProperties.NORTH;
            case EAST -> BlockStateProperties.EAST;
            case SOUTH -> BlockStateProperties.SOUTH;
            case WEST -> BlockStateProperties.WEST;
            default -> throw new IllegalArgumentException("horizontal fence connection required");
        }, true).setValue(switch (second) {
            case NORTH -> BlockStateProperties.NORTH;
            case EAST -> BlockStateProperties.EAST;
            case SOUTH -> BlockStateProperties.SOUTH;
            case WEST -> BlockStateProperties.WEST;
            default -> throw new IllegalArgumentException("horizontal fence connection required");
        }, true);
    }

    private static final class Builder {
        private final String id;
        private final DreamIslandShape shape;
        private final int maximumDepth;
        private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        private final Map<Direction, BlockPos> exits = new EnumMap<>(Direction.class);
        private final Map<BlockPos, ResourceKey<LootTable>> lootChests = new LinkedHashMap<>();

        private Builder(String id, int depth, boolean grass, long seed) {
            this.id = id;
            this.maximumDepth = depth;
            this.shape = DreamIslandShape.forScene(id, seed);
            BlockState stone = RDBlocks.DREAM_STONE.block().defaultBlockState();
            for (int x = shape.minX(); x <= shape.maxX(); x++)
                for (int z = shape.minZ(); z <= shape.maxZ(); z++) {
                    if (!shape.contains(x, z))
                        continue;
                    clearColumn(x, z);
                    int columnDepth = shape.depthAt(x, z, depth);
                    for (int y = -columnDepth; y < 0; y++)
                        set(x, y, z, stone);
                    // Untinted moss keeps the authored gardens consistent with natural-island
                    // vegetation and distinct from the pink dream-stone and water.
                    boolean planted = grass && shape.distanceToEdge(x, z) >= 1.3;
                    set(x, 0, z, planted ? Blocks.MOSS_BLOCK.defaultBlockState() : stone);
                    if (planted && columnDepth > 1)
                        set(x, -1, z, Blocks.DIRT.defaultBlockState());
                }
        }

        private boolean surface(int x, int z) {
            return blocks.containsKey(new BlockPos(x, 0, z));
        }

        private void clearColumn(int x, int z) {
            for (int y = 1; y <= 20; y++)
                blocks.put(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
        }

        private void set(int x, int y, int z, BlockState state) {
            if (y >= 0 && !blocks.containsKey(new BlockPos(x, 1, z)))
                clearColumn(x, z);
            blocks.put(new BlockPos(x, y, z), state);
        }

        private void fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState state) {
            for (int x = minX; x <= maxX; x++)
                for (int y = minY; y <= maxY; y++)
                    for (int z = minZ; z <= maxZ; z++)
                        set(x, y, z, state);
        }

        private void chest(int x, int y, int z, Direction facing, ResourceKey<LootTable> table) {
            set(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
            lootChests.put(new BlockPos(x, y, z), table);
        }

        private void railEdge() {
            // Rail deliberate walking approaches, not every irregular natural cliff edge.
            switch (id) {
                case "huaian_station" -> {
                    railX(-24, -18, -2);
                    railX(-24, -18, 2);
                    railX(18, 24, -2);
                    railX(18, 24, 2);
                    railX(-14, -2, 15);
                    railX(2, 14, 15);
                    railZ(-2, 15, 21);
                    railZ(2, 15, 21);
                }
                case "bamboo_memory" -> {
                    railX(10, 18, -2);
                    railX(10, 18, 2);
                }
                case "tea_garden_memory" -> {
                    railZ(-2, -16, -10);
                    railZ(2, -16, -10);
                }
                case "mirror_pond_memory" -> {
                    railX(-18, -11, -2);
                    railX(-18, -11, 2);
                }
                default -> {
                }
            }
        }

        private void railX(int from, int to, int z) {
            for (int x = from; x <= to; x++) {
                if (!surface(x, z))
                    set(x, 0, z, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
                set(x, 1, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
            }
        }

        private void railZ(int x, int from, int to) {
            for (int z = from; z <= to; z++) {
                if (!surface(x, z))
                    set(x, 0, z, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
                set(x, 1, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
            }
        }

        private void connectSurfaces() {
            // Explicit connections make previews and clipped generation independent of update order.
            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                if (state.is(Blocks.DARK_OAK_FENCE)) {
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        BlockState neighbor = blocks.get(pos.relative(d));
                        state = state.setValue(switch (d) {
                            case NORTH -> BlockStateProperties.NORTH;
                            case EAST -> BlockStateProperties.EAST;
                            case SOUTH -> BlockStateProperties.SOUTH;
                            case WEST -> BlockStateProperties.WEST;
                            default -> throw new IllegalArgumentException();
                        }, neighbor != null && neighbor.is(Blocks.DARK_OAK_FENCE));
                    }
                } else if (state.is(RDBlocks.TATAMI_BLOCK.asBlock())) {
                    for (Direction side : Direction.Plane.HORIZONTAL) {
                        state = state.setValue(TatamiBlock.property(side),
                                sameBlockAt(state, pos.relative(side)));
                    }
                    for (TatamiBlock.Corner corner : TatamiBlock.Corner.values()) {
                        state = state.setValue(corner.property,
                                sameBlockAt(state, pos.relative(corner.first))
                                        && sameBlockAt(state, pos.relative(corner.second))
                                        && sameBlockAt(state, pos.relative(corner.first).relative(corner.second)));
                    }
                }
                entry.setValue(state);
            }
        }

        private boolean sameBlockAt(BlockState state, BlockPos pos) {
            BlockState neighbor = blocks.get(pos);
            return neighbor != null && neighbor.is(state.getBlock());
        }

        private void clearApproach(Direction direction, int reach) {
            // Port headroom is guaranteed even where an ellipse narrows to a single top block.
            for (int along = Math.max(0, reach - 4); along <= reach; along++)
                for (int across = -1; across <= 1; across++) {
                    int x = direction.getStepX() * along + direction.getStepZ() * across;
                    int z = direction.getStepZ() * along + direction.getStepX() * across;
                    set(x, 0, z, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
                    for (int y = 1; y <= 4; y++)
                        set(x, y, z, Blocks.AIR.defaultBlockState());
                }
        }

        private DreamScene finish(BlockPos landing) {
            // Seeds may erode an outer headland, but never leave authored planting on an invalid rim.
            for (Map.Entry<BlockPos, BlockState> entry : new ArrayList<>(blocks.entrySet())) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                if (pos.getY() != 1 || state.isAir())
                    continue;
                boolean plant = state.is(Blocks.BAMBOO)
                        || state.getBlock() instanceof VegetationBlock && !state.is(Blocks.LILY_PAD);
                if (plant)
                    set(pos.getX(), 0, pos.getZ(), Blocks.MOSS_BLOCK.defaultBlockState());
                else if (!surface(pos.getX(), pos.getZ()) && !state.is(Blocks.LILY_PAD)) {
                    set(pos.getX(), 0, pos.getZ(), RDBlocks.DREAM_STONE.block().defaultBlockState());
                }
            }
            for (int x = landing.getX() - 1; x <= landing.getX() + 1; x++)
                for (int z = landing.getZ() - 1; z <= landing.getZ() + 1; z++) {
                    if (!surface(x, z))
                        set(x, 0, z, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
                    for (int y = landing.getY(); y <= landing.getY() + 2; y++)
                        set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            // Authored terraces and bridge ports can project beyond a seeded coast; give each a
            // short tapered foundation while retaining the exact floor and doorway coordinates.
            for (BlockPos pos : List.copyOf(blocks.keySet())) {
                if (pos.getY() != 0 || blocks.get(pos).isAir())
                    continue;
                int depth = shape.contains(pos.getX(), pos.getZ())
                        ? shape.depthAt(pos.getX(), pos.getZ(), maximumDepth) : 2;
                for (int y = -depth; y < 0; y++) {
                    BlockPos support = new BlockPos(pos.getX(), y, pos.getZ());
                    if (!blocks.containsKey(support))
                        blocks.put(support, RDBlocks.DREAM_STONE.block().defaultBlockState());
                }
            }
            connectSurfaces();
            return new DreamScene(id, blocks, landing, exits, lootChests);
        }
    }
}
