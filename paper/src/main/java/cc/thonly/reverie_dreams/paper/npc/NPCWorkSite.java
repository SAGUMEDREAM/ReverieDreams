package cc.thonly.reverie_dreams.paper.npc;

import java.util.UUID;
import org.bukkit.Location;

/** Current-source WorkArea ordering and bounded radius/vertical range. No chunk access occurs here. */
public record NPCWorkSite(UUID worldId, int x, int y, int z, int radius, int height) {
    public record Pos(int x, int y, int z) { }
    public NPCWorkSite {
        if (worldId == null) throw new IllegalArgumentException("Missing work world");
        radius = Math.clamp(radius, 1, 16); height = Math.clamp(height, 1, 8);
    }
    public boolean contains(Location point) {
        return point.getWorld().getUID().equals(worldId) && contains(point.getBlockX(), point.getBlockY(), point.getBlockZ());
    }
    public boolean contains(int bx, int by, int bz) {
        return Math.abs((long) bx - x) <= radius && Math.abs((long) bz - z) <= radius && Math.abs((long) by - y) <= height;
    }
    public int size() { return (radius * 2 + 1) * (radius * 2 + 1) * (height * 2 + 1); }
    public Pos position(int index) {
        if (index < 0 || index >= size()) throw new IllegalArgumentException("Work scan index outside site");
        int width = radius * 2 + 1, layer = index / (width * width);
        int dy = (layer + 1) / 2 * (layer % 2 == 0 ? -1 : 1), horizontal = index % (width * width);
        if (horizontal == 0) return new Pos(x, y + dy, z);
        int ring = (int) Math.ceil((Math.sqrt(horizontal + 1.0) - 1) / 2), side = ring * 2;
        int offset = horizontal - (side - 1) * (side - 1), step = offset % side;
        return switch (offset / side) {
            case 0 -> new Pos(x + ring, y + dy, z - ring + step);
            case 1 -> new Pos(x + ring - step, y + dy, z + ring);
            case 2 -> new Pos(x - ring, y + dy, z + ring - step);
            default -> new Pos(x - ring + step, y + dy, z - ring);
        };
    }
}
