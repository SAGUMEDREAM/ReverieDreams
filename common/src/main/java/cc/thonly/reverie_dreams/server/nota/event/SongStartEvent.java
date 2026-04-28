package cc.thonly.reverie_dreams.server.nota.event;

import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;

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
