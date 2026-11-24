package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.DanmakuLaserEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * 激光的 Polymer 代理：映射成 ItemDisplay，用 laser 物品沿激光方向拉伸摆放。
 *
 * <p>近似代理：把物品平面用 LEFT_ROTATION 对齐激光方向，再沿该轴按
 * {@code length/4} 拉伸（激光实体本体不是弹幕，物品变换只求原版客户端
 * 能看到一条方向正确、粗细近似的光条；状态切换预警/激活不实时更新）。
 */
public record LaserImpl(DanmakuLaserEntity entity) implements PolymerEntity {
    public static final Brightness BRIGHTNESS = new Brightness(15, 15);

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.entity.level().isClientSide()) {
            var direction = this.entity.beamDirection();
            Vector3f target = new Vector3f((float) direction.x, (float) direction.y, (float) direction.z);
            Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(0.0f, 0.0f, 1.0f), target);
            float width = this.entity.getBeamWidth();
            float length = this.entity.getBeamLength();
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.BRIGHTNESS, BRIGHTNESS.pack()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.Item.ITEM, this.entity.getDisplayItem()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.Item.ITEM_DISPLAY, ItemDisplayContext.FIXED.getId()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.LEFT_ROTATION, rotation));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.SCALE,
                    new Vector3f(Math.max(0.2f, width), Math.max(0.2f, width), length / 4f)));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.TELEPORTATION_DURATION, 1));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.INTERPOLATION_DURATION, 0));
        }
    }
}
