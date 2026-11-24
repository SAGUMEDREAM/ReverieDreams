package cc.thonly.reverie_dreams.world.settlement;

import cc.thonly.reverie_dreams.data.gensokyo.GensokyoLandmark;
import cc.thonly.reverie_dreams.registry.content.GensokyoLandmarks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Explicit landmark-backed setup for the two built-in Gensokyo settlements.
 */
public final class SettlementLandmarkIntegration {
    public static final Identifier HUMAN_VILLAGE = SettlementType.HUMAN_VILLAGE;
    public static final Identifier TENGU_SETTLEMENT = SettlementType.TENGU_SETTLEMENT;

    public static final Identifier POST_SITE = SettlementType.POST_SITE;
    public static final Identifier SECOND_SITE = SettlementType.SECOND_SITE;

    public static final Identifier PATROL_ROLE = SettlementType.PATROL_ROLE;
    public static final Identifier TRADER_ROLE = SettlementType.TRADER_ROLE;
    public static final Identifier GUARD_ROLE = SettlementType.GUARD_ROLE;

    private static final double LANDMARK_RANGE = 64.0;
    private static final int MAX_SITE_DISTANCE_SQR = SettlementGroundNavigation.MAX_LEG_SQR;
    private static final int MAX_ENROLL_DISTANCE_SQR = 10 * 10;
    private static final int MAX_SITES = 32;
    private static final int MAX_ROADS = 64;

    private SettlementLandmarkIntegration() {
    }

    public static boolean install(ServerLevel level, Identifier landmark, BlockPos post, BlockPos second) {
        SettlementType type = productionType(level, landmark);
        if (type == null || !validStationPair(level, landmark, post, second)) {
            return false;
        }

        SettlementPilotData data = level.getDataStorage().get(SettlementPilotData.TYPE);
        if (data != null && data.layout(landmark) != null) {
            return false;
        }

        SettlementPilotData created = SettlementPilotData.getOrCreate(level);
        return replaceLayout(created, landmark, type.defaultLayout(post, second));
    }

    public static boolean enroll(ServerLevel level, Identifier landmark, Mob resident, @Nullable BlockPos bedHead) {
        return enroll(level, landmark, resident, bedHead, false);
    }

    public static boolean enroll(ServerLevel level, Identifier landmark, Mob resident, @Nullable BlockPos bedHead,
                                 boolean trader) {
        SettlementType type = productionType(level, landmark);
        if (type == null || resident == null || resident.level() != level
                || !resident.isAlive() || !level.isPositionEntityTicking(resident.blockPosition())
                || (resident instanceof TamableAnimal animal && animal.isTame())) {
            return false;
        }

        SettlementPilotData data = level.getDataStorage().get(SettlementPilotData.TYPE);
        SettlementPilotData.Layout layout = data == null ? null : data.layout(landmark);
        if (layout == null || data.member(resident.getUUID()) != null || !nearStation(layout, resident)) {
            return false;
        }

        if (!type.admits(resident, trader)) {
            return false;
        }
        Identifier role = type.role(resident, trader);
        if (role == null)
            return false;

        if (bedHead != null && (!withinLandmark(level, landmark, bedHead)
                || SettlementBed.status(resident, level, bedHead) != SettlementBed.Status.AVAILABLE))
            return false;
        return SettlementPilotData.assign(resident, landmark, role, bedHead);
    }

    public static @Nullable SettlementPilotData.Layout layout(ServerLevel level, Identifier id) {
        if (productionType(level, id) == null) {
            return null;
        }
        SettlementPilotData data = level.getDataStorage().get(SettlementPilotData.TYPE);
        return data == null ? null : data.layout(id);
    }

