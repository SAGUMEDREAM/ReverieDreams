package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.MoonRabbitEntity;
import cc.thonly.reverie_dreams.mixin.accessor.RabbitEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public record MoonRabbitImpl(MoonRabbitEntity moonRabbitEntity) implements PolymerEntity {

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.moonRabbitEntity.level().isClientSide()) {
            data.add(SynchedEntityData.DataValue.create(RabbitEntityAccessor.getVariant(), Rabbit.Variant.WHITE.id()));
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.RABBIT;
    }
}
