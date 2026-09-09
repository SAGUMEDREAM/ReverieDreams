package cc.thonly.reverie_dreams.fabric.compat.ysm.network;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record C2SYsmScreenPacket(int entityId) implements CustomPacketPayload{
    public static final Identifier payload = ReverieDreams.id("ysm_model_screen");
    public static final Type<C2SYsmScreenPacket> PACKET_ID = new Type<>(payload);
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SYsmScreenPacket> CODEC = StreamCodec.ofMember(C2SYsmScreenPacket::write, C2SYsmScreenPacket::read);

    public static C2SYsmScreenPacket read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        return new C2SYsmScreenPacket(entityId);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
