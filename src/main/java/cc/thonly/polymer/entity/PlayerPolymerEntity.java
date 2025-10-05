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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface PlayerPolymerEntity extends PolymerEntity, PolymerHolderEntity {

    @Override
    default void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        PolymerEntity.super.onEntityPacketSent(consumer, packet);
        if (packet instanceof EntitySetHeadYawS2CPacket headYawS2CPacket) {
            var ent = this.getEntity();
            consumer.accept(new EntityS2CPacket.Rotate(ent.getId(), MathHelper.packDegrees(headYawS2CPacket.getHeadYaw()), (byte) (ent.getPitch() * 256.0F / 360.0F), ent.isOnGround()));
        }
    }

    @Override
    default List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(List<Pair<EquipmentSlot, ItemStack>> items, ServerPlayerEntity player) {
        return PolymerEntity.super.getPolymerVisibleEquipment(items, player);
    }

    @Override
    default void onBeforeSpawnPacket(ServerPlayerEntity player, Consumer<Packet<?>> packetConsumer) {
        PlayerListS2CPacket packet = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(PlayerListS2CPacket.Action.ADD_PLAYER));
        GameProfile profile = new GameProfile(this.getEntity().getUuid(), "");
        profile.getProperties().put("textures", this.getSkin());
        List<PlayerListS2CPacket.Entry> entries = packet.getEntries();
        entries.add(new PlayerListS2CPacket.Entry(
                profile.getId(),
                profile,
                false,
                Integer.MAX_VALUE,
                GameMode.ADVENTURE,
                Text.empty(),
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
    default void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        data.add(DataTracker.SerializedEntry.of(
                PlayerEntityAccessor.getPlayerModelParts(),
                (byte) (0xFF & ~0x01)
        ));
        data.add(DataTracker.SerializedEntry.of(
                EntityAccessor.getNameVisible(),
                false
        ));
    }

    default void onTrackingStopped(ServerPlayerEntity player) {
        var e = this.getEntity();
        ItemDisplayElement element = PolymerEntityHelper.POLYMER_PLAYER_ELEMENTS.get(e);
        if (element != null) {
            ElementHolder holder = element.getHolder();
            if (holder != null) {
                holder.destroy();
            }
        }
        PolymerEntityHelper.POLYMER_PLAYER_ELEMENTS.remove(e);
        player.networkHandler.sendPacket(new PlayerRemoveS2CPacket(List.of((this.getEntity().getUuid()))));
    }

    @Override
    default void onEntityTrackerTick(Set<PlayerAssociatedNetworkHandler> listeners) {
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