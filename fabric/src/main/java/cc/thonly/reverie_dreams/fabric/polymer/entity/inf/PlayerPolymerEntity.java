package cc.thonly.reverie_dreams.fabric.polymer.entity.inf;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.fabric.polymer.helper.PolymerEntityHelper;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.mixin.accessor.EntityAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.AvatarAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.MannequinAccessor;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
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
import net.minecraft.world.entity.*;
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
        var packet = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER));
        var profile = new GameProfile((this.getEntity()).getUUID(), "", new PropertyMap(ImmutableMultimap.of("textures", this.getSkin())));
        packet.entries().add(new ClientboundPlayerInfoUpdatePacket.Entry(
                profile.id(),
                profile,
                false,
                Integer.MAX_VALUE,
                GameType.ADVENTURE,
                Component.empty(),
                true,
                0,
                null
        ));
        packetConsumer.accept(packet);
    }

    default void sendRefreshPacket() {
        var e = this.getEntity();
        PolymerEntityUtils.refreshEntity(e);
    }

    @Override
    default void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        if (!(this.getEntity() instanceof BaseNPCLikeEntity)) {
            ReverieDreams.LOGGER.error("%s is not class BaseNPCLikeEntity.class".formatted(this.getEntity()));
            return;
        }
        var e = (BaseNPCLikeEntity) this.getEntity();
        data.removeIf(x -> x.id() >= AvatarAccessor.getPlayerMainHand().id());
        if (initial) {
            data.add(SynchedEntityData.DataValue.create(
                    AvatarAccessor.getPlayerModelParts(),
                    MannequinAccessor.getAllLayers()
            ));
            data.add(SynchedEntityData.DataValue.create(
                    AvatarAccessor.getPlayerMainHand(),
                    Avatar.DEFAULT_MAIN_HAND
            ));
            data.add(SynchedEntityData.DataValue.create(
                    EntityAccessor.getNameVisible(),
                    false
            ));
        }
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
        if (!(this.getEntity() instanceof BaseNPCLikeEntity)) {
            ReverieDreams.LOGGER.error("%s is not class BaseNPCLikeEntity.class".formatted(this.getEntity()));
            return;
        }
        var e = (BaseNPCLikeEntity) this.getEntity();

    }

    @Override
    default EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PLAYER;
    }

    LivingEntity getEntity();

    Property getSkin();
}