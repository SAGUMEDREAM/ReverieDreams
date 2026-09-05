package cc.thonly.reverie_dreams.api.nota.callback;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

public interface SongTickCallback {

    /**
     * Called at the start of the song tick.
     */
    Event<SongTickCallback> EVENT = EventFactory.createArrayBacked(SongTickCallback.class, (callbacks) -> (songPlayer) -> {
        for (SongTickCallback callback : callbacks) {
            callback.onSongTick(songPlayer);
        }
    });

    void onSongTick(SongPlayer songPlayer);
}
