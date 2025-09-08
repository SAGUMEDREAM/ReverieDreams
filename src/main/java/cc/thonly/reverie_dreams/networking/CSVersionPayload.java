package cc.thonly.reverie_dreams.networking;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CSVersionPayload(String version) implements CustomPayload {
    public static final Identifier payload = Touhou.id("cs_version_payload");
    public static final CustomPayload.Id<CSVersionPayload> PACKET_ID = new CustomPayload.Id<>(payload);
    public static final PacketCodec<RegistryByteBuf, CSVersionPayload> codec = PacketCodec.of(CSVersionPayload::write, CSVersionPayload::read);

    public static CSVersionPayload read(RegistryByteBuf buf) {
        String version = buf.readString();
        return new CSVersionPayload(version);
    }

    public void write(RegistryByteBuf buf) {
        buf.writeString(ConstantInfo.VERSION);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
