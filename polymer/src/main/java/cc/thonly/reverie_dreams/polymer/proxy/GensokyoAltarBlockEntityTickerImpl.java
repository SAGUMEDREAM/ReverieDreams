package cc.thonly.reverie_dreams.polymer.proxy;

import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.polymer.block.GensokyoAltarImpl;
import cc.thonly.reverie_dreams.proxy.GensokyoAltarBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GensokyoAltarBlockEntityTickerImpl implements GensokyoAltarBlockEntityTicker {
    @Override
    public void handle(Level world, BlockPos pos, BlockState state, GensokyoAltarBlockEntity blockEntity) {
        if (blockEntity.tick > 5) {
            GensokyoAltarImpl.Model altarModel = GensokyoAltarImpl.POS_TO_MODEL.get(pos.asLong());
            if (altarModel != null) {
                altarModel.update();
            }
            blockEntity.tick = 0;
        }
        GensokyoAltarImpl.Model altarModel = GensokyoAltarImpl.POS_TO_MODEL.get(pos.asLong());
        if (altarModel != null) {
            altarModel.angle += 2f;
            if (altarModel.angle >= 360) {
                altarModel.angle = 0;
            }
        }
        blockEntity.tick++;
    }
}
