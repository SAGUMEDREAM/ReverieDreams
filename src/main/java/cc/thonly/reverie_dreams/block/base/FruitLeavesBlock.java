package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.block.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FruitLeavesBlock extends LeavesBlock implements Fertilizable {
    public static final MapCodec<FruitLeavesBlock> CODEC = FruitLeavesBlock.createCodec(FruitLeavesBlock::new);
    public static final List<FruitLeavesBlock> FRUIT_LEAVES_BLOCKS = new ArrayList<>();
    public static final int MAX_AGE = 3;
    public static final IntProperty AGE_PROPERTY = IntProperty.of("fruit_age", 0, MAX_AGE);
    private Item output;
    private Block emptyLeavesBlock;

    public FruitLeavesBlock(Settings settings) {
        super(0.01f, settings.nonOpaque());
        this.setDefaultState(
                this.getStateManager()
                        .getDefaultState()
                        .with(DISTANCE, 7)
                        .with(AGE_PROPERTY, 0)
                        .with(LeavesBlock.WATERLOGGED, false)
        );
        BlockTypeGroup.FRUIT_LEAVES.add(this);
    }

    public FruitLeavesBlock(Item output, Block emptyLeavesBlock, Settings settings) {
        this(settings);
        this.emptyLeavesBlock = emptyLeavesBlock;
        this.output = output;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack main = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack off = player.getStackInHand(Hand.OFF_HAND);
        boolean growItem = main.getItem() == Items.BONE_MEAL || off.getItem() == Items.BONE_MEAL;

        if (!world.isClient() && world instanceof ServerWorld serverWorld && growItem) {
            return ActionResult.PASS;
        }
        if (growItem) {
            return ActionResult.PASS;
        }

        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            Integer age = state.get(AGE_PROPERTY);
            Random random = world.getRandom();
            if (age >= MAX_AGE) {
                world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0f, 0.8f + world.random.nextFloat() * 0.4f);
                ItemEntity drop = new ItemEntity(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this.output, random.nextBetween(1, 3)));
                drop.setPickupDelay(10);
                drop.setVelocity(0, 0.2, 0);
                world.spawnEntity(drop);
                world.setBlockState(pos, state.with(AGE_PROPERTY, 1));
                return ActionResult.SUCCESS_SERVER;
            } else {
                return ActionResult.PASS;
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected BlockSoundGroup getSoundGroup(BlockState state) {
        return BlockSoundGroup.GRASS;
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    public boolean canFillWithFluid(@Nullable LivingEntity filler, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE_PROPERTY);
    }

    @Override
    public MapCodec<? extends LeavesBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void spawnLeafParticle(World world, BlockPos pos, Random random) {
        EntityEffectParticleEffect entityEffectParticleEffect = EntityEffectParticleEffect.create(ParticleTypes.TINTED_LEAVES, world.getBlockColor(pos));
        ParticleUtil.spawnParticle(world, pos, random, entityEffectParticleEffect);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        float f;
        int i;
        if (world.getBaseLightLevel(pos, 0) >= 9 && (i = this.getAge(state)) < MAX_AGE && random.nextInt((int) (25.0f / (f = getAvailableMoisture(this, world, pos))) + 1) == 0) {
            world.setBlockState(pos, this.withAge(i + 1), Block.NOTIFY_LISTENERS);
        }
    }

    public BlockState withAge(int age) {
        return this.getDefaultState().with(AGE_PROPERTY, age);
    }

    public int getAge(BlockState state) {
        return state.get(AGE_PROPERTY);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return state.get(AGE_PROPERTY) < MAX_AGE;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        float f;
        int age = state.get(AGE_PROPERTY);
//        System.out.println(age);
        if (age < MAX_AGE) {
            world.setBlockState(pos, state.with(AGE_PROPERTY, Math.min(age + 1, MAX_AGE)), Block.NOTIFY_ALL_AND_REDRAW);
        }
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
