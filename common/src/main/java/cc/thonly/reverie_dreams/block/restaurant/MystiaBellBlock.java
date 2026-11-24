package cc.thonly.reverie_dreams.block.restaurant;

import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

public final class MystiaBellBlock extends RestaurantBlock {
    public static final MapCodec<MystiaBellBlock> CODEC = simpleCodec(MystiaBellBlock::new);

    public MystiaBellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<MystiaBellBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RestaurantBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(2, 0, 2, 14, 16, 14);
    }
}
