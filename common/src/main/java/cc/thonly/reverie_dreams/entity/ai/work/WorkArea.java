package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * One bounded work site shared by navigation, production, diagnostics and storage.
 */
public record WorkArea(BlockPos center, int radius, int height) {
    public WorkArea {
        center = center.immutable();
        radius = Math.clamp(radius, 1, 16);
        height = Math.clamp(height, 1, 8);
    }

    public boolean contains(BlockPos pos) {
        return Math.abs((long) pos.getX() - center.getX()) <= radius
                && Math.abs((long) pos.getZ() - center.getZ()) <= radius
                && Math.abs((long) pos.getY() - center.getY()) <= height;
    }

    public AABB box() {
        return new AABB(center).inflate(radius, height, radius);
    }

    public Iterable<BlockPos> positions() {
        return BlockPos.betweenClosed(center.offset(-radius, -height, -radius), center.offset(radius, height, radius));
    }

    public int size() {
        return (radius * 2 + 1) * (radius * 2 + 1) * (height * 2 + 1);
    }

    public BlockPos position(int index) {
        int width = radius * 2 + 1;
        int layer = index / (width * width);
        int y = (layer + 1) / 2 * (layer % 2 == 0 ? -1 : 1);
        int horizontal = index % (width * width);
        if (horizontal == 0)
            return center.offset(0, y, 0);
        int ring = (int) Math.ceil((Math.sqrt(horizontal + 1.0) - 1) / 2);
        int side = ring * 2;
        int offset = horizontal - (side - 1) * (side - 1);
        int step = offset % side;
        return switch (offset / side) {
            case 0 -> center.offset(ring, y, -ring + step);
            case 1 -> center.offset(ring - step, y, ring);
            case 2 -> center.offset(-ring, y, ring - step);
            default -> center.offset(-ring + step, y, -ring);
        };
    }
}
