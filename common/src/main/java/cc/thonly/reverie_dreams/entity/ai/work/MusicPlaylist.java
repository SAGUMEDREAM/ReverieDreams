package cc.thonly.reverie_dreams.entity.ai.work;

import java.util.List;

public final class MusicPlaylist {
    private MusicPlaylist() {
    }

    public static String select(List<String> configured, List<String> available, String selected, int index, boolean loop) {
        List<String> tracks = tracks(configured, available, selected);
        if (tracks.isEmpty() || !loop && index >= tracks.size())
            return null;
        return tracks.get(Math.floorMod(index, tracks.size()));
    }

    public static List<String> tracks(List<String> configured, List<String> available, String selected) {
        return configured.isEmpty()
                ? selected != null && available.contains(selected) ? List.of(selected) : available
                : configured.stream().filter(available::contains).distinct().toList();
    }

    public static String next(List<String> available, String current) {
        return available.isEmpty() ? null : available.get(Math.floorMod(available.indexOf(current) + 1, available.size()));
    }
}
