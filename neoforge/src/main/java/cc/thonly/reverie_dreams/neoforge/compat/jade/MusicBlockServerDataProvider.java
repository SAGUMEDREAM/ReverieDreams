package cc.thonly.reverie_dreams.neoforge.compat.jade;

import cc.thonly.reverie_dreams.block.entity.MusicBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;

public class MusicBlockServerDataProvider implements StreamServerDataProvider<BlockAccessor, MusicBlockData> {
    public static final MusicBlockServerDataProvider INSTANCE = new MusicBlockServerDataProvider();

    @Override
    public @Nullable MusicBlockData streamData(BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof MusicBlockEntity musicBlockEntity)) {
            return null;
        }
        return new MusicBlockData(musicBlockEntity.getSelect());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MusicBlockData> streamCodec() {
        return MusicBlockData.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.MUSIC_BLOCK_DATA_PROVIDER;
    }
}
