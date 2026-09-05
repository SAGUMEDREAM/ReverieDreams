package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.villager.AbstractSeller;
import cc.thonly.reverie_dreams.mixin.accessor.VillagerEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.level.Level;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public record VillagerImpl(AbstractSeller seller) implements PolymerEntity {
    @SuppressWarnings("resource")
    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        Level level = this.seller.level();
        if (initial && !level.isClientSide()) {
            VillagerData villagerDataCache = this.seller.getVillagerDataCache();
            VillagerData modifyData = villagerDataCache == null ? this.seller.getModifyVillagerData(level.registryAccess()) : villagerDataCache;
            if (modifyData == null) {
                return;
            }
            this.seller.setVillagerDataCache(modifyData);

            SynchedEntityData.DataValue<VillagerData> entry = SynchedEntityData.DataValue.create(
                    VillagerEntityAccessor.getVillagerData(),
                    modifyData);

            data.add(entry);
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.VILLAGER;
    }
}
