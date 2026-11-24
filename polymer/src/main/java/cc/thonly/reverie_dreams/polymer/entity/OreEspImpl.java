package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.OreEspEntity;
import cc.thonly.reverie_dreams.mixin.accessor.BlockDisplayAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record OreEspImpl(OreEspEntity oreEspEntity) implements PolymerEntity {
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.BLOCK_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        for (int i = 0; i < data.size(); i++) {
            // 原版客户端解析不了模组方块的 block state，替换成原版满方块；
            // 该方块状态只用来提供轮廓形状，颜色由发光颜色决定。
            if (data.get(i).value() instanceof BlockState state && !isVanillaBlock(state)) {
                data.set(i, SynchedEntityData.DataValue.create(
                        BlockDisplayAccessor.reverie_dreams$getBlockStateData(),
                        Blocks.STONE.defaultBlockState()));
            }
        }
    }

    private static boolean isVanillaBlock(BlockState state) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id != null && "minecraft".equals(id.getNamespace());
    }
}
