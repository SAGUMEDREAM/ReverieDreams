package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import cc.thonly.reverie_dreams.util.nbs.NotaUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MusicBlockEntity extends BlockEntity {
    private String select = null;
    public boolean isFirst = true;

    public MusicBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.MUSIC_BLOCK.value(), pos, state);
    }

    @Nullable
    public SongPlayer getSelfPlayer() {
        Map<Long, SongPlayer> blockPos2SongPlayer = NotaUtils.blockMusicPlayCache.get(this.level);
        if (blockPos2SongPlayer == null) return null;
        return blockPos2SongPlayer.get(this.worldPosition.asLong());
    }

    public static synchronized void tick(Level world, BlockPos pos, BlockState state, MusicBlockEntity blockEntity) {
        if (world.isClientSide()) return;

        boolean hasRedstone = world.hasNeighborSignal(pos);

        Map<Long, SongPlayer> blockPos2SongPlayer = NotaUtils.blockMusicPlayCache.computeIfAbsent(world, w -> new HashMap<>());
        SongPlayer songPlayer = blockPos2SongPlayer.get(pos.asLong());

        if (hasRedstone && blockEntity.isFirst && blockEntity.select != null) {
            blockEntity.isFirst = false;
            NotaUtils.playAt(world, pos, blockEntity.getSelect());
            return;
        }

        if (hasRedstone && songPlayer == null) {
            NotaUtils.playAt(world, pos, blockEntity.getSelect());
            return;
        }

        if (!hasRedstone && songPlayer != null && songPlayer.isPlaying()) {
            songPlayer.setPlaying(false);
            blockPos2SongPlayer.remove(pos.asLong());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putString("Select", this.select == null ? "" : this.select);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.select = view.getStringOr("Select","");
    }

    public int play() {
        List<String> filenames = this.getFilenames();
        if (filenames.isEmpty() || select == null || !filenames.contains(select)) {
            return -1;
        }

        if (this.level != null && !this.level.isClientSide()) {
            NotaUtils.playAt(level, worldPosition, select);
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
        return NotaUtils.getFileNames();
    }
}
