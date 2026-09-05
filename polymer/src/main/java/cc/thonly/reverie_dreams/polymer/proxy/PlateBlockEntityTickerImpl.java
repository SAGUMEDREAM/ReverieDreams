package cc.thonly.reverie_dreams.polymer.proxy;

import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.polymer.block.PlateImpl;
import cc.thonly.reverie_dreams.proxy.PlateBlockEntityTicker;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class PlateBlockEntityTickerImpl implements PlateBlockEntityTicker {

    @Override
    public void handle(Level world, BlockPos pos, BlockState state, PlateBlockEntity blockEntity) {
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }

        Map<Long, PlateImpl.Model> longModelMap = PlateImpl.MAPPING.computeIfAbsent(serverWorld, w -> new Object2ObjectOpenHashMap<>());
        var model = longModelMap.get(pos.asLong());
        if (model != null) {
            model.updateItem(state);
        }
    }
}
