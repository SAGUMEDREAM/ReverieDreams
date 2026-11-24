package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

/**
 * Natural silver-ear colonies need a local wet or wooded habitat; player planting is unaffected.
 */
public final class TremellaFeature extends Feature<SimpleBlockConfiguration> {
    public static final int HABITAT_RADIUS = 3;

    public TremellaFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        return context.level().isEmptyBlock(context.origin())
                && hasHabitat(context.level(), context.origin()) && Feature.SIMPLE_BLOCK.place(context);
    }

    public static boolean hasHabitat(BlockGetter level, BlockPos origin) {
        var probe = new BlockPos.MutableBlockPos();
        for (int dx = -HABITAT_RADIUS; dx <= HABITAT_RADIUS; dx++) {
            for (int dy = -HABITAT_RADIUS; dy <= HABITAT_RADIUS; dy++) {
                for (int dz = -HABITAT_RADIUS; dz <= HABITAT_RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > HABITAT_RADIUS * HABITAT_RADIUS)
                        continue;
                    probe.setWithOffset(origin, dx, dy, dz);
                    var state = level.getBlockState(probe);
                    if (level.getFluidState(probe).is(FluidTags.WATER)
                            || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)
                            || state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS)
                            || state.is(Blocks.FARMLAND) && state.getValue(BlockStateProperties.MOISTURE) > 0)
                        return true;
                }
            }
        }
        return false;
    }
}
