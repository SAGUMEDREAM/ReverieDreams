package cc.thonly.reverie_dreams.entity.ai;

import cc.thonly.reverie_dreams.api.player.PlayerComponentManager;
import cc.thonly.reverie_dreams.entity.ForestYoukai;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import cc.thonly.reverie_dreams.server.player.FactionReputationComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * The cache shares one player scan per level tick; distances always use current positions.
 */
public final class YoukaiRelations {
    public static final int PEACE_REPUTATION = 50;
    public static final double PEACE_RADIUS = 128;

    private record PeacePlayers(long tick, Map<Long, List<UUID>> cells) {
    }

    private static final Map<ServerLevel, PeacePlayers> PEACE_PLAYERS = new WeakHashMap<>();

    private YoukaiRelations() {
    }

    public static FactionReputationComponent reputation(ServerPlayer player) {
        return PlayerComponentManager.serverAccess()
                .getOrCreatePlayerComponent(player, FactionReputationComponent.class).get();
    }

    public static void invalidatePeacePlayers() {
        PEACE_PLAYERS.clear();
    }

    public static boolean insideSquare(double centerX, double centerZ, double x, double z) {
        return Math.abs(x - centerX) <= PEACE_RADIUS && Math.abs(z - centerZ) <= PEACE_RADIUS;
    }

    public static boolean isHuman(LivingEntity target) {
        return target.getType().builtInRegistryHolder().is(RDEntityTypeTags.HUMAN);
    }

    public static boolean mayHunt(ForestYoukai mob, LivingEntity target) {
        if (!(mob.level() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD) || level.getDifficulty() == Difficulty.PEACEFUL
                || !isHuman(target) || target.level() != level || !target.isAlive())
            return false;
        if (target instanceof Player player) {
            if (player.isCreative() || player.isSpectator())
                return false;
            if (player instanceof ServerPlayer serverPlayer && reputation(serverPlayer).getYokai() >= PEACE_REPUTATION)
                return false;
        }
        var cells = peacePlayers(level);
        int cellX = cell(mob.getX()), cellZ = cell(mob.getZ());
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                var ids = cells.get(cellKey(cellX + dx, cellZ + dz));
                if (ids == null)
                    continue;
                for (UUID id : ids) {
                    if (!(level.getEntity(id) instanceof ServerPlayer source))
                        continue;
                    if (source.level() != level || !source.isAlive() || source.isSpectator()
                            || reputation(source).getYokai() < PEACE_REPUTATION)
                        continue;
                    // An aggressor outside this square is deliberately not stopped by a protected victim.
                    if (insideSquare(source.getX(), source.getZ(), mob.getX(), mob.getZ())
                            && insideSquare(source.getX(), source.getZ(), target.getX(), target.getZ()))
                        return false;
                }
            }
        return true;
    }

    private static int cell(double value) {
        return (int) Math.floor(value / PEACE_RADIUS);
    }

    private static long cellKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static Map<Long, List<UUID>> peacePlayers(ServerLevel level) {
        PeacePlayers old = PEACE_PLAYERS.get(level);
        long tick = level.getGameTime();
        if (old != null && old.tick() == tick)
            return old.cells();
        Map<Long, List<UUID>> cells = new HashMap<>();
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && reputation(player).getYokai() >= PEACE_REPUTATION)
                cells.computeIfAbsent(cellKey(cell(player.getX()), cell(player.getZ())), ignored -> new ArrayList<>()).add(player.getUUID());
        }
        PEACE_PLAYERS.put(level, new PeacePlayers(tick, cells));
        return cells;
    }
}
