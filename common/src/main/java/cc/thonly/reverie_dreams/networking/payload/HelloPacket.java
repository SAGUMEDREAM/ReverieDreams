package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HelloPacket() implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("hello_payload");
    public static final Type<HelloPacket> PACKET_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, HelloPacket> CODEC = StreamCodec.ofMember(HelloPacket::write, HelloPacket::read);

    public static HelloPacket read(RegistryFriendlyByteBuf buf) {
        return new HelloPacket();
    }

    public void write(RegistryFriendlyByteBuf buf) {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
