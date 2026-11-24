package cc.thonly.reverie_dreams.entity.ai.work;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MusicPlaylistTest {
    private final List<String> files = List.of("a.nbs", "b.nbs", "c.nbs");
    @Test void configuredOrderIgnoresMissingAndDuplicateFiles() {
        var configured = List.of("missing.nbs", "c.nbs", "a.nbs", "c.nbs");
        assertEquals(List.of("c.nbs", "a.nbs"), MusicPlaylist.tracks(configured, files, "b.nbs"));
        assertEquals("a.nbs", MusicPlaylist.select(configured, files, null, 1, false));
        assertNull(MusicPlaylist.select(configured, files, null, 2, false));
        assertEquals("c.nbs", MusicPlaylist.select(configured, files, null, 2, true));
    }
    @Test void explicitInstrumentSelectionWinsOverDirectoryFallback() {
        assertEquals("b.nbs", MusicPlaylist.select(List.of(), files, "b.nbs", 5, true));
        assertNull(MusicPlaylist.select(List.of(), files, "b.nbs", 1, false));
        assertEquals("a.nbs", MusicPlaylist.select(List.of(), files, "deleted.nbs", 0, false));
    }
    @Test void nextTrackWrapsAndAnEmptyPlaylistNeverStarts() {
        assertEquals("a.nbs", MusicPlaylist.next(files, "c.nbs"));
        assertNull(MusicPlaylist.next(List.of(), "c.nbs"));
        assertNull(MusicPlaylist.select(List.of("deleted.nbs"), files, null, 0, true));
    }
}
