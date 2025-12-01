package cc.thonly.polymer.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.reverie_dreams.mixin.accessor.EntityAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.PlayerEntityAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface PlayerPolymerEntity extends PolymerEntity, PolymerHolderEntity {

    @Override
    default void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        PolymerEntity.super.onEntityPacketSent(consumer, packet);
        if (packet instanceof ClientboundRotateHeadPacket headYawS2CPacket) {
            var ent = this.getEntity();
            consumer.accept(new ClientboundMoveEntityPacket.Rot(ent.getId(), Mth.packDegrees(headYawS2CPacket.getYHeadRot()), (byte) (ent.getXRot() * 256.0F / 360.0F), ent.onGround()));
        }
    }

    @Override
    default List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(List<Pair<EquipmentSlot, ItemStack>> items, ServerPlayer player) {
        return PolymerEntity.super.getPolymerVisibleEquipment(items, player);
    }

    @Override
    default void onBeforeSpawnPacket(ServerPlayer player, Consumer<Packet<?>> packetConsumer) {
        ClientboundPlayerInfoUpdatePacket packet = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER));
        GameProfile profile = new GameProfile(this.getEntity().getUUID(), "");
        profile.getProperties().put("textures", this.getSkin());
        List<ClientboundPlayerInfoUpdatePacket.Entry> entries = packet.entries();
        entries.add(new ClientboundPlayerInfoUpdatePacket.Entry(
                profile.getId(),
                profile,
                false,
                Integer.MAX_VALUE,
                GameType.ADVENTURE,
                Component.empty(),
                true,
                0,
                null)
        );
        packetConsumer.accept(packet);
    }

    default void sendRefreshPacket() {
        var e = this.getEntity();
       PolymerEntityUtils.refreshEntity(e);
    }

    @Override
    default void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        data.add(SynchedEntityData.DataValue.create(
                PlayerEntityAccessor.getPlayerModelParts(),
                (byte) (0xFF & ~0x01)
        ));
        data.add(SynchedEntityData.DataValue.create(
                EntityAccessor.getNameVisible(),
                false
        ));
    }

    default void onTrackingStopped(ServerPlayer player) {
        var e = this.getEntity();
        ItemDisplayElement element = PolymerEntityHelper.POLYMER_PLAYER_ELEMENTS.get(e);
        if (element != null) {
            ElementHolder holder = element.getHolder();
            if (holder != null) {
                holder.destroy();
            }
        }
        PolymerEntityHelper.POLYMER_PLAYER_ELEMENTS.remove(e);
        player.connection.send(new ClientboundPlayerInfoRemovePacket(List.of((this.getEntity().getUUID()))));
    }

    @Override
    default void onEntityTrackerTick(Set<ServerPlayerConnection> listeners) {
        PolymerEntity.super.onEntityTrackerTick(listeners);
        var e = this.getEntity();

    }

    @Override
    default EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PLAYER;
    }

    LivingEntity getEntity();

    Property getSkin();
}