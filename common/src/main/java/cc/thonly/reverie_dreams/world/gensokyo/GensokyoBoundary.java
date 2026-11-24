package cc.thonly.reverie_dreams.world.gensokyo;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapLoader;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Boundary protection for the fixed Gensokyo map. Everything outside the map
 * is protected: player-caused block writes, blasts and piston moves are all
 * refused, while world generation is left alone (it never goes through
 * {@code Level.setBlock}, so it is unaffected).
 *
 * <p>Movement rules: non-spectator players, vehicles and projectiles are confined to the
 * exact map rectangle; the {@link #BORDER_TRANSITION_BLOCKS}-block decorative ring beyond
 * the map is scenery only (it exists for the Distant Horizons LOD skip and is not
 * walkable). Mobs stay inside the map rectangle too, and spectators are unrestricted.
 * Collision walls alone cannot stop teleports or mounts, so the server also clamps
 * players and mobs once per tick and refuses mob spawns outside the map.</p>
 *
 * <p>Survival proximity feedback is rendered client-side by
 * {@code GensokyoBoundaryGizmos}, using the same red barrier-box style as
 * vanilla's block entity bounding box renderer.</p>
 *
 * @see cc.thonly.reverie_dreams.mixin.world.LevelBoundaryWriteMixin
 * @see cc.thonly.reverie_dreams.mixin.world.ServerExplosionBoundaryMixin
 * @see cc.thonly.reverie_dreams.mixin.world.PistonBoundaryMixin
 */
public final class GensokyoBoundary {
    /**
     * 24 chunks: the same margin the Distant Horizons LOD generator skips beyond the map.
     */
    public static final int BORDER_TRANSITION_CHUNKS = 24;
    public static final int BORDER_TRANSITION_BLOCKS = BORDER_TRANSITION_CHUNKS * 16;

    private GensokyoBoundary() {
    }

    public static boolean applies(Level level) {
        return level.dimension().equals(RDBuiltinLevels.GENSOKYO) && GensokyoFeature.isEnabled();
    }

    /**
     * Movement bounds for non-spectator players: the exact map rectangle.
     */
    public static GensokyoBoundaryCollision.Bounds playerBounds(Level level) {
        return GensokyoBoundaryCollision.bounds(level);
    }

    public static boolean isOutsideMap(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(RDBuiltinLevels.GENSOKYO)) {
            return false;
        }
        GensokyoMapData map = GensokyoMapLoader.get(GensokyoChunkGenerator.DEFAULT_MAP);
        return !map.contains(pos.getX(), pos.getZ());
    }

    public static boolean isOutsideMap(Level level, double x, double z) {
        return applies(level) && !GensokyoBoundaryCollision.bounds(level).contains(x, z);
    }

    public static boolean isOutsidePlayerArea(Level level, double x, double z) {
        return applies(level) && !playerBounds(level).contains(x, z);
    }

    /**
     * Safety net for teleports and mounts that bypass the collision perimeter.
     */
    public static void confinePlayer(ServerPlayer player) {
        Level level = player.level();
        if (!applies(level) || player.isSpectator()) {
            return;
        }
        GensokyoBoundaryCollision.Bounds bounds = playerBounds(level);
        if (bounds.contains(player.getX(), player.getZ())) {
            return;
        }
        double x = bounds.clampX(player.getX());
        double z = bounds.clampZ(player.getZ());
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            player.stopRiding();
            vehicle.teleportTo(x, vehicle.getY(), z);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(x, player.getY(), z);
    }

    /**
     * Mobs never leave the map rectangle, whether by AI, knockback or a mob teleport.
     */
    public static void confineMob(Mob mob) {
        Level level = mob.level();
        if (!applies(level)) {
            return;
        }
        GensokyoBoundaryCollision.Bounds bounds = GensokyoBoundaryCollision.bounds(level);
        if (bounds.contains(mob.getX(), mob.getZ())) {
            return;
        }
        double x = bounds.clampX(mob.getX());
        double z = bounds.clampZ(mob.getZ());
        if (mob.getVehicle() != null) {
            mob.stopRiding();
        }
        mob.setDeltaMovement(Vec3.ZERO);
        mob.teleportTo(x, mob.getY(), z);
    }

    /**
     * Ender pearls and similar teleports bypass collision. Relative X/Z transitions carry
     * offsets instead of absolute coordinates, so those are left to the per-tick clamp.
     */
    public static TeleportTransition clampTeleport(ServerPlayer player, TeleportTransition transition) {
        if (!applies(transition.newLevel())
                || player.isSpectator()
                || transition.asPassenger()
                || transition.relatives().contains(Relative.X)
                || transition.relatives().contains(Relative.Z)) {
            return transition;
        }
        Vec3 position = transition.position();
        GensokyoBoundaryCollision.Bounds bounds = playerBounds(transition.newLevel());
        if (bounds.contains(position.x, position.z)) {
            return transition;
        }
        return transition.withPosition(new Vec3(bounds.clampX(position.x), position.y, bounds.clampZ(position.z)));
    }

    /**
     * Natural spawning, spawners and spawn eggs all end up in {@code ServerLevel.addFreshEntity}.
     */
    public static boolean refuseMobSpawn(ServerLevel level, Entity entity) {
        return entity instanceof Mob && isOutsideMap(level, entity.getX(), entity.getZ());
    }
}
