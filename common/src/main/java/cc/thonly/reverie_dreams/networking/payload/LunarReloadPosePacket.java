package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/** Owner-only first-person feedback after the server accepts a magazine swap. */
public record LunarReloadPosePacket(boolean offHand, int weaponNumber) implements CustomPacketPayload {
    public static final Type<LunarReloadPosePacket> PACKET_ID = new Type<>(ReverieDreams.id("lunar_reload_pose"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LunarReloadPosePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, LunarReloadPosePacket::offHand,
            ByteBufCodecs.VAR_INT, LunarReloadPosePacket::weaponNumber,
            LunarReloadPosePacket::new);

    public static void send(ServerPlayer player, InteractionHand hand, int weaponNumber) {
        if (player.connection != null && NetworkManager.canPlayerReceive(player, PACKET_ID)) {
            NetworkManager.sendToPlayer(player, new LunarReloadPosePacket(hand == InteractionHand.OFF_HAND, weaponNumber));
        }
    }

    @Override
    public Type<LunarReloadPosePacket> type() {
        return PACKET_ID;
    }
}
