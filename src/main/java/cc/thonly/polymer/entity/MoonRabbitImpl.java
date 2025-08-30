package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.MoonRabbitEntity;
import cc.thonly.reverie_dreams.mixin.accessor.RabbitEntityAccessor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public record MoonRabbitImpl(MoonRabbitEntity moonRabbitEntity) implements PolymerEntity {

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.moonRabbitEntity.getWorld().isClient) {
            data.add(DataTracker.SerializedEntry.of(RabbitEntityAccessor.getVariant(), RabbitEntity.Variant.WHITE.getIndex()));
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.RABBIT;
    }
}
