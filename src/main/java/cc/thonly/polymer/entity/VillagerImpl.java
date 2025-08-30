package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.villager.AbstractSellerEntity;
import cc.thonly.reverie_dreams.mixin.accessor.VillagerEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.VillagerData;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public record VillagerImpl(AbstractSellerEntity seller) implements PolymerEntity {
    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.seller.getWorld().isClient) {
            MinecraftServer server = this.seller.getServer();
            assert server != null;
            VillagerData modifyData = this.seller.getModifyVillagerData(server);

            DataTracker.SerializedEntry<VillagerData> entry = DataTracker.SerializedEntry.of(
                    VillagerEntityAccessor.VILLAGER_DATA(),
                    modifyData);

            data.add(entry);
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.VILLAGER;
    }
}
