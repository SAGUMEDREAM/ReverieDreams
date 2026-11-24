package cc.thonly.reverie_dreams.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mushroom that can grow at any light level on a suitable surface.
 */
public final class MagicMushroomBlock extends MushroomBlock {
    public static final int LIGHT_LEVEL = 7;
    public static final MapCodec<MushroomBlock> CODEC = RecordCodecBuilder.<MushroomBlock>mapCodec(i -> i.group(
            ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(b -> ((MagicMushroomBlock) b).giant), propertiesCodec()
    ).apply(i, MagicMushroomBlock::new));
    private final ResourceKey<ConfiguredFeature<?, ?>> giant;
    private final VoxelShape shape;

    public MagicMushroomBlock(ResourceKey<ConfiguredFeature<?, ?>> giant, Properties properties) {
        super(giant, properties.lightLevel(state -> LIGHT_LEVEL));
        this.giant = giant;
        this.shape = giant.identifier().getPath().endsWith("blue_mushroom") ? Block.column(10, 0, 6) : Block.column(8, 0, 9);
    }

    @Override
    public MapCodec<MushroomBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState floor = level.getBlockState(below);
        return floor.is(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT) || mayPlaceOn(floor, level, below);
    }
}
