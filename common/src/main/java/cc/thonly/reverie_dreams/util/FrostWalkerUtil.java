package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public final class FrostWalkerUtil {
    public static final Random RANDOM = new Random();

    public static void freezeWater(
            Level level,
            BlockPos center,
            int radius
    ) {
        int minY = center.getY() - 1;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, 0, -radius),
                center.offset(radius, 0, radius)
        )) {
            BlockPos waterPos = new BlockPos(
                    pos.getX(),
                    minY,
                    pos.getZ()
            );

            BlockState state = level.getBlockState(waterPos);

            if (!state.is(Blocks.WATER)) {
                continue;
            }

            BlockPos above = waterPos.above();

            if (!level.getBlockState(above).isAir()) {
                continue;
            }

            level.setBlock(
                    waterPos,
                    RANDOM.nextBoolean() ? Blocks.ICE.defaultBlockState() : RDBlocks.MAGIC_ICE_BLOCK.asBlockState(),
                    3
            );
        }
    }

}