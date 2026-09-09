package cc.thonly.reverie_dreams.fabric.compat.ysm.network;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record YsmScreenPacket(int entityId) implements CustomPacketPayload{
    public static final Identifier payload = ReverieDreams.id("ysm_model_screen");
    public static final Type<YsmScreenPacket> PACKET_ID = new Type<>(payload);
    public static final StreamCodec<RegistryFriendlyByteBuf, YsmScreenPacket> CODEC = StreamCodec.ofMember(YsmScreenPacket::write, YsmScreenPacket::read);

    public static YsmScreenPacket read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        return new YsmScreenPacket(entityId);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
