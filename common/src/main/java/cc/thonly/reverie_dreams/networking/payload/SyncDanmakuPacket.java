package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncDanmakuPacket(int entityId, CompoundTag tag) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("sync_danmaku_entity");
    public static final Type<SyncDanmakuPacket> PACKET_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDanmakuPacket> CODEC = StreamCodec.ofMember(SyncDanmakuPacket::write, SyncDanmakuPacket::read);

    public static SyncDanmakuPacket read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        CompoundTag tag = buf.readNbt();
        return new SyncDanmakuPacket(entityId, tag);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.tag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}