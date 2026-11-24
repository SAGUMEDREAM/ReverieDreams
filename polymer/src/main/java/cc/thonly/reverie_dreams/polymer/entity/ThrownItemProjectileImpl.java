package cc.thonly.reverie_dreams.polymer.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;

import java.util.List;

public record ThrownItemProjectileImpl(ThrowableItemProjectile projectile) implements PolymerEntity {
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.SNOWBALL;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        if (initial) {
            // The server's default item differs from the snowball client's default.
            var item = ThrowableItemProjectile.DATA_ITEM_STACK;
            data.removeIf(value -> value.id() == item.id());
            data.add(SynchedEntityData.DataValue.create(item, projectile.getItem().copy()));
        }
    }
}
