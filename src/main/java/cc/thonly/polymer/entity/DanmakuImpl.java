package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Set;

public record DanmakuImpl(DanmakuEntity danmakuEntity) implements PolymerEntity {
    public DanmakuImpl {

    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        setTileProjectileData(data, initial);
    }

    @Override
    public void onEntityTrackerTick(Set<PlayerAssociatedNetworkHandler> listeners) {
        PolymerEntity.super.onEntityTrackerTick(listeners);
    }

    public void setTileProjectileData(List<DataTracker.SerializedEntry<?>> data, boolean initial) {
        if (initial && !this.danmakuEntity.getWorld().isClient) {
            var sendBase = true;
            for (int i = 0; i < data.size(); i++) {
                var roll = data.get(i);
                if (roll.id() == DanmakuEntity.ROLL.id() && roll.handler() == DanmakuEntity.ROLL.dataType()) {
                    data.set(i, DataTracker.SerializedEntry.of(DisplayTrackedData.LEFT_ROTATION, new Quaternionf().rotateY(MathHelper.HALF_PI).rotateZ((float) roll.value())));
                    sendBase = false;
                    break;
                }
            }

            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TELEPORTATION_DURATION, 3));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.INTERPOLATION_DURATION, 0));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.SCALE, new Vector3f(this.danmakuEntity.getScale() * 0.85f)));
            if (this.danmakuEntity.getTile()) {
                data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.BILLBOARD, (byte) DisplayEntity.BillboardMode.CENTER.ordinal()));
            } else {
                data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TRANSLATION, new Vector3f(0, -0.1f, 0)));
                data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.INTERPOLATION_DURATION, 2));
                data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TELEPORTATION_DURATION, 4));
                if (sendBase) {
                    data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.LEFT_ROTATION, new Quaternionf().rotateX(MathHelper.HALF_PI)));
                }
            }

            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM, this.danmakuEntity.getItemStack()));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM_DISPLAY, ItemDisplayContext.GUI.getIndex()));
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }

}
