package cc.thonly.reverie_dreams.api.nota.callback;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

public interface SongEndCallback {

	/**
	 * Called on end of a song.
	 *
	 */
	Event<SongEndCallback> EVENT = EventFactory.createArrayBacked(SongEndCallback.class, (callbacks) -> (songPlayer) -> {
		for (SongEndCallback callback : callbacks) {
			callback.onSongEnd(songPlayer);
		}
	});

	void onSongEnd(SongPlayer songPlayer);
}
