package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record PlayerComponentUpdatePacket(UUID playerUUID,
                                          CompoundTag data
) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("player_component_update");
    public static final Type<PlayerComponentUpdatePacket> PACKET_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerComponentUpdatePacket> CODEC = StreamCodec.ofMember(PlayerComponentUpdatePacket::write, PlayerComponentUpdatePacket::read);

    private static PlayerComponentUpdatePacket read(RegistryFriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        CompoundTag compoundTag = buf.readNbt();
        return new PlayerComponentUpdatePacket(uuid, compoundTag);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.playerUUID);
        buf.writeNbt(this.data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
