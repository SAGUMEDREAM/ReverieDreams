package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.ModItems;
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

public record BaguaFurnaceImpl(BaguaFurnaceEntity baguaFurnaceEntity) implements PolymerEntity {
    public BaguaFurnaceImpl {

    }

    public void setTileProjectileData(List<DataTracker.SerializedEntry<?>> data, boolean initial) {
        if (initial && !this.baguaFurnaceEntity.getWorld().isClient) {
            var sendBase = true;
            for (int i = 0; i < data.size(); i++) {
                var roll = data.get(i);
                if (roll.id() == DanmakuEntity.ROLL.id() && roll.handler() == DanmakuEntity.ROLL.dataType()) {
                    float base = (float) roll.value();
                    Quaternionf from = new Quaternionf().rotateY(MathHelper.HALF_PI).rotateZ(base);
                    Quaternionf to = new Quaternionf().rotateY(MathHelper.HALF_PI).rotateZ(base + (float) (2 * Math.PI));
                    data.set(i, DataTracker.SerializedEntry.of(DisplayTrackedData.LEFT_ROTATION, from));
                    data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.RIGHT_ROTATION, to));
                    data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.INTERPOLATION_DURATION, 20));
                    sendBase = false;
                    break;
                }

            }

            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TELEPORTATION_DURATION, 3));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.INTERPOLATION_DURATION, 0));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.SCALE, new Vector3f(2f)));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TRANSLATION, new Vector3f(0, -0.1f, 0)));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.INTERPOLATION_DURATION, 2));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TELEPORTATION_DURATION, 4));
            if (sendBase) {
                data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.LEFT_ROTATION, new Quaternionf().rotateX(MathHelper.HALF_PI)));
            }

            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM, ModItems.BAGUA_FURNACE.getDefaultStack()));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM_DISPLAY, ItemDisplayContext.GROUND.getIndex()));
        }
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

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }
}
