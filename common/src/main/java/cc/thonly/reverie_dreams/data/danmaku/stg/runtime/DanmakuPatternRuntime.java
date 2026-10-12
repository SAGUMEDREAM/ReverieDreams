package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * {@link DanmakuPattern} 的运行时实例。
 *
 * <p>每个图案实例维护自己的 tick 计数与发射器时间线，由 {@link DanmakuDirector} 统一驱动。
 */
public final class DanmakuPatternRuntime {
    private static final double TARGET_SEARCH_RADIUS = 48.0D;

    private final DanmakuPattern pattern;
    private final ServerLevel world;
    @Nullable
    private final Entity source;
    private final Vec3 origin;
    private final List<DanmakuEmitterRuntime> emitters = new ArrayList<>();
    private int tick = 0;
    private boolean canceled = false;

    public DanmakuPatternRuntime(DanmakuPattern pattern, ServerLevel world, @Nullable Entity source, Vec3 origin) {
        this.pattern = pattern;
        this.world = world;
        this.source = source;
        this.origin = origin;
        for (DanmakuEmitter emitter : pattern.emitters()) {
            this.emitters.add(new DanmakuEmitterRuntime(emitter, this));
        }
    }

    public static DanmakuPatternRuntime start(ServerLevel world, @Nullable Entity source, Vec3 origin, DanmakuPattern pattern) {
        DanmakuPatternRuntime runtime = new DanmakuPatternRuntime(pattern, world, source, origin);
        DanmakuDirector.add(runtime);
        return runtime;
    }

    public static DanmakuPatternRuntime start(ServerLevel world, Entity source, DanmakuPattern pattern) {
        return start(world, source, source.getEyePosition(), pattern);
    }

    public void tick() {
        if (this.canceled) {
            return;
        }
        if (this.source != null && this.source.isRemoved()) {
            this.cancel();
            return;
        }
        this.tick++;
        boolean anyActive = false;
        for (DanmakuEmitterRuntime emitter : this.emitters) {
            if (emitter.tick(this.tick)) {
                anyActive = true;
            }
        }
        if (!anyActive) {
            this.canceled = true;
        }
    }

    public boolean isDone() {
        return this.canceled;
    }

    public void cancel() {
        this.canceled = true;
    }

    /**
     * 锚点：跟随源实体（若存在）或图案启动时记录的位置。
     */
    public Vec3 anchorPosition() {
        if (this.source != null && !this.source.isRemoved()) {
            return this.source.position();
        }
        return this.origin;
    }

    @Nullable
    public Vec3 targetPosition() {
        Entity target = this.findTarget();
        return target == null ? null : target.getEyePosition();
    }

    @Nullable
    private Entity findTarget() {
        if (this.source instanceof Mob mob && mob.getTarget() != null && mob.getTarget().isAlive()) {
            return mob.getTarget();
        }
        Predicate<Entity> predicate = entity -> entity instanceof Player player
                && !player.isSpectator()
                && !player.isCreative();
        return this.world.getNearestPlayer(this.origin.x, this.origin.y, this.origin.z, TARGET_SEARCH_RADIUS, predicate);
    }

    public DanmakuPattern getPattern() {
        return this.pattern;
    }

    public ServerLevel getWorld() {
        return this.world;
    }

    @Nullable
    public Entity getSource() {
        return this.source;
    }

    public Vec3 getOrigin() {
        return this.origin;
    }

    public int getTick() {
        return this.tick;
    }
}
