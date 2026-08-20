package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CustomRegistrySyncPacket(Identifier registryKey,
                                       CompoundTag data,
                                       CompoundTag tags
) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("custom_registry_sync");
    public static final Type<CustomRegistrySyncPacket> PACKET_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, CustomRegistrySyncPacket> CODEC = StreamCodec.ofMember(CustomRegistrySyncPacket::write, CustomRegistrySyncPacket::read);

    public static CustomRegistrySyncPacket read(RegistryFriendlyByteBuf buf) {
        Identifier typeId = buf.readIdentifier();
        CompoundTag data = buf.readNbt();
        CompoundTag tags = buf.readNbt();
        return new CustomRegistrySyncPacket(typeId, data, tags);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(this.registryKey());
        buf.writeNbt(this.data);
        buf.writeNbt(this.tags);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
