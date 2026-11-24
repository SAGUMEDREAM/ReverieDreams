package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.DanmakuFamiliarEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

import java.util.List;

/**
 * 使魔的 Polymer 代理：映射成 ItemDisplay，展示 familiar 携带的渲染物品栈
 * （缩放参考客户端渲染器的 0.5，乘 FAMILIAR 类型 scale 0.8 保持与原生视觉接近）。
 */
public record FamiliarImpl(DanmakuFamiliarEntity entity) implements PolymerEntity {
    public static final Brightness BRIGHTNESS = new Brightness(15, 15);

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        if (initial && !this.entity.level().isClientSide()) {
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.BRIGHTNESS, BRIGHTNESS.pack()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.Item.ITEM, this.entity.getDisplayItem()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.Item.ITEM_DISPLAY, ItemDisplayContext.GROUND.getId()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.BILLBOARD, (byte) Display.BillboardConstraints.CENTER.ordinal()));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.SCALE, new Vector3f(0.4f)));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.TELEPORTATION_DURATION, 1));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.INTERPOLATION_DURATION, 2));
        }
    }
}
