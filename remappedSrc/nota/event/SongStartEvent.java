package nota.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import nota.player.SongPlayer;

public interface SongStartEvent {

	/**
	 * Called on start of a song.
	 *
	 */
	Event<SongStartEvent> EVENT = EventFactory.createArrayBacked(SongStartEvent.class, (callbacks) -> (songPlayer) -> {
		for (SongStartEvent callback : callbacks) {
			callback.onSongStart(songPlayer);
		}
	});

	void onSongStart(SongPlayer songPlayer);
}
