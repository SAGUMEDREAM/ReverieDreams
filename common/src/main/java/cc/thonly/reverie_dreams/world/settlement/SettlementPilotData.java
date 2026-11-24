package cc.thonly.reverie_dreams.world.settlement;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Explicit registrations for the pilot. Each dimension owns its own layouts and membership.
 */
public final class SettlementPilotData extends SavedData {
    private static final String MEMBER_HINT = "reverie_dreams:settlement_pilot";
    private static final int MAX_SITES = 32;
    private static final int MAX_DUTY_STOPS = 8;
    private static final int PATHS_PER_TICK = 2;
    private static final long PATH_REQUEST_TTL = 12;

    public enum Behavior implements StringRepresentable {
        HOLD_POST("hold_post"), INSPECT_ROUTE("inspect_route");

        private static final Codec<Behavior> CODEC = StringRepresentable.fromEnum(Behavior::values);
        private final String id;

        Behavior(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public record Duty(Behavior behavior, List<Identifier> sites) {
        private static final Codec<Duty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Behavior.CODEC.fieldOf("behavior").forGetter(Duty::behavior),
                Identifier.CODEC.listOf().fieldOf("sites").forGetter(Duty::sites)
        ).apply(instance, Duty::new));

        public Duty {
            sites = List.copyOf(sites);
        }

        private boolean isValid(Map<Identifier, BlockPos> positions, List<SettlementRoadGraph.Edge> roads) {
            if (behavior == Behavior.HOLD_POST) {
                return sites.size() == 1 && positions.containsKey(sites.getFirst());
            }
            if (sites.size() < 2 || sites.size() > MAX_DUTY_STOPS) {
                return false;
            }
            for (int i = 0; i < sites.size(); i++) {
                BlockPos from = positions.get(sites.get(i));
                BlockPos to = positions.get(sites.get((i + 1) % sites.size()));
                if (from == null || to == null || from.equals(to)
                        || (from.distSqr(to) > SettlementGroundNavigation.MAX_LEG_SQR
                        && SettlementRoadGraph.route(positions, roads, from, to, pos -> true, Set.of()).isEmpty())) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * One daily work window, measured in ticks of the overworld clock.
     */
    public record Routine(int workStart, int workEnd, @Nullable Identifier restSite) {
        public static final int DAY = 24_000;
        private static final Codec<Integer> TIME = Codec.intRange(0, DAY - 1);
        private static final Codec<Routine> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TIME.fieldOf("work_start").forGetter(Routine::workStart),
                TIME.fieldOf("work_end").forGetter(Routine::workEnd),
                Identifier.CODEC.optionalFieldOf("rest_site").forGetter(routine -> Optional.ofNullable(routine.restSite))
        ).apply(instance, (start, end, rest) -> new Routine(start, end, rest.orElse(null))));

        public Routine {
            if (workStart < 0 || workStart >= DAY || workEnd < 0 || workEnd >= DAY) {
                throw new IllegalArgumentException("Settlement work times must be in [0, 24000)");
            }
        }

        public boolean isWorking(long clock) {
            int time = Math.floorMod(clock, DAY);
            if (workStart <= workEnd) {
                return time >= workStart && time < workEnd;
            }
            return time >= workStart || time < workEnd;
        }
    }

