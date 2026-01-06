package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.minecraft.util.tvio.TagValueFunction;
import cc.thonly.reverie_dreams.util.TouhouNotaUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import nota.player.SongPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MusicBlockEntity extends BlockEntity {
    private String select = null;
    public boolean isFirst = true;

    public MusicBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.MUSIC_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public SongPlayer getSelfPlayer() {
        Map<Long, SongPlayer> blockPos2SongPlayer = TouhouNotaUtils.blockMusicPlayCache.get(this.level);
        if (blockPos2SongPlayer == null) return null;
        return blockPos2SongPlayer.get(this.worldPosition.asLong());
    }

    public static synchronized void tick(Level world, BlockPos pos, BlockState state, MusicBlockEntity blockEntity) {
        if (world.isClientSide()) return;

        boolean hasRedstone = world.hasNeighborSignal(pos);

        Map<Long, SongPlayer> blockPos2SongPlayer = TouhouNotaUtils.blockMusicPlayCache.computeIfAbsent(world, w -> new HashMap<>());
        SongPlayer songPlayer = blockPos2SongPlayer.get(pos.asLong());

        if (hasRedstone && blockEntity.isFirst && blockEntity.select != null) {
            blockEntity.isFirst = false;
            TouhouNotaUtils.playAt(world, pos, blockEntity.getSelect());
            return;
        }

        if (hasRedstone && songPlayer == null) {
            TouhouNotaUtils.playAt(world, pos, blockEntity.getSelect());
            return;
        }

        if (!hasRedstone && songPlayer != null && songPlayer.isPlaying()) {
            songPlayer.setPlaying(false);
            blockPos2SongPlayer.remove(pos.asLong());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (this.level != null) {
            TagValueFunction.write(compoundTag, this.level.registryAccess(), view-> {
                view.putString("Select", this.select == null ? "" : this.select);
            });
        }

    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        if (this.level != null) {
            TagValueFunction.read(compoundTag, this.level.registryAccess(), view-> {
                this.select = view.getStringOr("Select", "");
            });
        }
    }

    public int play() {
        List<String> filenames = this.getFilenames();
        if (filenames.isEmpty() || select == null || !filenames.contains(select)) {
            return -1;
        }

        if (this.level != null && !this.level.isClientSide) {
            TouhouNotaUtils.playAt(level, worldPosition, select);
        }
        return filenames.indexOf(select);
    }

    public int prev() {
        List<String> filenames = this.getFilenames();
        if (filenames.isEmpty()) return -1;

        int index = select == null ? 0 : filenames.indexOf(select);
        if (index == -1) index = 0;

        index = (index - 1 + filenames.size()) % filenames.size();
        this.select = filenames.get(index);
        this.setChanged();
        return index;
    }

    public int next() {
        List<String> filenames = this.getFilenames();
        if (filenames.isEmpty()) return -1;

        int index = select == null ? -1 : filenames.indexOf(select);
        index = (index + 1) % filenames.size();
        this.select = filenames.get(index);
        this.setChanged();
        return index;
    }


    public List<String> getFilenames() {
        return TouhouNotaUtils.getFileNames();
    }
}
