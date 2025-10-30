package cc.thonly.reverie_dreams.server;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@ToString
public class DelayedTask {
    public static final List<DelayedTask> TASKS = new ArrayList<>();
    private MinecraftServer server;
    private float ticksLeft;
    private Runnable action;

    public DelayedTask(MinecraftServer server, float ticksLeft, Runnable action) {
        this.server = server;
        this.ticksLeft = ticksLeft;
        this.action = action;
        TASKS.add(this);
    }

    public static synchronized DelayedTask create(MinecraftServer server, float ticksLeft, Runnable action) {
        return new DelayedTask(server, ticksLeft, action);
    }

    public static synchronized DelayedTask createFromSecond(MinecraftServer server, float second, Runnable action) {
        return new DelayedTask(server, second * 20, action);
    }

    public static synchronized void when(MinecraftServer server, BooleanPredicate predicate, float intervalSeconds, Runnable action, Runnable elseAction) {
        createFromSecond(server, intervalSeconds, () -> {
            if (predicate.get()) {
                action.run();
            } else {
                elseAction.run();
                when(server, predicate, intervalSeconds, action, elseAction);
            }
        });
    }

    public static synchronized void whenTick(MinecraftServer server, BooleanPredicate predicate, float intervalTick, Runnable action, Runnable elseAction) {
        create(server, intervalTick, () -> {
            if (predicate.get()) {
                action.run();
            } else {
                elseAction.run();
                whenTick(server, predicate, intervalTick, action, elseAction);
            }
        });
    }


    public static synchronized void repeat(MinecraftServer server, int times, float intervalSeconds, Runnable action) {
        if (times <= 0) return;
        createFromSecond(server, intervalSeconds, () -> {
            action.run();
            repeat(server, times - 1, intervalSeconds, action);
        });
    }

    public static synchronized void repeat(MinecraftServer server, int times, int intervalTick, Runnable action) {
        if (times <= 0) return;
        create(server, intervalTick, () -> {
            action.run();
            repeat(server, times - 1, intervalTick, action);
        });
    }


    public static synchronized void tick(MinecraftServer server) {
        Set<DelayedTask> collect = TASKS.stream().filter(t -> t.server.equals(server)).collect(Collectors.toSet());
        for (var task : collect) {
            task.tick();
        }
    }

    public void stop() {
        TASKS.remove(this);
    }

    public synchronized boolean tick() {
        if (--this.ticksLeft <= 0) {
            this.action.run();
            TASKS.remove(this);
            return true;
        }
        return false;
    }

    public interface BooleanPredicate {
        Boolean get();
    }
}
