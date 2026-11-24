package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuOp;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuSpawner;
import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuLaserEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuFamiliarEntity;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.SequencedSet;
import java.util.function.Predicate;

/**
 * {@link DanmakuPattern} 的运行时实例。
 *
 * <p>每个图案实例维护自己的 tick 计数与发射器时间线，由 {@link DanmakuDirector} 统一驱动。
 * phaseOwned（阶段持有子弹）的实例还会按时间线应用图案级操作（{@link DanmakuOp}）。
 */
public final class DanmakuPatternRuntime {
    private static final double TARGET_SEARCH_RADIUS = 48.0D;

    private final DanmakuPattern pattern;
    private final ServerLevel world;
    @Nullable
    private final Entity source;
    @Nullable
    private final Entity caster;
    private final SpellCastOrigin origin;
    @Nullable
    private final SpellCastOrigin viewOrigin;
    private final int timelineEnd;
    private final List<DanmakuEmitterRuntime> emitters;
    private final DanmakuLaserRuntime lasers;
    private final DanmakuStrikeRuntime strikes;
    private final PriorityQueue<DanmakuEmitterRuntime> scheduled = new PriorityQueue<>(
            Comparator.comparingInt(DanmakuEmitterRuntime::nextFireTick).thenComparingInt(DanmakuEmitterRuntime::order));
    private boolean targetSampled;
    @Nullable
    private Vec3 sampledTarget;
    @Nullable
    private final Entity target;
    @Nullable
    private final SequencedSet<Entity> ownedEntities;
    /**
     * phaseOwned 时待生成/已生成的使魔实例。
     */
    private final List<FamiliarInstance> familiars = new ArrayList<>();
    private int tick = -1;
    private int previousAttackTick = -1;
    private boolean canceled = false;

    /**
     * 一个待生成/已生成的使魔及其子图案运行时。
     */
    private static final class FamiliarInstance {
        final DanmakuFamiliar spec;
        @Nullable
        DanmakuFamiliarEntity entity;
        @Nullable
        DanmakuPatternRuntime runtime;

        FamiliarInstance(DanmakuFamiliar spec) {
            this.spec = spec;
        }
    }

    public DanmakuPatternRuntime(DanmakuPattern pattern, ServerLevel world, @Nullable Entity source, Vec3 origin) {
        this(pattern, world, source, source, new SpellCastOrigin(world, source, origin), null, null,
                source != null || pattern.requiresOwnership());
    }

