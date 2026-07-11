package cc.thonly.reverie_dreams.api.nota.callback;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface SongEndCallback {

	/**
	 * Called on end of a song.
	 *
	 */
	Event<SongEndCallback> EVENT = EventFactory.of((callbacks) -> (songPlayer) -> {
		for (SongEndCallback callback : callbacks) {
			callback.onSongEnd(songPlayer);
		}
	});

	void onSongEnd(SongPlayer songPlayer);
}
