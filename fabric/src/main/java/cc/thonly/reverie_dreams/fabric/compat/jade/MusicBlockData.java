package cc.thonly.reverie_dreams.fabric.compat.jade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MusicBlockData(String midiName) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MusicBlockData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MusicBlockData::midiName,
            MusicBlockData::new
    );
}
