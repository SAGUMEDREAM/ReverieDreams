package cc.thonly.reverie_dreams.fabric.polymer.entity;

import cc.thonly.reverie_dreams.entity.MoonRabbit;
import cc.thonly.reverie_dreams.mixin.accessor.RabbitEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public record MoonRabbitImpl(MoonRabbit moonRabbit) implements PolymerEntity {

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.moonRabbit.level().isClientSide()) {
            data.add(SynchedEntityData.DataValue.create(RabbitEntityAccessor.getVariant(), Rabbit.Variant.WHITE.id()));
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.RABBIT;
    }
}
