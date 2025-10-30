package nota.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import nota.Nota;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Playlist;
import nota.model.Song;

/**
 * SongPlayer playing to everyone added to it no matter where they are
 */
public class RadioSongPlayer extends SongPlayer {

	public RadioSongPlayer(Song song) {
		super(song);
	}

	public RadioSongPlayer(Playlist playlist) {
		super(playlist);
	}

	@Override
	public void playTick(Player player, int tick) {
		byte playerVolume = Nota.getPlayerVolume(player);

		for(Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if(note == null) {
				continue;
			}

			float volume = (layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F;
			var eyePos = player.getEyePosition();
			channelMode.play(player, new BlockPos((int) eyePos.x(), (int) eyePos.y(), (int) eyePos.z()), song, layer, note, volume, !enable10Octave);
		}
	}
}