    public record Layout(Map<Identifier, BlockPos> sites, Map<Identifier, Duty> roles,
                         Map<Identifier, Routine> routines, List<SettlementRoadGraph.Edge> roads) {
        private static final Codec<Layout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Identifier.CODEC, BlockPos.CODEC).fieldOf("sites").forGetter(Layout::sites),
                Codec.unboundedMap(Identifier.CODEC, Duty.CODEC).fieldOf("roles").forGetter(Layout::roles),
                Codec.unboundedMap(Identifier.CODEC, Routine.CODEC).optionalFieldOf("routines", Map.of()).forGetter(Layout::routines),
                SettlementRoadGraph.Edge.CODEC.listOf().optionalFieldOf("roads", List.of()).forGetter(Layout::roads)
        ).apply(instance, Layout::new));

        public Layout(Map<Identifier, BlockPos> sites, Map<Identifier, Duty> roles) {
            this(sites, roles, Map.of(), List.of());
        }

        public Layout(Map<Identifier, BlockPos> sites, Map<Identifier, Duty> roles, Map<Identifier, Routine> routines) {
            this(sites, roles, routines, List.of());
        }

        public Layout {
            Map<Identifier, BlockPos> immutablePositions = new HashMap<>();
            sites.forEach((id, pos) -> immutablePositions.put(id, pos.immutable()));
            sites = Map.copyOf(immutablePositions);
            roles = Map.copyOf(roles);
            routines = Map.copyOf(routines);
            roads = List.copyOf(roads);
        }

        boolean isRoadSite(BlockPos position) {
            return this.roads.stream().anyMatch(edge -> position.equals(this.sites.get(edge.a()))
                    || position.equals(this.sites.get(edge.b())));
        }

        private boolean isValid() {
            return !sites.isEmpty() && sites.size() <= MAX_SITES && !roles.isEmpty()
                    && SettlementRoadGraph.isValid(sites, roads)
                    && roles.values().stream().allMatch(duty -> duty.isValid(sites, roads))
                    && roles.keySet().containsAll(routines.keySet())
                    && routines.values().stream().allMatch(routine -> routine.restSite == null || sites.containsKey(routine.restSite));
        }
    }

    public record Member(Identifier settlement, Identifier role, @Nullable BlockPos bedHead) {
        private static final Codec<Member> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("settlement").forGetter(Member::settlement),
                Identifier.CODEC.fieldOf("role").forGetter(Member::role),
                BlockPos.CODEC.optionalFieldOf("bed_head").forGetter(member -> Optional.ofNullable(member.bedHead))
        ).apply(instance, (settlement, role, bed) -> new Member(settlement, role, bed.orElse(null))));

        public Member(Identifier settlement, Identifier role) {
            this(settlement, role, null);
        }

        public Member {
            if (bedHead != null) {
                bedHead = bedHead.immutable();
            }
        }
    }

    public static final Codec<SettlementPilotData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1).optionalFieldOf("version", 1).forGetter(data -> 1),
            Codec.unboundedMap(Identifier.CODEC, Layout.CODEC).fieldOf("settlements").forGetter(data -> data.layouts),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Member.CODEC).fieldOf("members").forGetter(data -> data.members),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, SettlementHaul.CODEC).optionalFieldOf("hauls", Map.of())
                    .forGetter(data -> data.hauls),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, SettlementTrade.CODEC).optionalFieldOf("trades", Map.of())
                    .forGetter(data -> data.trades),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, SettlementTask.CODEC).optionalFieldOf("tasks", Map.of())
                    .forGetter(data -> data.tasks)
    ).apply(instance, SettlementPilotData::new));

    public static final SavedDataType<SettlementPilotData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("reverie_dreams", "settlement_pilot"),
            SettlementPilotData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<Identifier, Layout> layouts = new HashMap<>();
    private final Map<UUID, Member> members = new HashMap<>();
    private final Map<UUID, SettlementHaul> hauls = new HashMap<>();
    private final Map<UUID, SettlementTrade> trades = new HashMap<>();
    private final Map<UUID, SettlementTask> tasks = new HashMap<>();
    private long budgetTick = Long.MIN_VALUE;
    private int pathsThisTick;
    private final Map<UUID, Long> waitingPaths = new LinkedHashMap<>();

    private SettlementPilotData() {
    }

    private SettlementPilotData(int version, Map<Identifier, Layout> layouts, Map<UUID, Member> members,
                                Map<UUID, SettlementHaul> hauls, Map<UUID, SettlementTrade> trades,
                                Map<UUID, SettlementTask> tasks) {
        layouts.forEach((id, layout) -> {
            if (layout.isValid()) {
                this.layouts.put(id, layout);
            }
        });
        members.forEach((id, member) -> {
            if (this.resolveDuty(member) != null) {
                this.members.put(id, member);
            }
        });
        // A loaded shipment can outlive its resident or layout until an explicit recovery returns it.
        this.hauls.putAll(hauls);
        trades.forEach((id, trade) -> {
            Member member = this.members.get(id);
            if (trade.isValid() && trade.matches(member, this.layoutFor(member)))
                this.trades.put(id, trade);
        });
        tasks.forEach((id, task) -> {
            Member member = this.members.get(id);
            if (task.isValid(member, this.layoutFor(member)))
                this.tasks.put(id, task);
        });
    }

    public static SettlementPilotData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Only explicit callers supply layouts. There is no landmark or spawn hook.
     */
    public void putLayout(Identifier id, Layout layout) {
        if (!layout.isValid()) {
            throw new IllegalArgumentException("Invalid settlement pilot layout: " + id);
        }
        if (!layout.equals(this.layouts.get(id))) {
            this.layouts.put(id, layout);
            this.trades.entrySet().removeIf(entry -> entry.getValue().settlement().equals(id)
                    && !entry.getValue().matches(this.members.get(entry.getKey()), layout));
            this.tasks.entrySet().removeIf(entry -> entry.getValue().settlement().equals(id)
                    && !entry.getValue().isValid(this.members.get(entry.getKey()), layout));
            this.setDirty();
        }
    }

    public void removeLayout(Identifier id) {
        if (this.layouts.remove(id) != null) {
            this.members.values().removeIf(member -> member.settlement().equals(id));
            this.hauls.values().removeIf(haul -> haul.settlement().equals(id) && !haul.carrying());
            this.trades.values().removeIf(trade -> trade.settlement().equals(id));
            this.tasks.values().removeIf(task -> task.settlement().equals(id));
            this.setDirty();
        }
    }

    public static boolean assign(Mob mob, Identifier settlement, Identifier role) {
        return assign(mob, settlement, role, null);
    }

    public static boolean assign(Mob mob, Identifier settlement, Identifier role, @Nullable BlockPos bedHead) {
        if (!(mob.level() instanceof ServerLevel level)
                || !(mob.getNavigation() instanceof SettlementGroundNavigation)
                || !mob.isAlive() || (mob instanceof TamableAnimal animal && animal.isTame())) {
            return false;
        }
        SettlementPilotData data = level.getDataStorage().get(TYPE);
        Member member = new Member(settlement, role, bedHead);
        if (data == null || data.resolveDuty(member) == null
                || (!mob.entityTags().contains(MEMBER_HINT) && !mob.addTag(MEMBER_HINT))) {
            return false;
        }
        if (!member.equals(data.members.put(mob.getUUID(), member))) {
            SettlementTrade trade = data.trade(mob.getUUID());
            if (trade != null && !trade.matches(member, data.layoutFor(member)))
                data.trades.remove(mob.getUUID());
            SettlementHaul haul = data.haul(mob.getUUID());
            if (haul != null && !haul.carrying() && !haul.belongsTo(member)) {
                data.hauls.remove(mob.getUUID());
            }
            SettlementTask task = data.task(mob.getUUID());
            if (task != null && !task.isValid(member, data.layoutFor(member)))
                data.tasks.remove(mob.getUUID());
            data.setDirty();
        }
        return true;
    }

    public static void unassign(Mob mob) {
        SettlementPilotData data = existingFor(mob);
        Member removed = data == null ? null : data.members.remove(mob.getUUID());
        if (removed != null) {
            data.trades.remove(mob.getUUID());
            data.tasks.remove(mob.getUUID());
            if (mob instanceof HumanEntity human)
                human.settlementTradeSession().close();
            SettlementHaul haul = data.haul(mob.getUUID());
            if (haul != null && !haul.carrying()) {
                data.hauls.remove(mob.getUUID());
            }
            data.setDirty();
            if (mob.level() instanceof ServerLevel level && mob.getSleepingPos().filter(pos -> pos.equals(removed.bedHead)).isPresent()) {
                SettlementBed.wake(mob, level);
            }
        }
        mob.removeTag(MEMBER_HINT);
    }

    public @Nullable Member member(UUID resident) {
        return this.members.get(resident);
    }

    public @Nullable Layout layout(Identifier id) {
        return this.layouts.get(id);
    }

    /**
     * One pending explicit visit per resident. It runs during work time after any cargo haul.
     */
    public static boolean queueTask(Mob mob, Identifier site) {
        if (!(mob.level() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || !mob.isAlive() || (mob instanceof TamableAnimal animal && animal.isTame()))
            return false;
        SettlementPilotData data = existingFor(mob);
        if (data == null || data.tasks.containsKey(mob.getUUID()))
            return false;
        Member member = data.member(mob.getUUID());
        Layout layout = data.layoutFor(member);
        if (layout == null || data.resolveDuty(member) == null || site == null)
            return false;
        BlockPos position = layout.sites().get(site);
        if (position == null)
            return false;
        data.tasks.put(mob.getUUID(), new SettlementTask(member.settlement(), member.role(),
                SettlementTask.Type.VISIT_SITE, site, position));
        data.setDirty();
        return true;
    }

    public static boolean cancelTask(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || !level.getServer().isSameThread())
            return false;
        SettlementPilotData data = existingFor(mob);
        if (data == null || data.tasks.remove(mob.getUUID()) == null)
            return false;
        data.setDirty();
        return true;
    }

    public @Nullable SettlementTask task(UUID resident) {
        return this.tasks.get(resident);
    }

    void completeTask(UUID resident, SettlementTask expected) {
        if (this.tasks.remove(resident, expected))
            this.setDirty();
    }

    /**
     * Pilot-only explicit registration. The offer stays tied to the resident's current posting.
     */
    public static boolean registerTrade(Mob resident, Identifier site, ItemStack payment, ItemStack result, int stock) {
        return registerTrade(resident, site, payment, result, stock, Optional.empty());
    }

    public static boolean registerStockTrade(Mob resident, Identifier site, ItemStack payment, ItemStack result,
                                             BlockPos goodsChest, BlockPos tillChest) {
        return registerTrade(resident, site, payment, result, 1,
                Optional.of(new SettlementTrade.Stockroom(goodsChest, tillChest)));
    }

    private static boolean registerTrade(Mob resident, Identifier site, ItemStack payment, ItemStack result, int stock,
                                         Optional<SettlementTrade.Stockroom> stockroom) {
        if (!(resident instanceof HumanEntity human) || !(resident.level() instanceof ServerLevel level)
                || !level.getServer().isSameThread() || !human.isAlive() || human.isTame())
            return false;
        var data = existingFor(resident);
        if (data == null || data.trades.containsKey(resident.getUUID()))
            return false;
        var member = data.member(resident.getUUID());
        var layout = data.layoutFor(member);
        var duty = data.resolveDuty(member);
        if (layout == null || duty == null || duty.behavior() != Behavior.HOLD_POST
                || !duty.sites().getFirst().equals(site))
            return false;
        var trade = new SettlementTrade(member.settlement(), member.role(), site, layout.sites().get(site),
                payment, result, stock, 0, stockroom);
        if (!trade.isValid())
            return false;
        data.trades.put(resident.getUUID(), trade);
        data.setDirty();
        return true;
    }

    public @Nullable SettlementTrade trade(UUID resident) {
        return this.trades.get(resident);
    }

    void completeTrade(UUID resident, SettlementTrade expected, int count) {
        if (this.trade(resident) != expected || count <= 0 || count > expected.stock() - expected.uses()) {
            throw new IllegalStateException("Settlement trade no longer matches the active offer");
        }
        this.trades.put(resident, expected.withUses(expected.uses() + count));
        this.setDirty();
    }

    /**
     * Queues one explicit shipment. Both standing positions must be registered in the same pilot.
     */
    public static boolean queueHaul(Mob mob, Identifier sourceSite, BlockPos sourceChest, Identifier destinationSite,
                                    BlockPos destinationChest, Item item, int count) {
        if (!(mob instanceof HumanEntity human) || !(mob.level() instanceof ServerLevel level)
                || !level.getServer().isSameThread() || !human.isAlive() || human.isTame()) {
            return false;
        }
        SettlementPilotData data = existingFor(mob);
        if (data == null || data.hauls.containsKey(mob.getUUID())) {
            return false;
        }
        Member member = data.member(mob.getUUID());
        Layout layout = data.layoutFor(member);
        if (layout == null || data.resolveDuty(member) == null) {
            return false;
        }
        BlockPos source = layout.sites().get(sourceSite);
        BlockPos destination = layout.sites().get(destinationSite);
        if (source == null || destination == null) {
            return false;
        }
        var haul = new SettlementHaul(member.settlement(), member.role(),
                new SettlementHaul.Stop(sourceSite, source, sourceChest),
                new SettlementHaul.Stop(destinationSite, destination, destinationChest), item, count, ItemStack.EMPTY);
        if (!haul.isValid() || (source.distSqr(destination) > SettlementGroundNavigation.MAX_LEG_SQR
                && SettlementRoadGraph.route(layout.sites(), layout.roads(), source, destination,
                pos -> true, Set.of()).isEmpty())) {
            return false;
        }
        data.hauls.put(mob.getUUID(), haul);
        data.setDirty();
        return true;
    }

    public @Nullable SettlementHaul haul(UUID resident) {
        return this.hauls.get(resident);
    }

    /**
     * Pilot administration. Return the complete batch to the original chest before clearing the order.
     */
    public static boolean cancelHaul(ServerLevel level, UUID resident) {
        if (!level.getServer().isSameThread()) {
            return false;
        }
        SettlementPilotData data = level.getDataStorage().get(TYPE);
        SettlementHaul haul = data == null ? null : data.haul(resident);
        if (haul == null || !haul.isValid() || (haul.carrying()
                && !SettlementChestInventory.putBatch(level, haul.source().chest(), haul.cargo()))) {
            return false;
        }
        data.hauls.remove(resident);
        data.setDirty();
        return true;
    }

    void collectHaul(UUID resident, SettlementHaul order, ItemStack batch) {
        this.hauls.put(resident, order.withCargo(batch));
        this.setDirty();
    }

    void completeHaul(UUID resident) {
        this.hauls.remove(resident);
        this.setDirty();
    }

    public static boolean hasAssignment(Mob mob) {
        SettlementPilotData data = existingFor(mob);
        return data != null && data.resolveDuty(data.member(mob.getUUID())) != null;
    }

    static @Nullable SettlementPilotData existingFor(Mob mob) {
        // The saved entity tag only avoids storage lookup for ordinary mobs. It grants no role.
        return mob.level() instanceof ServerLevel level && mob.entityTags().contains(MEMBER_HINT)
                ? level.getDataStorage().get(TYPE) : null;
    }

    @Nullable Duty resolveDuty(@Nullable Member member) {
        Layout layout = this.layoutFor(member);
        return layout == null ? null : layout.roles().get(member.role());
    }

    @Nullable Layout layoutFor(@Nullable Member member) {
        return member == null ? null : this.layouts.get(member.settlement());
    }

    boolean takePathBudget(long tick, UUID resident) {
        if (tick != this.budgetTick) {
            this.budgetTick = tick;
            this.pathsThisTick = 0;
            this.waitingPaths.values().removeIf(lastRequest -> tick - lastRequest > PATH_REQUEST_TTL);
        }
        this.waitingPaths.put(resident, tick);
        int remaining = PATHS_PER_TICK - this.pathsThisTick;
        // Earlier waiters reserve the available slots even when their goals tick later in the tick.
        var waitingResidents = this.waitingPaths.keySet().iterator();
        while (waitingResidents.hasNext()) {
            if (remaining-- <= 0)
                break;
            if (waitingResidents.next().equals(resident)) {
                waitingResidents.remove();
                this.pathsThisTick++;
                return true;
            }
        }
        return false;
    }

    void withdrawPathRequest(UUID resident) {
        this.waitingPaths.remove(resident);
    }
}
