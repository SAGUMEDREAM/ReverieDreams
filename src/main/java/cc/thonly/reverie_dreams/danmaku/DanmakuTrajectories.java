package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.danmaku.trajectory.*;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.util.Identifier;

public class DanmakuTrajectories {
    public static final DanmakuTrajectory SINGLE = register(Touhou.id("single"), new SingleTrajectory());
    public static final DanmakuTrajectory TRIPLE = register(Touhou.id("triple"), new TripleTrajectory());
    public static final DanmakuTrajectory BULLET = register(Touhou.id("bullet"), new BulletTrajectory());
    public static final DanmakuTrajectory STAR = register(Touhou.id("star"), new PatternTrajectory(Pattern.BuiltIn.STAR));
    public static final DanmakuTrajectory HEART = register(Touhou.id("heart"), new PatternTrajectory(Pattern.BuiltIn.HEART));
    public static final DanmakuTrajectory X = register(Touhou.id("x"), new PatternTrajectory(Pattern.BuiltIn.X));
    public static final DanmakuTrajectory ROUND = register(Touhou.id("round"), new RoundTrajectory());
    public static final DanmakuTrajectory RING = register(Touhou.id("ring"), new RingTrajectory());

    public static DanmakuTrajectory register(Identifier key, DanmakuTrajectory value) {
        return RegistryManager.registerForBuiltin(RegistryManager.DANMAKU_TRAJECTORY, key, value);
    }

    public static void bootstrap(IntrinsicalRegister<DanmakuTrajectory> registry) {

    }
}
