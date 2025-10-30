package nota.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import nota.player.SongPlayer;

public interface SongEndEvent {

	/**
	 * Called on end of a song.
	 *
	 */
	Event<SongEndEvent> EVENT = EventFactory.createArrayBacked(SongEndEvent.class, (callbacks) -> (songPlayer) -> {
		for (SongEndEvent callback : callbacks) {
			callback.onSongEnd(songPlayer);
		}
	});

	void onSongEnd(SongPlayer songPlayer);
}
