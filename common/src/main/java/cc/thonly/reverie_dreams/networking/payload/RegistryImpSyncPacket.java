package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RegistryImpSyncPacket(Identifier registryKey,
                                    CompoundTag data
) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("registry_impl_sync");
    public static final Type<RegistryImpSyncPacket> PACKET_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RegistryImpSyncPacket> CODEC = StreamCodec.ofMember(RegistryImpSyncPacket::write, RegistryImpSyncPacket::read);

    public static RegistryImpSyncPacket read(RegistryFriendlyByteBuf buf) {
        Identifier typeId = buf.readIdentifier();
        CompoundTag compoundTag = buf.readNbt();
        return new RegistryImpSyncPacket(typeId, compoundTag);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(this.registryKey());
        buf.writeNbt(this.data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
