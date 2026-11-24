package cc.thonly.reverie_dreams.block.nature;

import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.tags.BlockTags;

import java.util.ArrayList;
import java.util.List;

public class FicusMicrocarpaBlock extends MultifaceBlock implements BonemealableBlock {
    public static final MapCodec<FicusMicrocarpaBlock> CODEC = simpleCodec(FicusMicrocarpaBlock::new);

    public FicusMicrocarpaBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends MultifaceBlock> codec() {
        return CODEC;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        Direction clicked = hit.getDirection();
        Direction face = hasFace(state, clicked)
                ? clicked
                : availableFaces(state).stream().findFirst().orElse(null);
        if (face == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        RandomSource random = level.getRandom();
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                1.0F, 0.8F + random.nextFloat() * 0.4F);
        Block.popResourceFromFace(level, pos, face,
                new ItemStack(RDIngredientItems.FICUS_MICROCARPA.asItem(), random.nextIntBetweenInclusive(1, 2)));

        BlockState remaining = state.setValue(getFaceProperty(face), false);
        if (remaining.getValue(WATERLOGGED)) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        } else if (!hasAnyFace(remaining)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, remaining, Block.UPDATE_ALL);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) == 0) {
            growFace(state, level, pos, random);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !growableFaces(level, pos, state).isEmpty();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int count = random.nextIntBetweenInclusive(1, 2);
        BlockState current = state;
        for (int i = 0; i < count; i++) {
            if (!growFace(current, level, pos, random)) {
                return;
            }
            current = level.getBlockState(pos);
        }
    }

    private static boolean growFace(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        List<Direction> faces = growableFaces(level, pos, state);
        if (faces.isEmpty()) {
            return false;
        }
        Direction face = faces.get(random.nextInt(faces.size()));
        level.setBlock(pos, state.setValue(getFaceProperty(face), true), Block.UPDATE_CLIENTS);
        return true;
    }

    private static List<Direction> growableFaces(LevelReader level, BlockPos pos, BlockState state) {
        List<Direction> faces = new ArrayList<>(4);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (hasFace(state, direction)) {
                continue;
            }
            if (!level.getBlockState(pos.relative(direction)).is(BlockTags.LOGS)) {
                continue;
            }
            if (canAttachTo(level, pos, direction)) {
                faces.add(direction);
            }
        }
        return faces;
    }
}
