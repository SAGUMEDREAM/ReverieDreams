package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GungnirThrowPacket() implements CustomPacketPayload {
    public static final GungnirThrowPacket INSTANCE = new GungnirThrowPacket();
    public static final Type<GungnirThrowPacket> PACKET_ID = new Type<>(ReverieDreams.id("gungnir_throw"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GungnirThrowPacket> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<GungnirThrowPacket> type() {
        return PACKET_ID;
    }
}
