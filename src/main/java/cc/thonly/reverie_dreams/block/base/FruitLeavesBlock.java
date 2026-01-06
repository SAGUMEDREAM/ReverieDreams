package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FruitLeavesBlock extends LeavesBlock implements BonemealableBlock, PolymerBlock, PolymerTexturedBlock, FactoryBlock {
    public static final MapCodec<FruitLeavesBlock> CODEC = FruitLeavesBlock.simpleCodec(FruitLeavesBlock::new);
    public static final List<FruitLeavesBlock> FRUIT_LEAVES_BLOCKS = new ArrayList<>();
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE_PROPERTY = IntegerProperty.create("fruit_age", 0, MAX_AGE);
    private Item output;
    private Block emptyLeavesBlock;

    public FruitLeavesBlock(Properties settings) {
        super(settings.noOcclusion());
        this.registerDefaultState(
                this.getStateDefinition()
                        .any()
                        .setValue(DISTANCE, 7)
                        .setValue(AGE_PROPERTY, MAX_AGE)
                        .setValue(LeavesBlock.WATERLOGGED, false)
        );
        BlockTypeGroup.FRUIT_LEAVES.add(this);
    }

    public FruitLeavesBlock(Item output, Block emptyLeavesBlock, Properties settings) {
        this(settings);
        this.emptyLeavesBlock = emptyLeavesBlock;
        this.output = output;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        boolean isGrowItem = main.getItem() == Items.BONE_MEAL || off.getItem() == Items.BONE_MEAL;

        if (!world.isClientSide() && world instanceof ServerLevel serverWorld && isGrowItem) {
            return InteractionResult.PASS;
        }

        if (isGrowItem) {
            ParticleUtils.spawnParticleInBlock(world, pos, 3, ParticleTypes.HAPPY_VILLAGER);
            Integer age = state.getValue(AGE_PROPERTY);
            if (age < MAX_AGE) {
                return InteractionResult.PASS;
            }
        }

        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            Integer age = state.getValue(AGE_PROPERTY);
            RandomSource random = world.getRandom();
            if (age >= MAX_AGE) {
                world.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 0.8f + world.random.nextFloat() * 0.4f);

                // 优化合适掉落位置
                double resultY = pos.getY();
                double resultYCloned = pos.getY();
                int tryY = (int) resultY;

                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

                final int MAX_TRY = 16;

                for (int i = 0; i < MAX_TRY; i++) {
                    mutable.set(pos.getX(), tryY - i, pos.getZ());
                    BlockState state0 = serverWorld.getBlockState(mutable);

                    if (state0.canBeReplaced()) {
                        resultY = (tryY - i);
                        break;
                    }
                }

                // 检测下面是虚空
                if (resultY < world.getMinY()) {
                    resultY = resultYCloned;
                }

                ItemEntity drop = new ItemEntity(
                        serverWorld,
                        pos.getX() + 0.5,
                        resultY + 0.3,
                        pos.getZ() + 0.5,
                        new ItemStack(this.output, random.nextIntBetweenInclusive(1, 3))
                );

                serverWorld.addFreshEntity(drop);
                drop.setPickUpDelay(10);
                world.addFreshEntity(drop);
                world.setBlockAndUpdate(pos, state.setValue(AGE_PROPERTY, 1));
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        return SoundType.GRASS;
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE_PROPERTY);
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        float f;
        int i;
        if (world.getRawBrightness(pos, 0) >= 9 && (i = this.getAge(state)) < MAX_AGE && random.nextInt((int) (25.0f / (f = getAvailableMoisture(this, world, pos))) + 1) == 0) {
            world.setBlock(pos, this.withAge(i + 1), Block.UPDATE_CLIENTS);
        }
    }

    public BlockState withAge(int age) {
        return this.defaultBlockState().setValue(AGE_PROPERTY, age);
    }

    public int getAge(BlockState state) {
        return state.getValue(AGE_PROPERTY);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return state.getValue(AGE_PROPERTY) < MAX_AGE;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        float f;
        int age = state.getValue(AGE_PROPERTY);
//        System.out.println(age);
        if (age < MAX_AGE) {
            world.setBlock(pos, state.setValue(AGE_PROPERTY, Math.min(age + 1, MAX_AGE)), Block.UPDATE_ALL_IMMEDIATE);
        }
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

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_LEAVES.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState);
    }

    @Getter
    public class Model extends BlockModel {
        private final ItemDisplayElement main;

        private final ItemStack EMPTY_MODEL;
        private final ItemStack FRUIT_MODEL;

        public Model(ServerLevel world, BlockPos pos, BlockState state) {
            ResourceLocation emptyId =
                    BuiltInRegistries.BLOCK.getKey(emptyLeavesBlock);
            ResourceLocation fruitId =
                    BuiltInRegistries.BLOCK.getKey(FruitLeavesBlock.this);

            EMPTY_MODEL = ItemDisplayElementUtil.getModel(
                    ResourceLocation.fromNamespaceAndPath(
                            emptyId.getNamespace(), "block/" + emptyId.getPath()
                    )
            );

            FRUIT_MODEL = ItemDisplayElementUtil.getModel(
                    ResourceLocation.fromNamespaceAndPath(
                            fruitId.getNamespace(), "block/" + fruitId.getPath()
                    )
            );

            main = ItemDisplayElementUtil.createSimple();
            main.setScale(new Vector3f(2));

            updateItem(state);
            addElement(main);
        }

        private void updateItem(BlockState state) {
            int age = state.getValue(AGE_PROPERTY);
            main.setItem(age >= MAX_AGE ? FRUIT_MODEL : EMPTY_MODEL);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType type) {
            if (type == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
                tick();
            }
            super.notifyUpdate(type);
        }
    }
}
