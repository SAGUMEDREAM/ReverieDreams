package cc.thonly.reverie_dreams.polymer.proxy;

import cc.thonly.reverie_dreams.block.kitchen.PlateBlock;
import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.polymer.block.PlateImpl;
import cc.thonly.reverie_dreams.proxy.PlateBlockEntityUpdater;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

public class PlateBlockEntityUpdaterImpl implements PlateBlockEntityUpdater {
    @Override
    public void handle(PlateBlockEntity blockEntity) {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Map<Long, PlateImpl.Model> longModelMap = PlateImpl.MAPPING.computeIfAbsent(serverLevel, w -> new Object2ObjectOpenHashMap<>());
        var model = longModelMap.get(blockEntity.getBlockPos().asLong());
        if (!(blockEntity.getBlockState().getBlock() instanceof PlateBlock)) {
            return;
        }
        if (model != null) {
            model.updateItem(blockEntity.getBlockState());
        }
    }
}
