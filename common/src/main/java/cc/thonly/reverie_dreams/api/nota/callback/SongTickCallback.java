package cc.thonly.reverie_dreams.api.nota.callback;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface SongTickCallback {

	/**
	 * Called at the start of the song tick.
	 */
	Event<SongTickCallback> EVENT = EventFactory.of((callbacks) -> (songPlayer) -> {
		for (SongTickCallback callback : callbacks) {
			callback.onSongTick(songPlayer);
		}
	});

	void onSongTick(SongPlayer songPlayer);
}
