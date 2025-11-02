package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.danmaku.trajectory.*;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.resources.ResourceLocation;

public class DanmakuTrajectories {
    public static final DanmakuTrajectory SINGLE = register(ReverieDreams.id("single"), new SingleTrajectory());
    public static final DanmakuTrajectory TRIPLE = register(ReverieDreams.id("triple"), new TripleTrajectory());
    public static final DanmakuTrajectory BULLET = register(ReverieDreams.id("bullet"), new BulletTrajectory());
    public static final DanmakuTrajectory STAR = register(ReverieDreams.id("star"), new PatternTrajectory(Pattern.BuiltIn.STAR));
    public static final DanmakuTrajectory HEART = register(ReverieDreams.id("heart"), new PatternTrajectory(Pattern.BuiltIn.HEART));
    public static final DanmakuTrajectory X = register(ReverieDreams.id("x"), new PatternTrajectory(Pattern.BuiltIn.X));
    public static final DanmakuTrajectory ROUND = register(ReverieDreams.id("round"), new RoundTrajectory());
    public static final DanmakuTrajectory RING = register(ReverieDreams.id("ring"), new RingTrajectory());

    public static DanmakuTrajectory register(ResourceLocation key, DanmakuTrajectory value) {
        return RegistryManager.registerForBuiltin(RegistryManager.DANMAKU_TRAJECTORY, key, value);
    }

    public static void bootstrap(IntrinsicalRegister<DanmakuTrajectory> registry) {

    }
}
