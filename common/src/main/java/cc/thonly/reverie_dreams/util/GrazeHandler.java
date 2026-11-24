package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side graze detection driven by each projectile.
 *
 * <p>No entity list is traversed here: a bullet only asks the level for the
 * nearest player inside the small graze radius. Calls are throttled by the
 * bullet to every other tick, and each bullet can graze only once.</p>
 */
public final class GrazeHandler {
    private static final int SOUND_COOLDOWN_TICKS = 3;
    private static final double GRAZE_RADIUS = 1.5D;
    private static final double GRAZE_QUERY_RADIUS = 3.0D;
    private static final double HIT_EXCLUSION = 0.05D;
    private static final Map<UUID, Integer> LAST_GRAZE_SOUND = new HashMap<>();

    private GrazeHandler() {
    }

    public static void clear() {
        LAST_GRAZE_SOUND.clear();
    }

    public static void tryGraze(BaseDanmakuEntity bullet) {
        if (bullet.hasGrazed() || bullet.isRemoved()) {
            return;
        }
        if (!(bullet.level() instanceof ServerLevel level)) {
            return;
        }

        Entity owner = bullet.getOwner();
        if (owner instanceof Player) {
            return;
        }

        Player nearest = level.getNearestPlayer(
                bullet.getX(), bullet.getY(), bullet.getZ(), GRAZE_QUERY_RADIUS,
                entity -> entity instanceof ServerPlayer serverPlayer
                        && serverPlayer.isAlive()
                        && !serverPlayer.isSpectator()
                        && !serverPlayer.isCreative()
        );
        if (!(nearest instanceof ServerPlayer player)) {
            return;
        }

        if (owner != null && owner.isAlliedTo(player)) {
            return;
        }

        // A bullet already intersecting the player's hitbox is a hit, not a graze.
        if (player.getBoundingBox().inflate(HIT_EXCLUSION).intersects(bullet.getBoundingBox())) {
            return;
        }

        double dx = bullet.getX() - player.getX();
        double dy = bullet.getY() + bullet.getBbHeight() * 0.5D
                - (player.getY() + player.getBbHeight() * 0.5D);
        double dz = bullet.getZ() - player.getZ();
        if (dx * dx + dy * dy + dz * dz > GRAZE_RADIUS * GRAZE_RADIUS) {
            return;
        }

        int now = level.getServer().getTickCount();
        Integer lastSound = LAST_GRAZE_SOUND.get(player.getUUID());
        if (lastSound != null && now - lastSound < SOUND_COOLDOWN_TICKS) {
            return;
        }

        bullet.markGrazed();
        LAST_GRAZE_SOUND.put(player.getUUID(), now);
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                RDSoundEvents.GRAZE.value(),
                SoundSource.PLAYERS,
                0.45F,
                1.3F + level.getRandom().nextFloat() * 0.15F
        );
    }
}
