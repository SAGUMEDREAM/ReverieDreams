package cc.thonly.reverie_dreams.proxy;

import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface PlateBlockEntityTicker {
    void handle(Level world, BlockPos pos, BlockState state, PlateBlockEntity blockEntity);
}
