package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.util.TouhouNotaUtils;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        super(ModBlockEntities.MUSIC_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public SongPlayer getSelfPlayer() {
        Map<Long, SongPlayer> blockPos2SongPlayer = TouhouNotaUtils.blockMusicPlayCache.get(this.world);
        if (blockPos2SongPlayer == null) return null;
        return blockPos2SongPlayer.get(this.pos.asLong());
    }

    public static synchronized void tick(World world, BlockPos pos, BlockState state, MusicBlockEntity blockEntity) {
        if (world.isClient()) return;

        boolean hasRedstone = world.isReceivingRedstonePower(pos);

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
            blockPos2SongPlayer.remove(pos);
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putString("Select", this.select == null ? "" : this.select);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.select = view.getString("Select","");
    }

    public int play() {
        List<String> filenames = this.getFilenames();
        if (filenames.isEmpty() || select == null || !filenames.contains(select)) {
            return -1;
        }

        if (this.world != null && !this.world.isClient) {
            TouhouNotaUtils.playAt(world, pos, select);
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
        this.markDirty();
        return index;
    }

    public int next() {
        List<String> filenames = this.getFilenames();
        if (filenames.isEmpty()) return -1;

        int index = select == null ? -1 : filenames.indexOf(select);
        index = (index + 1) % filenames.size();
        this.select = filenames.get(index);
        this.markDirty();
        return index;
    }


    public List<String> getFilenames() {
        return TouhouNotaUtils.getFileNames();
    }
}
