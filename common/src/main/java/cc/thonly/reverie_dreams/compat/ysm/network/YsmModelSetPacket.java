package cc.thonly.reverie_dreams.compat.ysm.network;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.networking.payload.PlayerMidiNotePacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;

public record YsmModelSetPacket(int entityId, int modelId, int modelNameSize, String modelName, long modelSize) implements CustomPacketPayload{
    public static final Identifier payload = ReverieDreams.id("ysm_model_set");
    public static final CustomPacketPayload.Type<YsmModelSetPacket> PACKET_ID = new CustomPacketPayload.Type<>(payload);
    public static final StreamCodec<RegistryFriendlyByteBuf, YsmModelSetPacket> CODEC = StreamCodec.ofMember(YsmModelSetPacket::write, YsmModelSetPacket::read);

    public YsmModelSetPacket(int entityId, int modelId, String modelName, long modelSize) {
        this(entityId, modelId, modelName.length(), modelName, modelSize);
    }

    public static YsmModelSetPacket read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        int modelId = buf.readInt();
        int modelNameSize = buf.readInt();
        String modelName = buf.readString(modelNameSize, StandardCharsets.UTF_8);
        long modelSize = buf.readLong();
        return new YsmModelSetPacket(entityId, modelId, modelNameSize, modelName, modelSize);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.modelId);
        buf.writeInt(this.modelNameSize);
        buf.writeCharSequence(this.modelName, StandardCharsets.UTF_8);
        buf.writeLong(this.modelSize);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
