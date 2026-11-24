package cc.thonly.reverie_dreams.world.gensokyo.update;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Optional claim/machine adapters may veto maintenance. Invoked on the server thread before backup.
 */
public final class StructureUpdateProtection {
    @FunctionalInterface
    public interface Guard {
        void check(ServerLevel level, String landmark, BoundingBox bounds);
    }

    private static final List<Guard> GUARDS = new CopyOnWriteArrayList<>();

    private StructureUpdateProtection() {
    }

    public static AutoCloseable register(Guard guard) {
        GUARDS.add(guard);
        return () -> GUARDS.remove(guard);
    }

    static void check(ServerLevel level, String landmark, BoundingBox bounds) {
        for (Guard guard : GUARDS)
            guard.check(level, landmark, bounds);
    }
}
