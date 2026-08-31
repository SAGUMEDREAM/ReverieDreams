package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

public record PlayerMidiNotePacket(EquipmentSlot slot, int key,
                                   int note, float volume,
                                   boolean press) implements CustomPacketPayload {
    public static final Identifier payload = ReverieDreams.id("player_midi-event");
    public static final Type<PlayerMidiNotePacket> PACKET_ID = new Type<>(payload);
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerMidiNotePacket> CODEC = StreamCodec.ofMember(PlayerMidiNotePacket::write, PlayerMidiNotePacket::read);

    public static PlayerMidiNotePacket read(RegistryFriendlyByteBuf buf) {
        EquipmentSlot slot = buf.readEnum(EquipmentSlot.class);
        int key = buf.readInt();
        int note = buf.readInt();
        float volume = buf.readFloat();
        boolean press = buf.readBoolean();
        return new PlayerMidiNotePacket(slot, key, note, volume, press);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(this.slot);
        buf.writeInt(this.key);
        buf.writeInt(this.note);
        buf.writeFloat(this.volume);
        buf.writeBoolean(this.press);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
