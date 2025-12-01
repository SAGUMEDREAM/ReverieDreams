package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.compat.BorukvaFoodCompatImpl;
import cc.thonly.reverie_dreams.inf.IMatureBlock;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@ToString
public abstract class AbstractCropBlock extends VegetationBlock implements BonemealableBlock, IMatureBlock {
    protected Item seed;

    protected AbstractCropBlock(Properties settings) {
        super(settings.noOcclusion().noCollission().randomTicks().instabreak().sound(SoundType.CROP));
        this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(this.getAgeProperty());
    }

    public abstract Integer getMaxAge();

    public abstract IntegerProperty getAgeProperty();

    @Override
    protected abstract MapCodec<? extends VegetationBlock> codec();

    protected ItemLike getSeedsItem() {
        return this.seed;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this.getSeedsItem());
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        if (!BorukvaFoodCompatImpl.hasBorukvaFood()) {
            return floor.is(Blocks.FARMLAND);
        } else {
            return floor.is(Blocks.FARMLAND) || floor.is(BorukvaFoodCompatImpl.BETTER_FARMLAND);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return !this.isMature(state);
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !this.isMature(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        int age = this.getAge(state);
        if (age >= this.getMaxAge()) return;

        if (world.getRawBrightness(pos, 0) >= 9) {
            float moisture = getAvailableMoisture(this, world, pos);

            int chance = (int) (15.0f / moisture) + 1;

            if (random.nextInt(chance) == 0) {
                world.setBlock(pos, this.withAge(age + 1), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
        if (!state.canSurvive(world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        this.applyGrowth(world, pos, state);
    }

    public void applyGrowth(Level world, BlockPos pos, BlockState state) {
        int i = Math.min(this.getMaxAge(), this.getAge(state) + this.getGrowthAmount(world));
        world.setBlock(pos, this.withAge(i), Block.UPDATE_CLIENTS);
    }

    protected int getGrowthAmount(Level world) {
        return Mth.nextInt(world.random, 1, 3);
    }

    public BlockState withAge(int age) {
        return (BlockState) this.defaultBlockState().setValue(this.getAgeProperty(), age);
    }

    public int getAge(BlockState state) {
        return state.getValue(this.getAgeProperty());
    }

    protected static float getAvailableMoisture(Block block, BlockGetter world, BlockPos pos) {
        boolean bl2;
        float f = 1.0f;
        BlockPos blockPos = pos.below();
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                float g = 0.0f;
                BlockState blockState = world.getBlockState(blockPos.offset(i, 0, j));
                if (blockState.is(Blocks.FARMLAND)) {
                    g = 1.0f;
                    if (blockState.getValue(FarmBlock.MOISTURE) > 0) {
                        g = 3.0f;
                    }
                }
                if (i != 0 || j != 0) {
                    g /= 4.0f;
                }
                f += g;
            }
        }
        BlockPos blockPos2 = pos.north();
        BlockPos blockPos3 = pos.south();
        BlockPos blockPos4 = pos.west();
        BlockPos blockPos5 = pos.east();
        boolean bl = world.getBlockState(blockPos4).is(block) || world.getBlockState(blockPos5).is(block);
        boolean bl3 = bl2 = world.getBlockState(blockPos2).is(block) || world.getBlockState(blockPos3).is(block);
        if (bl && bl2) {
            f /= 2.0f;
        } else {
            boolean bl32;
            boolean bl4 = bl32 = world.getBlockState(blockPos4.north()).is(block) || world.getBlockState(blockPos5.north()).is(block) || world.getBlockState(blockPos5.south()).is(block) || world.getBlockState(blockPos4.south()).is(block);
            if (bl32) {
                f /= 2.0f;
            }
        }
        return f;
    }

}
