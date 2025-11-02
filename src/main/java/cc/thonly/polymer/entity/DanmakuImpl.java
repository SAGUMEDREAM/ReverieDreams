package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
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
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;

public record DanmakuImpl(DanmakuEntity danmakuEntity) implements PolymerEntity {
    public DanmakuImpl {

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

    public void setTileProjectileData(List<SynchedEntityData.DataValue<?>> data, boolean initial) {
        if (initial && !this.danmakuEntity.level().isClientSide) {
            var sendBase = true;
            for (int i = 0; i < data.size(); i++) {
                var roll = data.get(i);
                if (roll.id() == DanmakuEntity.ROLL.id() && roll.serializer() == DanmakuEntity.ROLL.serializer()) {
                    data.set(i, SynchedEntityData.DataValue.create(
                            DisplayTrackedData.LEFT_ROTATION,
                            new Quaternionf()
                                    .rotateY(Mth.HALF_PI)
                                    .rotateZ((float) roll.value())));
                    sendBase = false;
                    break;
                }
            }

            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, 3));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.INTERPOLATION_DURATION, 0));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.SCALE, new Vector3f(this.danmakuEntity.getProperties().getScale() * 0.85f * 0.65f)));
            if (this.danmakuEntity.getProperties().isTile()) {
                data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.BILLBOARD, (byte) Display.BillboardConstraints.CENTER.ordinal()));
            } else {
                data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TRANSLATION, new Vector3f(0, -0.1f, 0)));
                data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.INTERPOLATION_DURATION, 2));
                data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, 4));
                if (sendBase) {
                    data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.LEFT_ROTATION, new Quaternionf().rotateX(Mth.HALF_PI)));
                }
            }

            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.Item.ITEM, this.danmakuEntity.getPickupItemStackOrigin()));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.Item.ITEM_DISPLAY, ItemDisplayContext.GUI.getId()));
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }

}
