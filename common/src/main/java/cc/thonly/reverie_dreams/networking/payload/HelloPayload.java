package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HelloPayload() implements CustomPacketPayload {
    public static final Identifier hello = ReverieDreams.id("hello_payload");
    public static final Type<HelloPayload> PACKET_ID = new Type<>(hello);
    public static final StreamCodec<RegistryFriendlyByteBuf, HelloPayload> CODEC = StreamCodec.ofMember(HelloPayload::write, HelloPayload::read);

    public static HelloPayload read(RegistryFriendlyByteBuf buf) {
        return new HelloPayload();
    }

    public void write(RegistryFriendlyByteBuf buf) {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
