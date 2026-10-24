package cc.thonly.reverie_dreams.api.nota.callback;

import cc.thonly.reverie_dreams.server.nota.player.SongPlayer;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface SongEvents {

    /**
     * Called on start of a song.
     *
     */
    Event<SongStartEvent> START = EventFactory.of((callbacks) -> (songPlayer) -> {
        for (SongStartEvent callback : callbacks) {
            callback.onSongStart(songPlayer);
        }
    });
    /**
     * Called at the start of the song tick.
     */
    Event<SongTickEvent> TICK = EventFactory.of((callbacks) -> (songPlayer) -> {
        for (SongTickEvent callback : callbacks) {
            callback.onSongTick(songPlayer);
        }
    });
    /**
     * Called on end of a song.
     *
     */
    Event<SongEndEvent> END = EventFactory.of((callbacks) -> (songPlayer) -> {
        for (SongEndEvent callback : callbacks) {
            callback.onSongEnd(songPlayer);
        }
    });

    interface SongStartEvent {

        void onSongStart(SongPlayer songPlayer);
    }

    interface SongTickEvent {

        void onSongTick(SongPlayer songPlayer);
    }

    interface SongEndEvent {

        void onSongEnd(SongPlayer songPlayer);
    }
}