    private DanmakuPatternRuntime(DanmakuPattern pattern, ServerLevel world, @Nullable Entity source, @Nullable Entity caster,
                                  SpellCastOrigin origin, @Nullable SpellCastOrigin viewOrigin,
                                  @Nullable Entity target, boolean phaseOwned) {
        this.pattern = pattern;
        this.world = world;
        this.source = source;
        this.caster = caster;
        this.origin = origin;
        this.viewOrigin = viewOrigin;
        this.timelineEnd = pattern.timing().isTimed() ? pattern.timing().duration() - 1 : pattern.maxDuration();
        this.target = target;
        this.ownedEntities = phaseOwned ? new LinkedHashSet<>() : null;
        if (phaseOwned) {
            for (DanmakuFamiliar familiar : pattern.familiars()) {
                this.familiars.add(new FamiliarInstance(familiar));
            }
        }
        this.emitters = new ArrayList<>(pattern.emitters().size());
        this.lasers = new DanmakuLaserRuntime(pattern.lasers());
        this.strikes = new DanmakuStrikeRuntime(pattern.strikes());
        for (int index = 0; index < pattern.emitters().size(); index++) {
            this.emitters.add(new DanmakuEmitterRuntime(pattern.emitters().get(index), this, index));
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

    /**
     * Start a held spell at the player's torso, with each volley aimed in view space.
     */
    public static DanmakuPatternRuntime startFromView(ServerLevel world, Player player, DanmakuPattern pattern) {
        return startFromFacing(world, player, pattern);
    }

    /**
     * Local-space moves can use any entity as their source, with no player-item dependency.
     */
    public static DanmakuPatternRuntime startFromFacing(ServerLevel world, Entity source, DanmakuPattern pattern) {
        SpellCastOrigin origin = SpellCastOrigin.fromBody(world, source);
        DanmakuPatternRuntime runtime = new DanmakuPatternRuntime(pattern, world, source, source, origin, origin, null, true);
        DanmakuDirector.add(runtime);
        return runtime;
    }

    @Nullable
    public static DanmakuPatternRuntime castFromFacing(ServerLevel world, Entity source, Identifier patternId) {
        if (source.level() != world || SpellCastOrigin.wasDefeated(source)) {
            return null;
        }
        DanmakuPattern pattern = BuiltInRegistryProviders.DANMAKU_PATTERN.findValue(patternId).orElse(null);
        return pattern == null ? null : startFromFacing(world, source, pattern);
    }

    @Nullable
    public static DanmakuPatternRuntime cast(ServerLevel world, Entity source, Identifier patternId) {
        DanmakuPattern pattern = BuiltInRegistryProviders.DANMAKU_PATTERN.findValue(patternId).orElse(null);
        if (pattern == null) {
            return null;
        }
        return source instanceof Player player ? startFromView(world, player, pattern) : start(world, source, pattern);
    }

    /**
     * 启动由阶段持有的图案。保留返回值，切换阶段时调用 {@link #stopAndClear()}。
     * 未绑定到源实体的发射器可以从与 Boss 高度不同的锚点发射。
     */
    @Nullable
    public static DanmakuPatternRuntime castPhase(ServerLevel world, Entity source, Vec3 origin, Entity target,
                                                  Identifier patternId) {
        DanmakuPattern pattern = BuiltInRegistryProviders.DANMAKU_PATTERN.findValue(patternId).orElse(null);
        if (pattern == null || !source.isAlive() || source.level() != world || !target.isAlive() || target.level() != world) {
            return null;
        }
        DanmakuPatternRuntime runtime = new DanmakuPatternRuntime(pattern, world, source, source,
                new SpellCastOrigin(world, source, origin), null, target, true);
        DanmakuDirector.add(runtime);
        return runtime;
    }

    public void tick() {
        if (this.canceled) {
            return;
        }
        if (!this.refreshOrigin()) {
            this.stopAndClear();
            return;
        }
        if (this.target != null && (!this.target.isAlive() || this.target.level() != this.world)) {
            this.stopAndClear();
            return;
        }
        this.targetSampled = false;
        this.tick++;
        if (this.pattern.timing().hasExpired(this.tick)) {
            this.stopAndClear();
            return;
        }
        int attackTick = this.pattern.timing().attackTick(this.tick);
        if (this.previousAttackTick < 0 || attackTick <= this.previousAttackTick) {
            this.scheduled.clear();
            this.lasers.seek(attackTick);
            this.strikes.reset();
            for (DanmakuEmitterRuntime emitter : this.emitters) {
                this.schedule(emitter, attackTick);
            }
        }
        this.previousAttackTick = attackTick;
        while (!this.scheduled.isEmpty() && this.scheduled.peek().nextFireTick() <= attackTick) {
            DanmakuEmitterRuntime emitter = this.scheduled.remove();
            emitter.fire(attackTick);
            this.schedule(emitter, attackTick + 1);
        }
        this.tickFamiliars(attackTick);
        this.applyDueOps(attackTick);
        this.lasers.fire(attackTick, this::spawnLaser);
        this.strikes.fire(attackTick, this, this.origin);
        if (!this.pattern.timing().isTimed() && attackTick >= this.timelineEnd
                && (this.ownedEntities == null || this.ownedEntities.isEmpty())
                && this.familiars.stream().noneMatch(f -> f.runtime != null && !f.runtime.isDone())) {
            this.canceled = true;
        }
    }

    private void schedule(DanmakuEmitterRuntime emitter, int fromTick) {
        if (emitter.scheduleFrom(fromTick)) {
            this.scheduled.add(emitter);
        }
    }

    private void spawnLaser(DanmakuLaser laser) {
        var placement = DanmakuLaserRuntime.place(laser, this.getOrigin(), this.origin.yaw(), this.viewFrame());
        DanmakuLaserEntity beam = DanmakuSpawner.spawnBeam(this.world, this.getCaster(), placement.origin(),
                placement.cue(), DanmakuTypes.LASER);
        if (beam != null) {
            beam.setCastOffset(placement.origin().subtract(this.getOrigin()));
            this.track(beam);
        }
    }

    /**
     * 在本轮图案达到使魔的发射时刻时生成实体，
     * 并以其为发射源、同一目标启动 phaseOwned 子图案运行时。
     */
    private void tickFamiliars(int attackTick) {
        for (FamiliarInstance instance : this.familiars) {
            if (instance.entity != null && instance.entity.isAlive()) {
                continue;
            }
            DanmakuFamiliar spec = instance.spec;
            if (attackTick != spec.delay()) {
                continue;
            }
            DanmakuFamiliarEntity entity = DanmakuFamiliarEntity.spawn(this.world, this.origin, spec, this.viewOrigin != null);
            if (entity == null) {
                continue;
            }
            DanmakuPatternRuntime subRuntime = new DanmakuPatternRuntime(
                    spec.pattern(), this.world, entity, this.caster,
                    new SpellCastOrigin(this.world, entity, entity.position()), this.viewOrigin, this.target, true);
            DanmakuDirector.add(subRuntime);
            instance.entity = entity;
            instance.runtime = subRuntime;
        }
    }

    /**
     * phaseOwned 时按 at 应用图案级操作；每个操作只应用一次。非 phaseOwned 忽略。
     */
    private void applyDueOps(int attackTick) {
        for (DanmakuOp op : this.pattern.ops()) {
            if (op.at() == attackTick) {
                this.applyOp(op);
            }
        }
    }

    private void applyOp(DanmakuOp op) {
        if (this.ownedEntities == null || this.ownedEntities.isEmpty()) {
            return;
        }
        Vec3 redirectTarget = op.is(DanmakuOp.REDIRECT) ? this.targetPosition() : null;
        DanmakuPatternOperations.apply(op, this.ownedEntities, this.world, redirectTarget);
    }

    public boolean isDone() {
        return this.canceled;
    }

    /**
     * Stops this cast's emitter schedule; already spawned entities keep their own lifetime.
     */
    public void stopEmitting() {
        this.canceled = true;
    }

    /**
     * @deprecated Use {@link #stopEmitting()} to make projectile retention explicit.
     */
    @Deprecated
    public void cancel() {
        this.stopEmitting();
    }

    /**
     * @deprecated Use {@link #stopAndClear()}.
     */
    @Deprecated
    public void cancelAndClear() {
        this.stopAndClear();
    }

    public void track(@Nullable Entity entity) {
        if (entity == null || this.ownedEntities == null || entity.isRemoved()) {
            return;
        }
        if (entity instanceof BaseDanmakuEntity bullet) {
            bullet.setSpellRuntime(this);
        } else if (entity instanceof DanmakuLaserEntity laser) {
            laser.setSpellRuntime(this);
        } else {
            return;
        }
        this.ownedEntities.add(entity);
    }

    public void untrack(Entity entity) {
        if (this.ownedEntities != null) {
            this.ownedEntities.remove(entity);
        }
    }

    /**
     * 停止发射，并移除仍在本世界中的本阶段子弹与使魔；可在发射自然结束后调用。
     */
    public void stopAndClear() {
        this.stopEmitting();
        // 先递归取消子图案（含各自子弹与嵌套使魔），再移除本层的使魔实体
        for (FamiliarInstance instance : this.familiars) {
            if (instance.runtime != null) {
                instance.runtime.stopAndClear();
            }
            if (instance.entity != null && instance.entity.isAlive()) {
                instance.entity.discard();
            }
        }
        if (this.ownedEntities == null) {
            return;
        }
        while (!this.ownedEntities.isEmpty()) {
            this.ownedEntities.removeFirst().discard();
        }
    }

    public boolean refreshOrigin() {
        return this.origin.update() && (this.source == this.caster
                || this.origin.entity() != null && !this.isCasterDefeated());
    }

    public Vec3 anchorPosition() {
        return this.origin.position();
    }

    @Nullable
    public Vec3 targetPosition() {
        if (!this.targetSampled) {
            Entity target = this.findTarget();
            this.sampledTarget = target == null ? null : target.getEyePosition();
            this.targetSampled = true;
        }
        return this.sampledTarget;
    }

    @Nullable
    private Entity findTarget() {
        if (this.target != null) {
            return this.target.isAlive() && this.target.level() == this.world ? this.target : null;
        }
        if (this.origin.entity() instanceof Mob mob && mob.getTarget() != null && mob.getTarget().isAlive()) {
            return mob.getTarget();
        }
        Predicate<Entity> predicate = entity -> entity instanceof Player player
                && entity != this.caster
                && !player.isSpectator()
                && !player.isCreative();
        Vec3 position = this.origin.position();
        return this.world.getNearestPlayer(position.x, position.y, position.z, TARGET_SEARCH_RADIUS, predicate);
    }

    public DanmakuPattern getPattern() {
        return this.pattern;
    }

    public ServerLevel getWorld() {
        return this.world;
    }

    @Nullable
    public Entity getSource() {
        return this.origin.entity();
    }

    @Nullable
    public Entity getCaster() {
        return this.caster != null && !this.caster.isRemoved() && this.caster.level() == this.world ? this.caster : null;
    }

    public boolean hasPlayerCaster() {
        return this.caster instanceof Player;
    }

    public boolean isCasterDefeated() {
        return SpellCastOrigin.wasDefeated(this.caster);
    }

    public Vec3 getOrigin() {
        return this.origin.position();
    }

    Vec3 sourceMuzzlePosition() {
        return this.origin.sourcePosition();
    }

    float sourcePitch(float fallback) {
        return this.origin.hasEntityPose() ? this.origin.pitch() : fallback;
    }

    float sourceYaw(float fallback) {
        return this.origin.hasEntityPose() ? this.origin.yaw() : fallback;
    }

    @Nullable
    SpellAimFrame viewFrame() {
        return this.viewOrigin == null ? null : this.viewOrigin.aimFrame();
    }

    public int getTick() {
        return this.tick;
    }
}
