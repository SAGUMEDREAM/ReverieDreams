package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests a shot or ends the current weapon use without releasing a shot. */
public record LunarAimFirePacket(boolean fire) implements CustomPacketPayload {
    public static final Type<LunarAimFirePacket> PACKET_ID = new Type<>(ReverieDreams.id("lunar_aim_fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LunarAimFirePacket> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, LunarAimFirePacket::fire, LunarAimFirePacket::new);

    @Override
    public Type<LunarAimFirePacket> type() {
        return PACKET_ID;
    }
}
