package cc.thonly.reverie_dreams.server.nota.model.playmode;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.server.nota.model.Layer;
import cc.thonly.reverie_dreams.server.nota.model.Note;
import cc.thonly.reverie_dreams.server.nota.model.Song;
import cc.thonly.reverie_dreams.server.nota.utils.InstrumentUtils;
import cc.thonly.reverie_dreams.server.nota.utils.NoteUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * {@link Note} is played inside of {@link Player}'s head.
 */
public class MonoMode extends ChannelMode {

	@Override
	public synchronized void play(Player player, BlockPos pos, Song song, Layer layer, Note note, float volume, boolean doTranspose) {
		float pitch;
		if(doTranspose) {
			pitch = NoteUtils.getPitchTransposed(note);
		}
		else {
			pitch = NoteUtils.getPitchInOctave(note);
		}
		if (ReverieDreams.getServer() == null) {
			return;
		}
		ReverieDreams.getServer().executeIfPossible(()-> {
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(),InstrumentUtils.getInstrument(note.getInstrument()), SoundSource.RECORDS, volume, pitch);
		});
	}
}
