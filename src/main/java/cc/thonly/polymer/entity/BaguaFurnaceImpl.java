package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.ModItems;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Set;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;

public record BaguaFurnaceImpl(BaguaFurnaceEntity baguaFurnaceEntity) implements PolymerEntity {
    public BaguaFurnaceImpl {

    }

    public void setTileProjectileData(List<SynchedEntityData.DataValue<?>> data, boolean initial) {
        if (initial && !this.baguaFurnaceEntity.level().isClientSide) {
            var sendBase = true;
            for (int i = 0; i < data.size(); i++) {
                var roll = data.get(i);
                if (roll.id() == DanmakuEntity.ROLL.id() && roll.serializer() == DanmakuEntity.ROLL.serializer()) {
                    float base = (float) roll.value();
                    Quaternionf from = new Quaternionf().rotateY(Mth.HALF_PI).rotateZ(base);
                    Quaternionf to = new Quaternionf().rotateY(Mth.HALF_PI).rotateZ(base + (float) (2 * Math.PI));
                    data.set(i, SynchedEntityData.DataValue.create(DisplayTrackedData.LEFT_ROTATION, from));
                    data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.RIGHT_ROTATION, to));
                    data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.INTERPOLATION_DURATION, 20));
                    sendBase = false;
                    break;
                }

            }

            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, 3));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.INTERPOLATION_DURATION, 0));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.SCALE, new Vector3f(2f)));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TRANSLATION, new Vector3f(0, -0.1f, 0)));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.INTERPOLATION_DURATION, 2));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, 4));
            if (sendBase) {
                data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.LEFT_ROTATION, new Quaternionf().rotateX(Mth.HALF_PI)));
            }

            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.Item.ITEM, ModItems.BAGUA_FURNACE.getDefaultInstance()));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.Item.ITEM_DISPLAY, ItemDisplayContext.GROUND.getId()));
        }
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        setTileProjectileData(data, initial);
    }

    @Override
    public void onEntityTrackerTick(Set<ServerPlayerConnection> listeners) {
        PolymerEntity.super.onEntityTrackerTick(listeners);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }
}
