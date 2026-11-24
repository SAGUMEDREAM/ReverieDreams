package cc.thonly.reverie_dreams.fabric.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Packs every visible cuboid face into its own island of the home atlas so a
 * brush edit stays confined to one face. Identity is the cuboid bounds, side
 * and material: the shared parts of the sixteen kotatsu connection variants
 * reuse one island, everything else keeps its pixels to itself. Each island
 * leaves a transparent two pixel border against its neighbours.
 */
final class JapaneseUvLayout {
    static final int SIZE = 512;
    private static final int PADDING = 2;
    private static final int PIXELS_PER_UNIT = SIZE / 16;
    private static final String MATERIAL = "x-rd-material";

    private record Key(double x1, double y1, double z1, double x2, double y2, double z2,
                       String side, String material) {}
    private record Rect(int x, int y, int width, int height) {}

    private final Map<Key, Rect> islands = new HashMap<>();
    private int cursorX = PADDING;
    private int cursorY = PADDING;
    private int shelfHeight;
    private int usedHeight;

    void assign(List<JsonObject> elements) {
        for (JsonObject element : elements) {
            double[] from = JapaneseModelKit.values(element.getAsJsonArray("from"));
            double[] to = JapaneseModelKit.values(element.getAsJsonArray("to"));
            JsonObject faces = element.getAsJsonObject("faces");
            for (String side : JapaneseModelKit.SIDES) {
                JsonObject face = faces.getAsJsonObject(side);
                if (face == null) {
                    continue;
                }
                JsonElement marker = face.remove(MATERIAL);
                if (marker == null) {
                    throw new IllegalStateException("Layout face " + side + " has no material marker");
                }
                double width = to[0] - from[0];
                double height = to[1] - from[1];
                if (side.equals("east") || side.equals("west")) {
                    width = to[2] - from[2];
                } else if (side.equals("up") || side.equals("down")) {
                    height = to[2] - from[2];
                }
                Key key = new Key(from[0], from[1], from[2], to[0], to[1], to[2], side, marker.getAsString());
                Rect rect = islands.get(key);
                if (rect == null) {
                    rect = allocate((int) Math.max(1, Math.round(width)), (int) Math.max(1, Math.round(height)));
                    islands.put(key, rect);
                }
                face.add("uv", JapaneseModelKit.array(
                        rect.x() / (double) PIXELS_PER_UNIT,
                        rect.y() / (double) PIXELS_PER_UNIT,
                        (rect.x() + rect.width()) / (double) PIXELS_PER_UNIT,
                        (rect.y() + rect.height()) / (double) PIXELS_PER_UNIT));
            }
        }
    }

    private Rect allocate(int width, int height) {
        if (cursorX + width + PADDING > SIZE) {
            cursorX = PADDING;
            cursorY += shelfHeight + PADDING;
            shelfHeight = 0;
        }
        if (cursorY + height + PADDING > SIZE) {
            throw new IllegalStateException("japanese_home islands exceed the " + SIZE + "px atlas");
        }
        Rect rect = new Rect(cursorX, cursorY, width, height);
        cursorX += width + PADDING;
        shelfHeight = Math.max(shelfHeight, height);
        usedHeight = Math.max(usedHeight, cursorY + height);
        return rect;
    }

    String summary() {
        return islands.size() + " islands, " + SIZE + "x" + SIZE + " atlas, used height " + usedHeight + "px";
    }
}
