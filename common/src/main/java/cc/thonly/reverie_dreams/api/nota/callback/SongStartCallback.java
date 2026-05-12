package cc.thonly.reverie_dreams.api.nota.callback;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

public interface SongStartCallback {

	/**
	 * Called on start of a song.
	 *
	 */
	Event<SongStartCallback> EVENT = EventFactory.createArrayBacked(SongStartCallback.class, (callbacks) -> (songPlayer) -> {
		for (SongStartCallback callback : callbacks) {
			callback.onSongStart(songPlayer);
		}
	});

	void onSongStart(SongPlayer songPlayer);
}
