package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * STG 弹幕总调度器：统一驱动所有运行中的 {@link DanmakuPatternRuntime}。
 *
 * <p>由服务器 tick 事件调用（见 {@code ReverieDreams#registerServerEvents}）。
 */
public final class DanmakuDirector {
    private static final List<DanmakuPatternRuntime> ACTIVE_PATTERNS = new ArrayList<>();

    private DanmakuDirector() {
    }

    public static void add(DanmakuPatternRuntime runtime) {
        ACTIVE_PATTERNS.add(runtime);
    }

    public static void tick(MinecraftServer server) {
        if (ACTIVE_PATTERNS.isEmpty()) {
            return;
        }
        Iterator<DanmakuPatternRuntime> iterator = ACTIVE_PATTERNS.iterator();
        while (iterator.hasNext()) {
            DanmakuPatternRuntime runtime = iterator.next();
            runtime.tick();
            if (runtime.isDone()) {
                iterator.remove();
            }
        }
    }

    public static void cancelAll() {
        for (DanmakuPatternRuntime runtime : ACTIVE_PATTERNS) {
            runtime.cancel();
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
