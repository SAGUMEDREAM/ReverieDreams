package cc.thonly.reverie_dreams.networking;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HelloPayload() implements CustomPacketPayload {
    public static final ResourceLocation hello = ReverieDreams.id("hello_payload");
    public static final CustomPacketPayload.Type<HelloPayload> PACKET_ID = new CustomPacketPayload.Type<>(hello);
    public static final StreamCodec<RegistryFriendlyByteBuf, HelloPayload> codec = StreamCodec.ofMember(HelloPayload::write, HelloPayload::read);

    public static HelloPayload read(RegistryFriendlyByteBuf buf) {
        return new HelloPayload();
    }

    public void write(RegistryFriendlyByteBuf buf) {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
