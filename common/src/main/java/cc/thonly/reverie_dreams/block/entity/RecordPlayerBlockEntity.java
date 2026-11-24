package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.decor.RecordPlayerBlock;
import cc.thonly.reverie_dreams.block.decor.RecordPlayerMedia;
import cc.thonly.reverie_dreams.client.audio.radio.RadioAudioClient;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The server stores configuration only; audio fetching and local bookmarks stay on each client.
 */
public final class RecordPlayerBlockEntity extends BlockEntity {
    private String mediaUrl = RecordPlayerMedia.DEFAULT_URL;

    public RecordPlayerBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.RECORD_PLAYER.get(), pos, state);
    }

    public String mediaUrl() {
        return mediaUrl;
    }

    public void configure(String url) {
        String normalized = RecordPlayerMedia.normalize(url);
        if (mediaUrl.equals(normalized))
            return;
        mediaUrl = normalized;
        setChanged();
        if (level != null && !level.isClientSide())
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("MediaUrl", mediaUrl);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            mediaUrl = RecordPlayerMedia.normalize(input.getStringOr("MediaUrl", RecordPlayerMedia.DEFAULT_URL));
        } catch (IllegalArgumentException invalid) {
            mediaUrl = RecordPlayerMedia.DEFAULT_URL;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(RecordPlayerBlock.PLAYING))
            RadioAudioClient.source(level, pos);
    }
}
