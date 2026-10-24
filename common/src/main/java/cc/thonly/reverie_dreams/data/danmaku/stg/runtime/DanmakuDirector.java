package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * STG 弹幕总调度器：统一驱动所有运行中的 {@link DanmakuPatternRuntime}。
 *
 * <p>由服务器 tick 事件调用（见 {@code ReverieDreams#registerServerEvents}）。
 * 同时提供全局每 tick 生成预算，防止多个图案叠加时瞬间生成过多子弹实体。
 */
public final class DanmakuDirector {
    /**
     * 每 tick 允许通过新引擎生成的子弹上限（所有图案共享）。
     * 旧版 SpellcardRenderer 有自己的 64/tick 上限；新引擎没有帧级上限，所以在这里兜底。
     */
    public static final int MAX_SPAWNS_PER_TICK = 128;

    private static final List<DanmakuPatternRuntime> ACTIVE_PATTERNS = new ArrayList<>();
    private static int spawnedThisTick = 0;

    private DanmakuDirector() {
    }

    public static void add(DanmakuPatternRuntime runtime) {
        ACTIVE_PATTERNS.add(runtime);
    }

    public static void tick(MinecraftServer server) {
        spawnedThisTick = 0;
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

    /**
     * 为一次子弹生成申请预算；返回 false 时调用方应放弃本次生成。
     */
    public static boolean consumeSpawnBudget() {
        if (spawnedThisTick >= MAX_SPAWNS_PER_TICK) {
            return false;
        }
        spawnedThisTick++;
        return true;
    }

    public static int spawnedThisTick() {
        return spawnedThisTick;
    }

    public static void cancelAll() {
        for (DanmakuPatternRuntime runtime : ACTIVE_PATTERNS) {
            runtime.cancel();
        }
        ACTIVE_PATTERNS.clear();
        spawnedThisTick = 0;
    }

    public static int activeCount() {
        return ACTIVE_PATTERNS.size();
    }

    public static List<DanmakuPatternRuntime> view() {
        return List.copyOf(ACTIVE_PATTERNS);
    }
}
