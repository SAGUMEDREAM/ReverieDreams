package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.compat.BorukvaFoodCompatImpl;
import cc.thonly.reverie_dreams.interfaces.IMatureBlock;
import cc.thonly.reverie_dreams.util.block.CropAgeModelProvider;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@ToString
public abstract class AbstractCropBlock extends PlantBlock implements Fertilizable, IMatureBlock {
    protected Item seed;
    protected CropAgeModelProvider modelProvider;

    protected AbstractCropBlock(Settings settings) {
        super(settings.nonOpaque().noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP));
        this.setDefaultState(this.stateManager.getDefaultState().with(this.getAgeProperty(), 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(this.getAgeProperty());
    }

    public abstract Integer getMaxAge();

    public abstract IntProperty getAgeProperty();

    @Override
    protected abstract MapCodec<? extends PlantBlock> getCodec();

    protected ItemConvertible getSeedsItem() {
        return this.seed;
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this.getSeedsItem());
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        if (!BorukvaFoodCompatImpl.hasBorukvaFood()) {
            return floor.isOf(Blocks.FARMLAND);
        } else {
            return floor.isOf(Blocks.FARMLAND) || floor.isOf(BorukvaFoodCompatImpl.BETTER_FARMLAND);
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return !this.isMature(state);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return !this.isMature(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int age = this.getAge(state);
        if (age >= this.getMaxAge()) return;

        if (world.getBaseLightLevel(pos, 0) >= 9) {
            float moisture = getAvailableMoisture(this, world, pos);

            int chance = (int)(10.0f / moisture) + 1;

            if (random.nextInt(chance) == 0) {
                world.setBlockState(pos, this.withAge(age + 1), Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        this.applyGrowth(world, pos, state);
    }

    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        int i = Math.min(this.getMaxAge(), this.getAge(state) + this.getGrowthAmount(world));
        world.setBlockState(pos, this.withAge(i), Block.NOTIFY_LISTENERS);
    }

    protected int getGrowthAmount(World world) {
        return MathHelper.nextInt(world.random, 1, 3);
    }

    public BlockState withAge(int age) {
        return (BlockState) this.getDefaultState().with(this.getAgeProperty(), age);
    }

    public int getAge(BlockState state) {
        return state.get(this.getAgeProperty());
    }

    protected static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        boolean bl2;
        float f = 1.0f;
        BlockPos blockPos = pos.down();
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                float g = 0.0f;
                BlockState blockState = world.getBlockState(blockPos.add(i, 0, j));
                if (blockState.isOf(Blocks.FARMLAND)) {
                    g = 1.0f;
                    if (blockState.get(FarmlandBlock.MOISTURE) > 0) {
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
        boolean bl = world.getBlockState(blockPos4).isOf(block) || world.getBlockState(blockPos5).isOf(block);
        boolean bl3 = bl2 = world.getBlockState(blockPos2).isOf(block) || world.getBlockState(blockPos3).isOf(block);
        if (bl && bl2) {
            f /= 2.0f;
        } else {
            boolean bl32;
            boolean bl4 = bl32 = world.getBlockState(blockPos4.north()).isOf(block) || world.getBlockState(blockPos5.north()).isOf(block) || world.getBlockState(blockPos5.south()).isOf(block) || world.getBlockState(blockPos4.south()).isOf(block);
            if (bl32) {
                f /= 2.0f;
            }
        }
        return f;
    }

}
