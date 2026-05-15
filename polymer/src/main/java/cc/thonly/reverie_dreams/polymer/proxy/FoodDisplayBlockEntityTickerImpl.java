package cc.thonly.reverie_dreams.polymer.proxy;

import cc.thonly.reverie_dreams.block.entity.FoodDisplayBlockEntity;
import cc.thonly.reverie_dreams.polymer.block.ItemStackDisplayImpl;
import cc.thonly.reverie_dreams.proxy.FoodDisplayBlockEntityTicker;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class FoodDisplayBlockEntityTickerImpl implements FoodDisplayBlockEntityTicker {

    @Override
    public void handle(Level world, BlockPos pos, BlockState state, FoodDisplayBlockEntity blockEntity) {
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }

        Map<Long, ItemStackDisplayImpl.Model> longModelMap = ItemStackDisplayImpl.MAPPING.computeIfAbsent(serverWorld, w -> new Object2ObjectOpenHashMap<>());
        var model = longModelMap.get(pos.asLong());
        if (model != null) {
            model.updateItem(state);
        }
    }
}
