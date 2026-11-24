package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.api.registry.SerializableProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * STG 图案（符卡）：一组 {@link DanmakuEmitter} 的时间线，
 * 可选携带使魔（{@link DanmakuFamiliar}）与图案级操作（{@link DanmakuOp}）。
 *
 * <p>图案可以通过 Java 注册（内置符卡），也可以通过数据包放在
 * {@code data/<namespace>/danmaku_pattern/<name>.json} 中热重载。
 */
public record DanmakuPattern(List<DanmakuEmitter> emitters, List<DanmakuFamiliar> familiars,
                             List<DanmakuOp> ops, List<DanmakuLaser> lasers,
                             SpellTiming timing,
                             List<DanmakuStrike> strikes) implements SerializableProvider<DanmakuPattern>, BuiltinObject {
    public static final Codec<DanmakuPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DanmakuEmitter.CODEC.listOf().fieldOf("emitters").forGetter(DanmakuPattern::emitters),
            DanmakuFamiliar.CODEC.listOf().optionalFieldOf("familiars", List.of()).forGetter(DanmakuPattern::familiars),
            DanmakuOp.CODEC.listOf().optionalFieldOf("ops", List.of()).forGetter(DanmakuPattern::ops),
            DanmakuLaser.CODEC.listOf().optionalFieldOf("lasers", List.of()).forGetter(DanmakuPattern::lasers),
            SpellTiming.CODEC.optionalFieldOf("timing", SpellTiming.UNTIMED).forGetter(DanmakuPattern::timing),
            DanmakuStrike.CODEC.listOf().optionalFieldOf("strikes", List.of()).forGetter(DanmakuPattern::strikes)
    ).apply(instance, DanmakuPattern::new));

    /**
     * 旧参数顺序的兼容构造器（无使魔、无图案级操作）。
     */
    public DanmakuPattern(List<DanmakuEmitter> emitters) {
        this(emitters, List.of(), List.of());
    }

    public DanmakuPattern(List<DanmakuEmitter> emitters, List<DanmakuFamiliar> familiars, List<DanmakuOp> ops) {
        this(emitters, familiars, ops, List.of(), SpellTiming.UNTIMED);
    }

    public DanmakuPattern(List<DanmakuEmitter> emitters, List<DanmakuFamiliar> familiars, List<DanmakuOp> ops,
                          List<DanmakuLaser> lasers, SpellTiming timing) {
        this(emitters, familiars, ops, lasers, timing, List.of());
    }

    public DanmakuPattern {
        emitters = List.copyOf(emitters);
        familiars = List.copyOf(familiars);
        ops = List.copyOf(ops);
        lasers = List.copyOf(lasers);
        strikes = List.copyOf(strikes);
    }

    public static DanmakuPattern of(DanmakuEmitter... emitters) {
        return new DanmakuPattern(Arrays.asList(emitters));
    }

    public DanmakuPattern withFamiliars(List<DanmakuFamiliar> familiars) {
        return new DanmakuPattern(this.emitters, familiars, this.ops, this.lasers, this.timing, this.strikes);
    }

    public DanmakuPattern withOps(List<DanmakuOp> ops) {
        return new DanmakuPattern(this.emitters, this.familiars, ops, this.lasers, this.timing, this.strikes);
    }

    public DanmakuPattern withTiming(SpellTiming timing) {
        return new DanmakuPattern(this.emitters, this.familiars, this.ops, this.lasers, timing, this.strikes);
    }

    public DanmakuPattern withLasers(List<DanmakuLaser> lasers) {
        return new DanmakuPattern(this.emitters, this.familiars, this.ops, lasers, this.timing, this.strikes);
    }

    public DanmakuPattern withStrikes(List<DanmakuStrike> strikes) {
        return new DanmakuPattern(this.emitters, this.familiars, this.ops, this.lasers, this.timing, strikes);
    }

    public boolean requiresOwnership() {
        return this.timing.isTimed() || !this.familiars.isEmpty() || !this.ops.isEmpty() || !this.lasers.isEmpty()
                || !this.strikes.isEmpty();
    }

    public int maxDuration() {
        int max = 0;
        for (DanmakuEmitter emitter : this.emitters) {
            max = Math.max(max, emitter.delay() + Math.max(0, emitter.duration()));
        }
        // 图案级操作与使魔时间线也纳入图案存活窗口
        for (DanmakuOp op : this.ops) {
            max = Math.max(max, op.at());
        }
        for (DanmakuFamiliar familiar : this.familiars) {
            int familiarEnd = familiar.delay()
                    + (familiar.duration() > 0 ? familiar.duration() : familiar.pattern().maxDuration());
            max = Math.max(max, familiarEnd);
        }
        for (DanmakuLaser laser : this.lasers) {
            max = Math.max(max, laser.at() + laser.warnTicks() + laser.activeTicks());
        }
        for (DanmakuStrike strike : this.strikes) {
            max = Math.max(max, SpellTiming.framesToTicks(strike.untilFrame()) - 1);
        }
        return max;
    }

    @Override
    public Codec<DanmakuPattern> getCodec() {
        return CODEC;
    }
}
