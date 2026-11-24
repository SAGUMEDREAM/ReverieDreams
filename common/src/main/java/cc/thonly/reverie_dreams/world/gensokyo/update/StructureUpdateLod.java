package cc.thonly.reverie_dreams.world.gensokyo.update;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Optional LOD adapter; success means submitted to the LOD queue, not synchronously persisted.
 */
public final class StructureUpdateLod {
    @FunctionalInterface
    public interface Refresher {
        String submit(ServerLevel level, LevelChunk chunk);
    }

    private static Refresher refresher = (level, chunk) -> "NOT_PRESENT";

    private StructureUpdateLod() {
    }

    public static void register(Refresher adapter) {
        refresher = adapter;
    }

    public static String submit(ServerLevel level, LevelChunk chunk) {
        try {
            return refresher.submit(level, chunk);
        } catch (Throwable error) {
            return "FAILED: " + error;
        }
    }
}
