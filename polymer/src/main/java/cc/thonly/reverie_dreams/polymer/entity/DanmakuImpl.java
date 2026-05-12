package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import java.util.Set;

@SuppressWarnings("resource")
public record DanmakuImpl(DanmakuEntity danmakuEntity) implements PolymerEntity {
    public static final Brightness BRIGHTNESS = new Brightness(15, 15);
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
        if (initial && !this.danmakuEntity.level().isClientSide()) {
            var sendBase = true;
            SynchedEntityData.DataValue<?> rRoll = null;
            for (int i = 0; i < data.size(); i++) {
                var roll = data.get(i);
                if (roll.id() == DanmakuEntity.ROLL.id() && roll.serializer() == DanmakuEntity.ROLL.serializer()) {
                    data.set(i, SynchedEntityData.DataValue.create(
                            DisplayEntityData.LEFT_ROTATION,
                            new Quaternionf()
                                    .rotateY(Mth.HALF_PI)
                                    .rotateZ((float) roll.value())));
                    sendBase = false;
                    rRoll = roll;
                    break;
                }
            }
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.BRIGHTNESS, BRIGHTNESS.pack()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.TELEPORTATION_DURATION, 3));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.INTERPOLATION_DURATION, 0));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.SCALE, new Vector3f(this.danmakuEntity.getDanmakuProperties().scale() * 0.85f)));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.Item.ITEM, this.danmakuEntity.getItemStack()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.Item.ITEM_DISPLAY, ItemDisplayContext.GROUND.getId()));
            if (this.danmakuEntity.getDanmakuProperties().tile()) {
                data.add(SynchedEntityData.DataValue.create(DisplayEntityData.BILLBOARD, (byte) Display.BillboardConstraints.CENTER.ordinal()));
            } else {
                data.add(SynchedEntityData.DataValue.create(DisplayEntityData.TRANSLATION, new Vector3f(0, 0, 0)));
                data.add(SynchedEntityData.DataValue.create(DisplayEntityData.INTERPOLATION_DURATION, 2));
                data.add(SynchedEntityData.DataValue.create(DisplayEntityData.TELEPORTATION_DURATION, 4));
                if (sendBase) {
                    data.add(SynchedEntityData.DataValue.create(DisplayEntityData.LEFT_ROTATION, new Quaternionf().rotateX(Mth.HALF_PI)));
                }
            }
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }

}
