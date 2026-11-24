package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.registry.content.block.RDMagicForestBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FicusGrowthFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MAX_SCAN = 24;

    public FicusGrowthFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos.MutableBlockPos cursor = context.origin().mutable();
        BlockPos logPos = null;
        for (int step = 0; step < MAX_SCAN; step++) {
            BlockState probe = level.getBlockState(cursor);
            if (probe.is(BlockTags.LOGS)) {
                logPos = cursor.immutable();
                break;
            }
            if (!probe.isAir() && !probe.is(BlockTags.LEAVES) && !probe.canBeReplaced()) {
                return false;
            }
            cursor.move(Direction.DOWN);
        }
        if (logPos == null) {
            return false;
        }

        BlockState ficus = RDMagicForestBlocks.FICUS_MICROCARPA.defaultBlockState();
        List<Direction> sides = new ArrayList<>(List.of(
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        for (int i = sides.size() - 1; i > 0; i--) {
            Collections.swap(sides, i, random.nextInt(i + 1));
        }

        int wanted = 1 + random.nextInt(3);
        int placed = 0;
        for (Direction side : sides) {
            if (placed >= wanted) {
                break;
            }
            BlockPos growthPos = logPos.relative(side);
            if (!level.getBlockState(growthPos).isAir()) {
                continue;
            }
            BlockState growth = ficus.setValue(MultifaceBlock.getFaceProperty(side.getOpposite()), true);
            if (!growth.canSurvive(level, growthPos)) {
                continue;
            }
            level.setBlock(growthPos, growth, Block.UPDATE_CLIENTS);
            placed++;
        }
        return placed > 0;
    }
}
