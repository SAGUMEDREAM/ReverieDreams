package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.villager.AbstractSellerEntity;
import cc.thonly.reverie_dreams.mixin.accessor.VillagerEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerData;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public record VillagerImpl(AbstractSellerEntity seller) implements PolymerEntity {
    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.seller.level().isClientSide) {
            MinecraftServer server = this.seller.getServer();
            assert server != null;
            VillagerData modifyData = this.seller.getModifyVillagerData(server);

            SynchedEntityData.DataValue<VillagerData> entry = SynchedEntityData.DataValue.create(
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
