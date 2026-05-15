package cc.thonly.reverie_dreams.proxy;

import cc.thonly.reverie_dreams.block.entity.FoodDisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface FoodDisplayBlockEntityTicker {
    void handle(Level world, BlockPos pos, BlockState state, FoodDisplayBlockEntity blockEntity);
}
