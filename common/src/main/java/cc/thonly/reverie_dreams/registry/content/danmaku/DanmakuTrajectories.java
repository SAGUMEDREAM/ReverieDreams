package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.GlyphPattern;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.*;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * One-shot item volley patterns. Spellcard STG patterns live in the separate
 * {@code DanmakuPattern} runtime and must not be mixed into this registry.
 */
public class DanmakuTrajectories {
    private static final Set<Identifier> LEGACY_IDS = new HashSet<>();

    @Deprecated
    public static final DanmakuTrajectory SINGLE = registerLegacy("single", new SingleTrajectory());
    @Deprecated
    public static final DanmakuTrajectory TRIPLE = registerLegacy("triple", new TripleTrajectory());
    @Deprecated
    public static final DanmakuTrajectory FAN = registerLegacy("fan", new FanTrajectory(5, 40.0f));
    @Deprecated
    public static final DanmakuTrajectory BULLET = registerLegacy("bullet", new BulletTrajectory(2, 0.4f));
    @Deprecated
    public static final DanmakuTrajectory STAR = registerLegacy("star", new PatternTrajectory(GlyphPattern.BuiltIn.STAR));
    @Deprecated
    public static final DanmakuTrajectory HEART = registerLegacy("heart", new PatternTrajectory(GlyphPattern.BuiltIn.HEART));
    @Deprecated
    public static final DanmakuTrajectory X = registerLegacy("x", new PatternTrajectory(GlyphPattern.BuiltIn.X));
    @Deprecated
    public static final DanmakuTrajectory ROUND = registerLegacy("round", new RoundTrajectory(12, 1.0f));
    @Deprecated
    public static final DanmakuTrajectory RING = registerLegacy("ring", new RingTrajectory(24, 1.0f, true));
    @Deprecated
    public static final DanmakuTrajectory RING_VIEW = registerLegacy("ring_view", new RoundTrajectory(16, 1.0f));
    @Deprecated
    public static final DanmakuTrajectory RING_HORIZONTAL = registerLegacy("ring_horizontal", new RingTrajectory(16, 1.5f, true));
    /**
     * Legacy spellcard-backed trajectory. Intentionally not registered as a normal template.
     */
    @Deprecated
    public static final DanmakuTrajectory CUSTOM = registerLegacy("custom", new CustomTrajectory());

    private static DanmakuTrajectory registerLegacy(String path, DanmakuTrajectory value) {
        Identifier key = ReverieDreams.id(path);
        DanmakuTrajectory trajectory = register(key, value);
        LEGACY_IDS.add(key);
        return trajectory;
    }

    public static boolean isLegacy(Identifier key) {
        return LEGACY_IDS.contains(key);
    }

    public static DanmakuTrajectory register(Identifier key, DanmakuTrajectory value) {
        return BuiltInRegistryProviders.registerForBuiltin(BuiltInRegistryProviders.DANMAKU_TRAJECTORY, key, value);
    }

    public static void bootstrap(RegistryProvider<DanmakuTrajectory> registry) {
        for (PlayerTrajectoryType type : PlayerTrajectoryType.values()) {
            register(type.id(), new PlayerTrajectory(type));
        }
    }
}
