package cc.thonly.reverie_dreams.server;

import lombok.Getter;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ParticleTickerManager {
    private static final List<Entry> ENTRIES = new LinkedList<>();

    public static synchronized void joinQueue(ServerLevel world, ParticleOptions particleEffect, int count, Vec3 from, Vec3 to, float durationSeconds) {
        ENTRIES.add(new Entry(world.getServer(), world, from, to, durationSeconds * 20, Present.LINEAR, particleEffect, count));
    }

    public static synchronized void joinQueue(ServerLevel world, ParticleOptions particleEffect, int count, Vec3 from, Vec3 to, Present present, float durationSeconds) {
        ENTRIES.add(new Entry(world.getServer(), world, from, to, durationSeconds * 20, present, particleEffect, count));
    }

    public static synchronized void tick(MinecraftServer server) {
        Iterator<Entry> iterator = ENTRIES.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.getServer() != server) {
                iterator.remove();
                continue;
            }

            if (entry.hasNext()) {
                Vec3 pos = entry.next();
                ParticleOptions particleType = entry.getParticleType();
                PlayerList playerManager = server.getPlayerList();
                ServerLevel world = entry.getWorld();
                world.sendParticles(particleType, pos.x,pos.y,pos.z,1,0,0,0,0.1);

            } else {
                iterator.remove();
            }
        }
    }

    @Getter
    public static class Entry {
        private final MinecraftServer server;
        private final ServerLevel world;
        private final Vec3 start;
        private final Vec3 end;
        private final double maxStep;
        private final Present present;
        private final ParticleOptions particleType;
        private final int count;
        private double currentStep = 0;

        public Entry(MinecraftServer server, ServerLevel world, Vec3 start, Vec3 end, double maxStep, ParticleOptions particleType, int count) {
            this(server, world, start, end, maxStep, Present.LINEAR, particleType, count);
        }

        public Entry(MinecraftServer server, ServerLevel world, Vec3 start, Vec3 end, double maxStep, Present present, ParticleOptions particleType, int count) {
            this.server = server;
            this.world = world;
            this.start = start;
            this.end = end;
            this.maxStep = maxStep;
            this.present = present;
            this.particleType = particleType;
            this.count = count;
        }

        public boolean hasNext() {
            if (this.currentStep > maxStep) {
                return false;
            }
            this.currentStep += 1;
            return true;
        }

        public Vec3 next() {
            double t = this.currentStep / this.maxStep;
            this.currentStep += 1;
            return this.present.instance.getVec(t, this.start, this.end);
        }
    }

    public enum Present {
        // 线性
        LINEAR(new PathInstance(t -> t, t -> t, t -> t)),

        // 抛物线(顶点为1)
        PARABOLA(new PathInstance(
                t -> t,
                t -> 4 * t * (1 - t),
                t -> t
        )),

        // 正弦波
        SINE_WAVE(new PathInstance(
                t -> t,
                t -> Math.sin(t * Math.PI * 2) * 0.5 + 0.5,
                t -> t
        )),

        // Z字形
        ZIGZAG(new PathInstance(
                t -> t,
                t -> ((int) (t * 10) % 2 == 0) ? 0.2 : 0.8,
                t -> t
        )),

        // 快入缓出（EaseInOutCubic）
        EASE_IN_OUT_CUBIC(new PathInstance(
                t -> t,
                t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2,
                t -> t
        )),

        // 反弹（BounceOut）
        BOUNCE_OUT(new PathInstance(
                t -> t,
                t -> {
                    final double n1 = 7.5625;
                    final double d1 = 2.75;
                    if (t < 1 / d1) {
                        return n1 * t * t;
                    } else if (t < 2 / d1) {
                        t -= 1.5 / d1;
                        return n1 * t * t + 0.75;
                    } else if (t < 2.5 / d1) {
                        t -= 2.25 / d1;
                        return n1 * t * t + 0.9375;
                    } else {
                        t -= 2.625 / d1;
                        return n1 * t * t + 0.984375;
                    }
                },
                t -> t
        )),

        // 弹簧（ElasticOut）
        ELASTIC_OUT(new PathInstance(
                t -> t,
                t -> {
                    final double c4 = (2 * Math.PI) / 3;
                    return t == 0 ? 0 :
                            t == 1 ? 1 :
                                    Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1;
                },
                t -> t
        )),

        // 回退（BackOut）
        BACK_OUT(new PathInstance(
                t -> t,
                t -> {
                    final double c1 = 1.70158;
                    final double c3 = c1 + 1;
                    return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
                },
                t -> t
        )),


        ;
        public final PathInstance instance;

        Present(PathInstance instance) {
            this.instance = instance;
        }
    }

    public record PathInstance(LinePath x, LinePath y, LinePath z) {
        public Vec3 getVec(double t, Vec3 from, Vec3 to) {
            return new Vec3(
                    x.get(t) * (to.x - from.x) + from.x,
                    y.get(t) * (to.y - from.y) + from.y,
                    z.get(t) * (to.z - from.z) + from.z
            );
        }
    }

    @FunctionalInterface
    public interface LinePath {
        double get(double t);
    }


}