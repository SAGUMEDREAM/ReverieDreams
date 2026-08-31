package cc.thonly.reverie_dreams.server.nota.model.playmode;

import cc.thonly.reverie_dreams.server.nota.model.Layer;
import cc.thonly.reverie_dreams.server.nota.model.Note;
import cc.thonly.reverie_dreams.server.nota.model.Song;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Decides how is {@link Note} played to {@link Player}
 */
public abstract class ChannelMode {

	public abstract void play(Player player, BlockPos pos, Song song, Layer layer, Note note, float volume, boolean doTranspose);
}
