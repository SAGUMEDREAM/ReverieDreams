package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/** Visual feedback for a shot the server accepted. */
public record LunarShotPosePacket(int entityId, boolean offHand, int weaponNumber) implements CustomPacketPayload {
    public static final Type<LunarShotPosePacket> PACKET_ID = new Type<>(ReverieDreams.id("lunar_shot_pose"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LunarShotPosePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LunarShotPosePacket::entityId,
            ByteBufCodecs.BOOL, LunarShotPosePacket::offHand,
            ByteBufCodecs.VAR_INT, LunarShotPosePacket::weaponNumber,
            LunarShotPosePacket::new);

    public static void broadcast(ServerPlayer shooter, InteractionHand hand, LunarWeaponType weapon) {
        var pose = new LunarShotPosePacket(shooter.getId(), hand == InteractionHand.OFF_HAND, weapon.number());
        @SuppressWarnings("unchecked")
        var packet = (Packet<? super ClientGamePacketListener>) NetworkManager.toPacket(
                NetworkManager.Side.S2C, pose, shooter.registryAccess());
        shooter.level().getChunkSource().chunkMap.sendToTrackingPlayersFiltered(shooter, packet,
                viewer -> NetworkManager.canPlayerReceive(viewer, PACKET_ID));
        if (NetworkManager.canPlayerReceive(shooter, PACKET_ID)) {
            shooter.connection.send(packet);
        }
    }

    @Override
    public Type<LunarShotPosePacket> type() {
        return PACKET_ID;
    }
}
