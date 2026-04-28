package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CSVersionPayload(String version) implements CustomPacketPayload {
    public static final Identifier payload = ReverieDreams.id("cs_version_payload");
    public static final Type<CSVersionPayload> PACKET_ID = new Type<>(payload);
    public static final StreamCodec<RegistryFriendlyByteBuf, CSVersionPayload> CODEC = StreamCodec.ofMember(CSVersionPayload::write, CSVersionPayload::read);

    public static CSVersionPayload read(RegistryFriendlyByteBuf buf) {
        String version = buf.readUtf();
        return new CSVersionPayload(version);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(PlatformContext.VERSION.get());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
