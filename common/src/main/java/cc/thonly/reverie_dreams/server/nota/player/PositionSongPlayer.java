package cc.thonly.reverie_dreams.server.nota.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import cc.thonly.reverie_dreams.server.nota.Nota;
import cc.thonly.reverie_dreams.server.nota.model.Layer;
import cc.thonly.reverie_dreams.server.nota.model.Note;
import cc.thonly.reverie_dreams.server.nota.model.Playlist;
import cc.thonly.reverie_dreams.server.nota.model.Song;

/**
 * SongPlayer created at a specified BlockPos
 */
public class PositionSongPlayer extends RangeSongPlayer {
	private BlockPos pos;
	Level world;

	public PositionSongPlayer(Song song, Level world) {
		super(song);
		this.world = world;
	}

	public PositionSongPlayer(Playlist playlist, Level world) {
		super(playlist);
		this.world = world;
	}

	/**
	 * Gets location on which is the PositionSongPlayer playing
	 *
	 * @return {@link BlockPos}
	 */
	public BlockPos getBlockPos() {
		return this.pos;
	}

	/**
	 * Sets location on which is the PositionSongPlayer playing
	 */
	public void setBlockPos(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void playTick(Player player, int tick) {
		if(!player.level().dimension().equals(world.dimension())) {
			return; // not in same world
		}

		byte playerVolume = Nota.getPlayerVolume(player);

		for(Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if(note == null) continue;

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F)
					* ((1F / 16F) * getDistance());

			if(isInRange(player)) {
				this.playerList.put(player.getUUID(), true);
				this.channelMode.play(player, pos, song, layer, note, volume, !enable10Octave);
			}
			else {
				this.playerList.put(player.getUUID(), false);
			}
		}
	}

	/**
	 * Returns true if the Player is able to hear the current PositionSongPlayer
	 *
	 * @param player in range
	 * @return ability to hear the current PositionSongPlayer
	 */
	@Override
	public boolean isInRange(Player player) {
		return player.blockPosition().closerThan(pos, getDistance());
	}
}
