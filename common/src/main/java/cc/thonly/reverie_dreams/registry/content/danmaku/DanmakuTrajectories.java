package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.Pattern;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.*;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.resources.Identifier;

public class DanmakuTrajectories {
    public static final DanmakuTrajectory SINGLE = register(ReverieDreams.id("single"), new SingleTrajectory());
    public static final DanmakuTrajectory TRIPLE = register(ReverieDreams.id("triple"), new TripleTrajectory());
    public static final DanmakuTrajectory BULLET = register(ReverieDreams.id("bullet"), new BulletTrajectory());
    public static final DanmakuTrajectory STAR = register(ReverieDreams.id("star"), new PatternTrajectory(Pattern.BuiltIn.STAR));
    public static final DanmakuTrajectory HEART = register(ReverieDreams.id("heart"), new PatternTrajectory(Pattern.BuiltIn.HEART));
    public static final DanmakuTrajectory X = register(ReverieDreams.id("x"), new PatternTrajectory(Pattern.BuiltIn.X));
    public static final DanmakuTrajectory ROUND = register(ReverieDreams.id("round"), new RoundTrajectory());
    public static final DanmakuTrajectory RING = register(ReverieDreams.id("ring"), new RingTrajectory());
    public static final DanmakuTrajectory CUSTOM = register(ReverieDreams.id("custom"), new CustomTrajectory());

    public static DanmakuTrajectory register(Identifier key, DanmakuTrajectory value) {
        return BuiltInRegistryProviders.registerForBuiltin(BuiltInRegistryProviders.DANMAKU_TRAJECTORY, key, value);
    }

    public static void bootstrap(RegistryProvider<DanmakuTrajectory> registry) {

    }
}
