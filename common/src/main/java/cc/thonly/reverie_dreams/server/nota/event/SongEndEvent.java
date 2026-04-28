package cc.thonly.reverie_dreams.server.nota.event;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

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
