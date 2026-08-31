package cc.thonly.reverie_dreams.api.nota.callback;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface SongStartCallback {

	/**
	 * Called on start of a song.
	 *
	 */
	Event<SongStartCallback> EVENT = EventFactory.of((callbacks) -> (songPlayer) -> {
		for (SongStartCallback callback : callbacks) {
			callback.onSongStart(songPlayer);
		}
	});

	void onSongStart(SongPlayer songPlayer);
}
