package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncEntityPacket(int entityId, CompoundTag tag) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("sync_entity");
    public static final CustomPacketPayload.Type<SyncEntityPacket> PACKET_ID = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityPacket> CODEC = StreamCodec.ofMember(SyncEntityPacket::write, SyncEntityPacket::read);

    public static SyncEntityPacket read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        CompoundTag tag = buf.readNbt();
        return new SyncEntityPacket(entityId, tag);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.tag);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}