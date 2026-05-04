package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

@Slf4j
public record ScreenshotMapPacket(UUID sessionId, byte[] pixels) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("screenshot_map");
    public static final Type<ScreenshotMapPacket> PACKET_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenshotMapPacket> CODEC = StreamCodec.ofMember(ScreenshotMapPacket::write, ScreenshotMapPacket::read);

    private static ScreenshotMapPacket read(RegistryFriendlyByteBuf buf) {
        UUID sessionId = buf.readUUID();
        int len = buf.readVarInt();
        byte[] pixels;
        try {
            pixels = new byte[len];
            buf.readBytes(pixels);
        } catch (Exception err) {
            log.error("Error: ", err);
            pixels = new byte[0];
        }
        return new ScreenshotMapPacket(sessionId, pixels);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.sessionId);
        buf.writeVarInt(this.pixels.length);
        buf.writeBytes(this.pixels);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
