package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RecipeManagerSyncPacket(Identifier typeId,
                                      CompoundTag data
) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("recipe_manager_sync");
    public static final Type<RecipeManagerSyncPacket> PACKET_ID = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeManagerSyncPacket> CODEC = StreamCodec.ofMember(RecipeManagerSyncPacket::write, RecipeManagerSyncPacket::read);

    private static RecipeManagerSyncPacket read(RegistryFriendlyByteBuf buf) {
        Identifier typeId = buf.readIdentifier();
        CompoundTag compoundTag = buf.readNbt();
        return new RecipeManagerSyncPacket(typeId, compoundTag);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(this.typeId());
        buf.writeNbt(this.data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
