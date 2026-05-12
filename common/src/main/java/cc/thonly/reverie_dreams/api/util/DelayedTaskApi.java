package cc.thonly.reverie_dreams.api.util;

import cc.thonly.reverie_dreams.server.DelayedTask;
import net.minecraft.server.MinecraftServer;

public interface DelayedTaskApi {

    // 在指定 tick 后执行一次任务
    static DelayedTask delay(MinecraftServer server, float ticks, Runnable action) {
        return DelayedTask.create(server, ticks, action);
    }

    // 在指定秒后执行一次任务
    static DelayedTask delaySeconds(MinecraftServer server, float seconds, Runnable action) {
        return DelayedTask.createFromSecond(server, seconds, action);
    }

    // 按秒间隔重复执行指定次数
    static void repeatSeconds(MinecraftServer server, int times, float seconds, Runnable action) {
        DelayedTask.repeat(server, times, seconds, action);
    }

    // 按 tick 间隔重复执行指定次数
    static void repeatTicks(MinecraftServer server, int times, int ticks, Runnable action) {
        DelayedTask.repeat(server, times, ticks, action);
    }

    // 每隔一段时间检查条件，满足时执行任务
    static void until(MinecraftServer server,
                      DelayedTask.BooleanPredicate predicate,
                      float intervalSeconds,
                      Runnable action) {
        DelayedTask.when(server, predicate, intervalSeconds, action, () -> {});
    }
}