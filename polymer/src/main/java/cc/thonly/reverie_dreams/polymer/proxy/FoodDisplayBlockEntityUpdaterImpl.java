package cc.thonly.reverie_dreams.polymer.proxy;

import cc.thonly.reverie_dreams.block.FoodDisplayBlock;
import cc.thonly.reverie_dreams.block.entity.FoodDisplayBlockEntity;
import cc.thonly.reverie_dreams.polymer.block.ItemStackDisplayImpl;
import cc.thonly.reverie_dreams.proxy.FoodDisplayBlockEntityUpdater;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

public class FoodDisplayBlockEntityUpdaterImpl implements FoodDisplayBlockEntityUpdater {
    @Override
    public void handle(FoodDisplayBlockEntity blockEntity) {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Map<Long, ItemStackDisplayImpl.Model> longModelMap = ItemStackDisplayImpl.MAPPING.computeIfAbsent(serverLevel, w -> new Object2ObjectOpenHashMap<>());
        var model = longModelMap.get(blockEntity.getBlockPos().asLong());
        if (!(blockEntity.getBlockState().getBlock() instanceof FoodDisplayBlock)) {
            return;
        }
        if (model != null) {
            model.updateItem(blockEntity.getBlockState());
        }
    }
}
