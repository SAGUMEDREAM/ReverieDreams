package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record StartScreenshotPacket(UUID sessionId) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("start_screenshot_map");
    public static final Type<StartScreenshotPacket> PACKET_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, StartScreenshotPacket> CODEC = StreamCodec.ofMember(StartScreenshotPacket::write, StartScreenshotPacket::read);

    private static StartScreenshotPacket read(RegistryFriendlyByteBuf buf) {
        return new StartScreenshotPacket(buf.readUUID());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.sessionId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
