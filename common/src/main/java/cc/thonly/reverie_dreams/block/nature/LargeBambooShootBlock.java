package cc.thonly.reverie_dreams.block.nature;

import cc.thonly.reverie_dreams.registry.content.block.RDBambooBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * Dig for a transplant, harvest for the existing ingredient, or leave it to grow into its own species.
 */
public final class LargeBambooShootBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<LargeBambooShootBlock> CODEC = simpleCodec(LargeBambooShootBlock::new);
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2);

    public LargeBambooShootBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    public MapCodec<LargeBambooShootBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return LargeBambooBlock.soil(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return box(4 - age, 0, 4 - age, 12 + age, 6 + age * 4, 12 + age);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) == 0 && level.getRawBrightness(pos.above(), 0) >= 9)
            advance(level, pos, state);
    }

    public boolean advance(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.is(this) || !canSurvive(state, level, pos) || !LargeBambooBlock.clear(level, pos.above()))
            return false;
        if (state.getValue(AGE) < 2)
            level.setBlock(pos, state.cycle(AGE), Block.UPDATE_CLIENTS);
        else
            level.setBlock(pos, RDBambooBlocks.LARGE_BAMBOO.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    public boolean harvest(ServerLevel level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this) || state.getValue(AGE) != 2)
            return false;
        level.removeBlock(pos, false);
        popResource(level, pos, new ItemStack(RDIngredientItems.BAMBOO_SHOOTS.asItem(), 1 + level.getRandom().nextInt(2)));
        return true;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof HoeItem) {
            if (level instanceof ServerLevel server) {
                level.removeBlock(pos, false);
                popResource(level, pos, new ItemStack(RDBambooBlocks.LARGE_BAMBOO_SHOOT));
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.BONE_MEAL))
            return InteractionResult.PASS;
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) != 2)
            return InteractionResult.PASS;
        if (level instanceof ServerLevel server)
            harvest(server, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return LargeBambooBlock.clear(level, pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        advance(level, pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
