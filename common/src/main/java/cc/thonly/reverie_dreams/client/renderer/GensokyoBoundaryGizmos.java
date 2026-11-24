package cc.thonly.reverie_dreams.client.renderer;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.world.phys.AABB;

/**
 * Client-side boundary warning for the fixed Gensokyo boundary.
 *
 * <p>This deliberately copies the barrier branch of vanilla's
 * {@code BlockEntityWithBoundingBoxRenderer}: one red 0.45..0.55 wireframe cube
 * per boundary cell. It is collected into the same per-frame {@link Gizmos}
 * collector, so the result is visually identical to a structure block showing
 * its invisible barrier blocks - but it is not tied to a block entity and is
 * available to all players near the edge, including creative and spectator.</p>
 */
public final class GensokyoBoundaryGizmos {
    private static final int WARNING_RANGE = 32;
    private static final int SEGMENT_RADIUS = 32;
    private static final int VERTICAL_BELOW = 8;
    private static final int VERTICAL_ABOVE = 8;
    private static final int BARRIER_COLOR = 0xFFFF0000;

    private GensokyoBoundaryGizmos() {
    }

    /**
     * Called from LevelRenderer.collectPerFrameGizmos() while its Gizmos collector is active.
     */
    public static void collect() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return;
        }
        if (!level.dimension().equals(RDBuiltinLevels.GENSOKYO)) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }

        var bounds = GensokyoBoundary.playerBounds(level);
        int MIN_X = bounds.x(), MAX_X = bounds.x() + bounds.width() - 1;
        int MIN_Z = bounds.z(), MAX_Z = bounds.z() + bounds.depth() - 1;
        int px = player.getBlockX();
        int pz = player.getBlockZ();
        int py = player.getBlockY();

        boolean west = px <= MIN_X + WARNING_RANGE;
        boolean east = px >= MAX_X - WARNING_RANGE;
        boolean north = pz <= MIN_Z + WARNING_RANGE;
        boolean south = pz >= MAX_Z - WARNING_RANGE;
        if (!west && !east && !north && !south) {
            return;
        }

        if (west) {
            drawXEdge(level, MIN_X - 1, pz, py);
        }
        if (east) {
            drawXEdge(level, MAX_X + 1, pz, py);
        }
        if (north) {
            drawZEdge(level, MIN_Z - 1, px, py);
        }
        if (south) {
            drawZEdge(level, MAX_Z + 1, px, py);
        }
    }

    private static void drawXEdge(ClientLevel level, int edgeX, int playerZ, int playerY) {
        var bounds = GensokyoBoundary.playerBounds(level);
        int fromZ = Math.max(bounds.z(), playerZ - SEGMENT_RADIUS);
        int toZ = Math.min(bounds.z() + bounds.depth() - 1, playerZ + SEGMENT_RADIUS);
        for (int z = fromZ; z <= toZ; z++) {
            for (int y = playerY - VERTICAL_BELOW; y <= playerY + VERTICAL_ABOVE; y++) {
                drawBarrierIfPresent(level, new BlockPos(edgeX, y, z));
            }
        }
    }

    private static void drawZEdge(ClientLevel level, int edgeZ, int playerX, int playerY) {
        var bounds = GensokyoBoundary.playerBounds(level);
        int fromX = Math.max(bounds.x(), playerX - SEGMENT_RADIUS);
        int toX = Math.min(bounds.x() + bounds.width() - 1, playerX + SEGMENT_RADIUS);
        for (int x = fromX; x <= toX; x++) {
            for (int y = playerY - VERTICAL_BELOW; y <= playerY + VERTICAL_ABOVE; y++) {
                drawBarrierIfPresent(level, new BlockPos(x, y, edgeZ));
            }
        }
    }

    private static void drawBarrierIfPresent(ClientLevel level, BlockPos pos) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        AABB box = new AABB(
                x + 0.45D, y + 0.45D, z + 0.45D,
                x + 0.55D, y + 0.55D, z + 0.55D
        );
        Gizmos.cuboid(box, GizmoStyle.stroke(BARRIER_COLOR));
    }
}
