package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectoryCast;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

/**
 * STG 弹幕总调度器：统一驱动所有运行中的 {@link DanmakuPatternRuntime}。
 *
 * <p>由服务器 tick 事件调用（见 {@code ReverieDreams#registerServerEvents}）。
 * 同帧齐射按各自时间线执行，其他施法不会占用其发射机会。
 */
public final class DanmakuDirector {
    private static final List<DanmakuPatternRuntime> ACTIVE_PATTERNS = new ArrayList<>();

    private DanmakuDirector() {
    }

    public static void add(DanmakuPatternRuntime runtime) {
        ACTIVE_PATTERNS.add(runtime);
    }

    public static void tick(MinecraftServer server) {
        PlayerTrajectoryCast.tick(server);
        if (ACTIVE_PATTERNS.isEmpty()) {
            return;
        }
        // A familiar can append a child runtime while its parent is ticking.
        int scheduled = ACTIVE_PATTERNS.size();
        for (int i = 0; i < scheduled; i++) {
            ACTIVE_PATTERNS.get(i).tick();
        }
        ACTIVE_PATTERNS.removeIf(DanmakuPatternRuntime::isDone);
    }

    public static void cancelAll() {
        PlayerTrajectoryCast.clear();
        for (DanmakuPatternRuntime runtime : ACTIVE_PATTERNS) {
            runtime.stopAndClear();
        }
        ACTIVE_PATTERNS.clear();
    }

    public static int activeCount() {
        return ACTIVE_PATTERNS.size();
    }

    public static List<DanmakuPatternRuntime> view() {
        return List.copyOf(ACTIVE_PATTERNS);
    }
}
