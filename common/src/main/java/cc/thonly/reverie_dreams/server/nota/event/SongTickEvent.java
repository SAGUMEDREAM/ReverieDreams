package cc.thonly.reverie_dreams.server.nota.event;

import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;

public interface SongTickEvent {

	/**
	 * Called at the start of the song tick.
	 */
	Event<SongTickEvent> EVENT = EventFactory.createArrayBacked(SongTickEvent.class, (callbacks) -> (songPlayer) -> {
		for (SongTickEvent callback : callbacks) {
			callback.onSongTick(songPlayer);
		}
	});

	void onSongTick(SongPlayer songPlayer);
}
