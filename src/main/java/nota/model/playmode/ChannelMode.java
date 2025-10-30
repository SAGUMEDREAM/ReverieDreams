package nota.model.playmode;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Song;

/**
 * Decides how is {@link Note} played to {@link Player}
 */
public abstract class ChannelMode {

	public abstract void play(Player player, BlockPos pos, Song song, Layer layer, Note note, float volume, boolean doTranspose);
}