    public static boolean addSite(ServerLevel level, Identifier landmark, Identifier site, BlockPos position) {
        if (productionType(level, landmark) == null || site == null
                || !withinLandmark(level, landmark, position)
                || !standable(level, position)) {
            return false;
        }

        SettlementPilotData data = level.getDataStorage().get(SettlementPilotData.TYPE);
        SettlementPilotData.Layout layout = data == null ? null : data.layout(landmark);
        if (layout == null || layout.sites().containsKey(site) || layout.sites().containsValue(position)
                || layout.sites().size() >= MAX_SITES) {
            return false;
        }

        Map<Identifier, BlockPos> sites = new LinkedHashMap<>(layout.sites());
        sites.put(site, position.immutable());
        return replaceLayout(data, landmark, new SettlementPilotData.Layout(sites, layout.roles(),
                layout.routines(), layout.roads()));
    }

    public static boolean connect(ServerLevel level, Identifier landmark, Identifier site, Identifier target) {
        if (productionType(level, landmark) == null || site == null || target == null || site.equals(target)) {
            return false;
        }

        SettlementPilotData data = level.getDataStorage().get(SettlementPilotData.TYPE);
        SettlementPilotData.Layout layout = data == null ? null : data.layout(landmark);
        BlockPos from = layout == null ? null : layout.sites().get(site);
        BlockPos to = layout == null ? null : layout.sites().get(target);
        if (layout == null || from == null || to == null || from.distSqr(to) > MAX_SITE_DISTANCE_SQR
                || !standable(level, from) || !standable(level, to)) {
            return false;
        }

        SettlementRoadGraph.Edge edge = new SettlementRoadGraph.Edge(site, target);
        if (layout.roads().size() >= MAX_ROADS || layout.roads().contains(edge)
                || layout.roads().contains(edge.reverse())) {
            return false;
        }

        List<SettlementRoadGraph.Edge> roads = new ArrayList<>(layout.roads());
        roads.add(edge);
        return replaceLayout(data, landmark, new SettlementPilotData.Layout(layout.sites(), layout.roles(),
                layout.routines(), roads));
    }

    private static @Nullable SettlementType productionType(ServerLevel level, Identifier landmark) {
        SettlementType type = SettlementType.byLandmark(landmark);
        return type != null && type.productionContext(level) ? type : null;
    }

    private static boolean validStationPair(ServerLevel level, Identifier landmark, BlockPos post, BlockPos second) {
        return post != null && second != null && !post.equals(second)
                && post.distSqr(second) <= MAX_SITE_DISTANCE_SQR
                && withinLandmark(level, landmark, post) && withinLandmark(level, landmark, second)
                && standable(level, post) && standable(level, second);
    }

    private static boolean withinLandmark(ServerLevel level, Identifier id, BlockPos position) {
        if (position == null) {
            return false;
        }
        GensokyoLandmark landmark = GensokyoLandmarks.get(id).orElse(null);
        return landmark != null
                && landmark.horizontalDistance(position.getX() + 0.5, position.getZ() + 0.5) <= LANDMARK_RANGE;
    }

    private static boolean standable(ServerLevel level, BlockPos position) {
        if (position == null || position.getY() <= level.getMinY()
                || position.above().getY() >= level.getMaxY()
                || !level.getWorldBorder().isWithinBounds(position)) {
            return false;
        }

        LevelChunk chunk = level.getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4);
        if (chunk == null || !level.isPositionEntityTicking(position)) {
            return false;
        }

        return chunk.getBlockState(position).getCollisionShape(chunk, position).isEmpty()
                && chunk.getBlockState(position.above()).getCollisionShape(chunk, position.above()).isEmpty()
                && chunk.getFluidState(position).isEmpty() && chunk.getFluidState(position.above()).isEmpty()
                && chunk.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP);
    }

    private static boolean nearStation(SettlementPilotData.Layout layout, Mob resident) {
        for (BlockPos site : layout.sites().values()) {
            if (resident.position().distanceToSqr(Vec3.atBottomCenterOf(site)) <= MAX_ENROLL_DISTANCE_SQR) {
                return true;
            }
        }
        return false;
    }

    private static boolean replaceLayout(SettlementPilotData data, Identifier landmark,
                                         SettlementPilotData.Layout layout) {
        try {
            data.putLayout(landmark, layout);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

}
