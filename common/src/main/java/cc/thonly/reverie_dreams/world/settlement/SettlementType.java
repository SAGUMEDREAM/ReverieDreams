package cc.thonly.reverie_dreams.world.settlement;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.entity.WhiteWolfTengu;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Built-in settlement behavior selected by landmark identifier.
 */
public interface SettlementType {
    Identifier HUMAN_VILLAGE = Identifier.fromNamespaceAndPath("reverie_dreams", "human_village");
    Identifier TENGU_SETTLEMENT = Identifier.fromNamespaceAndPath("reverie_dreams", "tengu_settlement");

    Identifier POST_SITE = Identifier.fromNamespaceAndPath("reverie_dreams", "post");
    Identifier SECOND_SITE = Identifier.fromNamespaceAndPath("reverie_dreams", "second");

    Identifier PATROL_ROLE = Identifier.fromNamespaceAndPath("reverie_dreams", "patrol");
    Identifier TRADER_ROLE = Identifier.fromNamespaceAndPath("reverie_dreams", "trader");
    Identifier GUARD_ROLE = Identifier.fromNamespaceAndPath("reverie_dreams", "guard");

    SettlementType HUMAN_VILLAGE_TYPE = new HumanVillage();
    SettlementType TENGU_SETTLEMENT_TYPE = new TenguSettlement();

    Map<Identifier, SettlementType> TYPES = Map.of(
            HUMAN_VILLAGE, HUMAN_VILLAGE_TYPE,
            TENGU_SETTLEMENT, TENGU_SETTLEMENT_TYPE
    );

    static @Nullable SettlementType byLandmark(@Nullable Identifier landmark) {
        return landmark == null ? null : TYPES.get(landmark);
    }

    Identifier landmark();

    boolean productionContext(@Nullable ServerLevel level);

    boolean admits(Mob resident, boolean trader);

    @Nullable Identifier role(Mob resident, boolean trader);

    SettlementPilotData.Layout defaultLayout(BlockPos post, BlockPos second);

    private static boolean gensokyoProductionContext(@Nullable ServerLevel level) {
        return level != null && level.getServer().isSameThread()
                && level.dimension().equals(RDBuiltinLevels.GENSOKYO);
    }

    record HumanVillage() implements SettlementType {
        @Override
        public Identifier landmark() {
            return HUMAN_VILLAGE;
        }

        @Override
        public boolean productionContext(@Nullable ServerLevel level) {
            return SettlementType.gensokyoProductionContext(level);
        }

        @Override
        public boolean admits(Mob resident, boolean trader) {
            return resident instanceof HumanEntity;
        }

        @Override
        public Identifier role(Mob resident, boolean trader) {
            return trader ? TRADER_ROLE : PATROL_ROLE;
        }

        @Override
        public SettlementPilotData.Layout defaultLayout(BlockPos post, BlockPos second) {
            Map<Identifier, BlockPos> sites = Map.of(POST_SITE, post.immutable(),
                    SECOND_SITE, second.immutable());
            List<SettlementRoadGraph.Edge> roads = List.of(new SettlementRoadGraph.Edge(POST_SITE, SECOND_SITE));
            Map<Identifier, SettlementPilotData.Duty> roles = Map.of(
                    PATROL_ROLE, new SettlementPilotData.Duty(SettlementPilotData.Behavior.INSPECT_ROUTE,
                            List.of(POST_SITE, SECOND_SITE)),
                    TRADER_ROLE, new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST,
                            List.of(POST_SITE)));
            Map<Identifier, SettlementPilotData.Routine> routines = Map.of(
                    PATROL_ROLE, new SettlementPilotData.Routine(0, 12_000, null),
                    TRADER_ROLE, new SettlementPilotData.Routine(0, 12_000, SECOND_SITE));
            return new SettlementPilotData.Layout(sites, roles, routines, roads);
        }
    }

    record TenguSettlement() implements SettlementType {
        @Override
        public Identifier landmark() {
            return TENGU_SETTLEMENT;
        }

        @Override
        public boolean productionContext(@Nullable ServerLevel level) {
            return SettlementType.gensokyoProductionContext(level);
        }

        @Override
        public boolean admits(Mob resident, boolean trader) {
            return !trader && resident instanceof WhiteWolfTengu tengu && !tengu.isNaturallySpawned();
        }

        @Override
        public Identifier role(Mob resident, boolean trader) {
            return GUARD_ROLE;
        }

        @Override
        public SettlementPilotData.Layout defaultLayout(BlockPos post, BlockPos second) {
            Map<Identifier, BlockPos> sites = Map.of(POST_SITE, post.immutable(),
                    SECOND_SITE, second.immutable());
            List<SettlementRoadGraph.Edge> roads = List.of(new SettlementRoadGraph.Edge(POST_SITE, SECOND_SITE));
            Map<Identifier, SettlementPilotData.Duty> roles = Map.of(GUARD_ROLE,
                    new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST, List.of(POST_SITE)));
            Map<Identifier, SettlementPilotData.Routine> routines = Map.of(GUARD_ROLE,
                    new SettlementPilotData.Routine(13_000, 23_000, SECOND_SITE));
            return new SettlementPilotData.Layout(sites, roles, routines, roads);
        }
    }
}
