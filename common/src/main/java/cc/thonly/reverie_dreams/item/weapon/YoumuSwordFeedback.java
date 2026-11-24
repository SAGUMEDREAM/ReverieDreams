package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Cosmetic only: vanilla particles, bounded local recipients, and owner-only action bars.
 */
public final class YoumuSwordFeedback {
    public static final int TEAL = 0x35A6B3, LIGHT = 0x74D6CD, WHITE = 0xC7F5E4;
    private static final DustParticleOptions SPIRIT = new DustParticleOptions(LIGHT, .8f);
    private static final DustParticleOptions EDGE = new DustParticleOptions(WHITE, .65f);

    private YoumuSwordFeedback() {
    }

    public static int chargePercent(long held) {
        return (int) (Math.clamp(held, 0, YoumuSwordCombat.FULL_CHARGE) * 100 / YoumuSwordCombat.FULL_CHARGE);
    }

    public static String chargeKey(long held, boolean counter) {
        if (counter)
            return "counter_ready";
        if (held >= 50)
            return "expiring";
        if (held >= YoumuSwordCombat.FULL_CHARGE)
            return "charged";
        if (held >= YoumuSwordCombat.MIN_CHARGE)
            return "charge_ready";
        return "charging";
    }

    public static MutableComponent text(String key, Object... args) {
        return Component.translatable("item.reverie_dreams.youmu." + key, args);
    }

    public static Component chargeText(long held, boolean counter) {
        if (counter)
            return text("counter_ready").withStyle(s -> s.withColor(WHITE));
        int percent = chargePercent(held), filled = percent / 10;
        return Component.empty().append(Component.literal("■".repeat(filled) + "□".repeat(10 - filled))
                        .withStyle(s -> s.withColor(counter || percent == 100 ? WHITE : TEAL)))
                .append(Component.literal(" " + percent + "% · ").withStyle(s -> s.withColor(WHITE)))
                .append(text(chargeKey(held, counter)).withStyle(s -> s.withColor(WHITE)));
    }

    /**
     * Vertical crescent perpendicular to travel, with safe bases even for vertical aim.
     */
    public static List<Vec3> crescent(Vec3 center, Vec3 forward, double radius, int points) {
        Vec3 direction = forward.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : forward.normalize();
        Vec3 side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1e-8)
            side = new Vec3(1, 0, 0);
        side = side.normalize();
        Vec3 up = side.cross(direction).normalize();
        List<Vec3> result = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double angle = -Math.PI / 2 + Math.PI * i / Math.max(1, points - 1);
            result.add(center.add(side.scale(Math.sin(angle) * radius)).add(up.scale(Math.cos(angle) * radius * .6)));
        }
        return List.copyOf(result);
    }

    public static List<Vec3> ring(Vec3 center, double radius, int points) {
        List<Vec3> result = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            result.add(center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
        }
        return List.copyOf(result);
    }

    private static void dots(ServerLevel world, List<Vec3> points, boolean white) {
        for (Vec3 point : points) {
            for (ServerPlayer viewer : world.players()) {
                if (viewer.distanceToSqr(point) <= 32 * 32) {
                    world.sendParticles(viewer, white ? EDGE : SPIRIT, false, false,
                            point.x, point.y, point.z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    public static void charge(ServerLevel world, Player player, long held) {
        Vec3 look = player.getLookAngle();
        Vec3 side = YoumuSwordGeometry.horizontal(look).cross(new Vec3(0, 1, 0));
        Vec3 center = player.getEyePosition().add(look.scale(.85)).add(side.scale(.4)).add(0, -.35, 0);
        dots(world, crescent(center, look, held >= YoumuSwordCombat.FULL_CHARGE ? .18 : .42, 5), false);
    }

    public static void slash(ServerLevel world, Vec3 center, Vec3 forward, boolean full) {
        Vec3 direction = YoumuSwordGeometry.horizontal(forward), side = direction.cross(new Vec3(0, 1, 0));
        List<Vec3> points = new ArrayList<>();
        for (int i = -7; i <= 7; i++) {
            double angle = i * Math.PI / 12;
            points.add(center.add(direction.scale(Math.cos(angle) * 1.6)).add(side.scale(Math.sin(angle) * 1.6)));
        }
        dots(world, points, full);
    }

    public static void wave(ServerLevel world, Vec3 center, Vec3 forward) {
        dots(world, crescent(center, forward, 1.1, 13), false);
        dots(world, crescent(center.add(forward.normalize().scale(-.12)), forward, .9, 9), true);
    }

    public static void guard(ServerLevel world, Player player) {
        Vec3 look = player.getLookAngle();
        dots(world, crescent(player.getEyePosition().add(look.scale(.9)).add(0, -.25, 0), look, .45, 7), true);
    }

    public static void impact(ServerLevel world, Vec3 at) {
        dots(world, List.of(at.add(.15, 0, 0), at.add(-.15, 0, 0), at.add(0, .15, 0), at.add(0, -.15, 0)), true);
    }

    public static void purification(ServerLevel world, Player player) {
        purification(world, player, .5);
    }

    public static void purification(ServerLevel world, Player player, double radius) {
        dots(world, ring(player.position().add(0, .12, 0), radius, 16), true);
    }

    public static void ascend(ServerLevel world, Vec3 at) {
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            points.add(at.add(Math.sin(i * .8) * .15, i * .13, Math.cos(i * .8) * .15));
        dots(world, points, true);
    }
}
