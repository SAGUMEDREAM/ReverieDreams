package cc.thonly.reverie_dreams.util.entity;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Map;

public class IAnimationPreset {
    private static final IAnimationPreset INSTANCE = new IAnimationPreset();
    private final Map<String, RawAnimation> CACHE_PLAY = new Object2ObjectOpenHashMap<>();
    private final Map<String, RawAnimation> CACHE_LOOP = new Object2ObjectOpenHashMap<>();
    private final Map<Integer, RawAnimation> CACHE_WAIT = new Object2ObjectOpenHashMap<>();

    public RawAnimation play(String animationName) {
        return CACHE_PLAY.computeIfAbsent(animationName, x -> RawAnimation.begin().thenPlay(animationName));
    }

    public RawAnimation loop(String animationName) {
        return CACHE_LOOP.computeIfAbsent(animationName, x -> RawAnimation.begin().thenLoop(animationName));
    }

    public RawAnimation wait(int ticks) {
        return CACHE_WAIT.computeIfAbsent(ticks, x -> RawAnimation.begin().thenWait(ticks));
    }

    public RawAnimation idle() {
        return loop("idle");
    }

    public RawAnimation walk() {
        return loop("walk");
    }

    public RawAnimation attackLoop() {
        return loop("attack");
    }

    public RawAnimation attack() {
        return play("attack");
    }

    public RawAnimation die() {
        return play("die");
    }

    public static IAnimationPreset getInstance() {
        return INSTANCE;
    }
}
