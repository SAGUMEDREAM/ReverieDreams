package cc.thonly.reverie_dreams.world.settlement;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One low-priority MOVE owner per enrolled mob; all duty execution happens in entity ticks.
 */
public final class SettlementDutyGoal extends Goal {
    public static final int PRIORITY = 7;

    private final Mob mob;
    private SettlementPilotData data;
    private SettlementPilotData.Member member;
    private SettlementPilotData.Layout layout;
    private SettlementPilotData.Duty duty;
    private int cursor;
    private long nextAttempt;
    private boolean resting;
    private BlockPos restTarget;
    private boolean approachingBed;
    private long nextBedAttempt;
    private SettlementHaul activeHaul;
    private SettlementTask activeTask;
    private BlockPos roadDestination;
    private List<Identifier> roadRoute = List.of();
    private Identifier roadPrevious;
    private int roadIndex;
    private boolean roadLegStarted;
    private final Map<SettlementRoadGraph.Edge, Long> blockedRoads = new HashMap<>();
    private final Map<BlockPos, Long> blockedEntrances = new HashMap<>();

    public SettlementDutyGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(this.mob.level() instanceof ServerLevel level)
                || !(this.mob.getNavigation() instanceof SettlementGroundNavigation navigation)
                || isInterrupted(this.mob)) {
            return false;
        }
        SettlementPilotData currentData = SettlementPilotData.existingFor(this.mob);
        SettlementPilotData.Member currentMember = currentData == null ? null : currentData.member(this.mob.getUUID());
        SettlementPilotData.Layout currentLayout = currentData == null ? null : currentData.layoutFor(currentMember);
        if (currentData != this.data || !Objects.equals(currentMember, this.member) || currentLayout != this.layout) {
            this.resetRoad(navigation);
            this.blockedRoads.clear();
            this.blockedEntrances.clear();
            this.data = currentData;
            this.member = currentMember;
            this.layout = currentLayout;
            this.duty = currentLayout == null ? null : currentLayout.roles().get(currentMember.role());
            this.cursor = 0;
            this.nextAttempt = level.getGameTime();
            this.restTarget = null;
            this.approachingBed = false;
            this.nextBedAttempt = 0;
            this.activeHaul = null;
            this.activeTask = null;
        }
        return this.duty != null && level.isPositionEntityTicking(this.mob.blockPosition());
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        // A lower-priority idle goal may have left a destination in the vanilla navigator.
        ((SettlementGroundNavigation) this.mob.getNavigation()).beginPilotControl();
    }

    @Override
    public void stop() {
        if (this.mob.getNavigation() instanceof SettlementGroundNavigation navigation) {
            this.resetRoad(navigation);
            navigation.endPilotControl();
        }
        // Keep the in-memory cursor for interruptions. Role changes and entity reloads reset it.
        this.nextAttempt = this.mob.level().getGameTime() + 20;
    }

    @Override
    public void tick() {
        if (!this.canUse() || !(this.mob.level() instanceof ServerLevel level)
                || !(this.mob.getNavigation() instanceof SettlementGroundNavigation navigation)) {
            return;
        }
        long now = level.getGameTime();
        if (this.mob instanceof HumanEntity human && human.settlementTradeSession().isActive()) {
            this.resetRoad(navigation);
            return;
        }
        SettlementPilotData.Routine routine = this.layout.routines().get(this.member.role());
        boolean shouldRest = routine != null && !routine.isWorking(level.getOverworldClockTime());
        if (this.resting != shouldRest) {
            this.resetRoad(navigation);
            this.resting = shouldRest;
            this.restTarget = null;
            this.approachingBed = false;
            this.nextAttempt = now;
            this.nextBedAttempt = now;
        }
        if (shouldRest) {
            this.tickRest(level, navigation, routine, now);
            return;
        }

        SettlementHaul haul = this.data.haul(this.mob.getUUID());
        if (haul != null && (!haul.belongsTo(this.member) || !haul.matches(this.layout) || !haul.isValid())) {
            haul = null;
        }
        if (this.activeHaul != haul) {
            this.resetRoad(navigation);
            this.activeHaul = haul;
            this.nextAttempt = Math.max(this.nextAttempt, now);
        }
        if (haul != null) {
            this.tickHaul(level, navigation, haul, now);
            return;
        }

        SettlementTask task = this.data.task(this.mob.getUUID());
        if (task != null && !task.isValid(this.member, this.layout)) {
            this.data.completeTask(this.mob.getUUID(), task);
            task = null;
        }
        if (this.activeTask != task) {
            this.resetRoad(navigation);
            this.activeTask = task;
            this.nextAttempt = now;
        }
        if (task != null) {
            if (this.mob.isOnFire() || this.mob.hurtTime > 0 || hasNearbyFuse(level, this.mob)) {
                this.resetRoad(navigation);
                return;
            }
            if (this.isAtSite(level, navigation, task.position())) {
                this.resetRoad(navigation);
                this.data.completeTask(this.mob.getUUID(), task);
                this.activeTask = null;
            } else {
                this.moveToSite(level, navigation, task.position(), now);
            }
            return;
        }

        BlockPos destination = this.layout.sites().get(this.duty.sites().get(this.cursor));
        if (this.isAtSite(level, navigation, destination)) {
            this.resetRoad(navigation);
            if (this.duty.behavior() == SettlementPilotData.Behavior.INSPECT_ROUTE && now >= this.nextAttempt) {
                this.cursor = (this.cursor + 1) % this.duty.sites().size();
                this.nextAttempt = now + 12;
            }
            return;
        }
        this.moveToSite(level, navigation, destination, now);
    }

    private void tickHaul(ServerLevel level, SettlementGroundNavigation navigation, SettlementHaul haul, long now) {
        if (this.mob.isOnFire() || this.mob.hurtTime > 0) {
            this.resetRoad(navigation);
            return;
        }
        SettlementHaul.Stop stop = haul.carrying() ? haul.destination() : haul.source();
        if (!level.isPositionEntityTicking(stop.approach()) || !level.isPositionEntityTicking(stop.chest())) {
            this.resetRoad(navigation);
            return;
        }
        if (now < this.nextAttempt) {
            return;
        }
        if (hasNearbyFuse(level, this.mob) || !SettlementChestInventory.isAvailable(level, stop.chest())) {
            this.resetRoad(navigation);
            this.nextAttempt = now + 40;
            return;
        }
        if (!this.isAtHaulStop(navigation, stop.approach())) {
            this.moveToSite(level, navigation, stop.approach(), now);
            return;
        }
        this.resetRoad(navigation);
        this.nextAttempt = now + 40;
        if (haul.carrying()) {
            if (SettlementChestInventory.putBatch(level, stop.chest(), haul.cargo())) {
                this.data.completeHaul(this.mob.getUUID());
                this.nextAttempt = now;
            }
        } else {
            var batch = SettlementChestInventory.takeBatch(level, stop.chest(), haul.item(), haul.count());
            if (!batch.isEmpty()) {
                this.data.collectHaul(this.mob.getUUID(), haul, batch);
                this.nextAttempt = now;
            }
        }
    }

    private boolean isAtHaulStop(SettlementGroundNavigation navigation, BlockPos approach) {
        Vec3 center = Vec3.atBottomCenterOf(approach);
        Vec3 position = this.mob.position();
        // Vanilla completes ground waypoints with separate X and Z tolerances, not a radius.
        double tolerance = Math.max(0.65, navigation.getMaxDistanceToWaypoint());
        return Math.abs(position.x - center.x) <= tolerance && Math.abs(position.z - center.z) <= tolerance
                && Math.abs(position.y - center.y) < 0.5;
    }

    private void tickRest(ServerLevel level, SettlementGroundNavigation navigation, SettlementPilotData.Routine routine, long now) {
        BlockPos bed = this.member.bedHead();
        // A failed approach gives the resident time at the fallback site before trying the bed again.
        boolean keepTryingBed = this.approachingBed || now >= this.nextBedAttempt;
        boolean bedAvailable = bed != null && !this.mob.isOnFire() && this.mob.hurtTime == 0 && keepTryingBed
                && SettlementBed.status(this.mob, level, bed) == SettlementBed.Status.AVAILABLE;
        if (bedAvailable && !hasNearbyFuse(level, this.mob)) {
            List<BlockPos> approaches = SettlementBed.approaches(level, bed);
            if (!approaches.isEmpty()) {
                this.chooseRestTarget(navigation, bed, true, now);
                if (approaches.stream().anyMatch(pos -> this.isAt(level, pos))
                        && SettlementBed.trySleep(this.mob, level, bed)) {
                    navigation.stopPilotPath();
                    return;
                }
                if (this.tryMove(level, navigation, approaches, now)) {
                    return;
                }
            }
        }

        if (keepTryingBed) {
            this.nextBedAttempt = now + 40;
        }
        BlockPos fallback = routine.restSite() == null ? null : this.layout.sites().get(routine.restSite());
        this.chooseRestTarget(navigation, fallback, false, now);
        if (fallback != null && !this.isAtSite(level, navigation, fallback)) {
            this.moveToSite(level, navigation, fallback, now);
        } else if (this.roadDestination != null) {
            this.resetRoad(navigation);
        }
    }

    private void chooseRestTarget(SettlementGroundNavigation navigation, BlockPos target, boolean bed, long now) {
        if (this.approachingBed != bed || !Objects.equals(this.restTarget, target)) {
            this.resetRoad(navigation);
            this.approachingBed = bed;
            this.restTarget = target;
            this.nextAttempt = now;
        }
    }

    private boolean isAt(ServerLevel level, BlockPos destination) {
        return level.isPositionEntityTicking(destination)
                && this.mob.position().distanceToSqr(Vec3.atBottomCenterOf(destination)) <= 0.65 * 0.65;
    }

    private boolean isAtSite(ServerLevel level, SettlementGroundNavigation navigation, BlockPos destination) {
        return this.layout.roads().isEmpty() ? this.isAt(level, destination)
                : level.isPositionEntityTicking(destination) && this.isAtHaulStop(navigation, destination);
    }

    private void resetRoad(SettlementGroundNavigation navigation) {
        navigation.stopPilotPath();
        if (this.data != null)
            this.data.withdrawPathRequest(this.mob.getUUID());
        this.roadDestination = null;
        this.roadRoute = List.of();
        this.roadPrevious = null;
        this.roadIndex = 0;
        this.roadLegStarted = false;
    }

    private void moveToSite(ServerLevel level, SettlementGroundNavigation navigation, BlockPos destination, long now) {
        if (!Objects.equals(this.roadDestination, destination)) {
            this.resetRoad(navigation);
            this.roadDestination = destination;
        }
        if (this.roadRoute.isEmpty() && now >= this.nextAttempt && navigation.isDone()
                && !this.layout.isRoadSite(destination)) {
            this.tryMove(level, navigation, List.of(destination), now);
            return;
        }
        if (!navigation.isDone() || now < this.nextAttempt)
            return;
        if (this.roadRoute.isEmpty()) {
            this.blockedRoads.values().removeIf(expiry -> expiry <= now);
            this.blockedEntrances.values().removeIf(expiry -> expiry <= now);
            BlockPos from = this.mob.blockPosition();
            this.roadPrevious = null;
            double nearest = Double.POSITIVE_INFINITY;
            for (var entry : this.layout.sites().entrySet()) {
                double distance = this.mob.position().distanceToSqr(Vec3.atBottomCenterOf(entry.getValue()));
                if ((distance < nearest || (distance == nearest && this.roadPrevious != null
                        && entry.getKey().compareTo(this.roadPrevious) < 0))
                        && this.isAtSite(level, navigation, entry.getValue())
                        && this.layout.isRoadSite(entry.getValue())) {
                    nearest = distance;
                    from = entry.getValue();
                    this.roadPrevious = entry.getKey();
                }
            }
            this.roadRoute = SettlementRoadGraph.route(this.layout.sites(), this.layout.roads(), from, destination,
                    pos -> level.isPositionEntityTicking(pos) && !this.blockedEntrances.containsKey(pos),
                    this.blockedRoads.keySet());
            if (this.roadRoute.isEmpty()) {
                this.nextAttempt = now + 30;
                return;
            }
        }
        Identifier next = this.roadRoute.get(this.roadIndex);
        BlockPos waypoint = this.layout.sites().get(next);
        if (this.isAtSite(level, navigation, waypoint)) {
            navigation.stopPilotPath();
            this.roadPrevious = next;
            this.roadLegStarted = false;
            if (++this.roadIndex == this.roadRoute.size())
                this.resetRoad(navigation);
            return;
        }
        if (this.roadLegStarted) {
            this.failRoadLeg(navigation, next, now);
            return;
        }
        if (!this.tryMove(level, navigation, List.of(waypoint), now)) {
            this.failRoadLeg(navigation, next, now);
        } else if (!navigation.isDone()) {
            this.roadLegStarted = true;
        }
    }

    private void failRoadLeg(SettlementGroundNavigation navigation, Identifier next, long now) {
        if (this.roadPrevious != null && !this.roadPrevious.equals(next)) {
            this.blockedRoads.put(new SettlementRoadGraph.Edge(this.roadPrevious, next), now + 100);
        } else if (this.roadPrevious == null) {
            this.blockedEntrances.put(this.layout.sites().get(next), now + 40);
        }
        this.resetRoad(navigation);
        this.nextAttempt = Math.max(this.nextAttempt, now + 12);
    }

    /**
     * False only when the latest path attempt could not reach any destination.
     */
    private boolean tryMove(ServerLevel level, SettlementGroundNavigation navigation,
                            List<BlockPos> destinations, long now) {
        if (!navigation.isDone() || now < this.nextAttempt) {
            return true;
        }
        int stagger = Math.floorMod(this.mob.getUUID().hashCode(), 11);
        var result = navigation.startPilotPath(level, destinations, this.data);
        this.nextAttempt = now + switch (result) {
            case STARTED -> 20;
            case BUDGET_EXHAUSTED -> 2 + stagger % 3;
            case UNAVAILABLE -> 30 + stagger;
        };
        return result != SettlementGroundNavigation.StartResult.UNAVAILABLE;
    }

    /**
     * Sleeping humanoids skip their goal selector, so their wake decision runs before their entity tick.
     */
    public static void tickSleepingResident(Mob mob) {
        if (!mob.isSleeping() || !(mob.level() instanceof ServerLevel level)
                || !level.isPositionEntityTicking(mob.blockPosition())) {
            return;
        }
        var data = SettlementPilotData.existingFor(mob);
        if (data == null) {
            return;
        }
        var member = data.member(mob.getUUID());
        var layout = data.layoutFor(member);
        var routine = layout == null ? null : layout.routines().get(member.role());
        var bed = mob.getSleepingPos().orElse(null);
        if (routine == null || routine.isWorking(level.getOverworldClockTime())
                || !Objects.equals(member.bedHead(), bed) || isDisrupted(mob)
                || mob.isOnFire() || mob.hurtTime > 0 || hasNearbyFuse(level, mob)
                || bed == null || SettlementBed.status(mob, level, bed) != SettlementBed.Status.AVAILABLE) {
            SettlementBed.wake(mob, level);
        }
    }

    static boolean hasNearbyFuse(ServerLevel level, Mob mob) {
        return !level.getEntitiesOfClass(Creeper.class, mob.getBoundingBox().inflate(6),
                creeper -> creeper.isAlive() && (creeper.isIgnited() || creeper.getSwellDir() > 0)).isEmpty();
    }

    static boolean isInterrupted(Mob mob) {
        return mob.isSleeping() || isDisrupted(mob);
    }

    private static boolean isDisrupted(Mob mob) {
        return !mob.isAlive() || mob.isPassenger() || mob.isLeashed()
                || (mob instanceof TamableAnimal animal && (animal.isTame() || animal.isOrderedToSit()))
                || (mob.getTarget() != null && mob.getTarget().isAlive());
    }
}
