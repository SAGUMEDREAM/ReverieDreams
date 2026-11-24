package cc.thonly.reverie_dreams.restaurant;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.block.entity.CupboardBlockEntity;
import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.block.entity.WorkshopStorageBlockEntity;
import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;
import cc.thonly.reverie_dreams.entity.ai.work.WorkWorld;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One restaurant snapshot shared by chefs and waiters; holds positions and UUIDs only. */
public record RestaurantScene(BlockPos bellPos, UUID bellId, WorkArea area, WorkArea scan,
                              List<BlockPos> plates, List<BlockPos> kitchenware,
                              List<BlockPos> counters, List<BlockPos> cupboards,
                              List<BlockPos> silverChests, List<BlockPos> barrels, List<BlockPos> tables,
                              List<BlockPos> chairs, List<UUID> customers) {
    public static final int SCAN_RADIUS = 16;
    public static final int SCAN_HEIGHT = 8;
    public static final double PLATE_COUNTER_DISTANCE_SQR = 16.0;

    public RestaurantScene {
        bellPos = bellPos.immutable();
        plates = List.copyOf(plates);
        kitchenware = List.copyOf(kitchenware);
        counters = List.copyOf(counters);
        cupboards = List.copyOf(cupboards);
        silverChests = List.copyOf(silverChests);
        barrels = List.copyOf(barrels);
        tables = List.copyOf(tables);
        chairs = List.copyOf(chairs);
        customers = List.copyOf(customers);
    }

    public static RestaurantScene scan(ServerLevel level, RestaurantBlockEntity bell) {
        WorkArea area = bell.area();
        WorkArea scan = new WorkArea(bell.getBlockPos(), SCAN_RADIUS, SCAN_HEIGHT);
        List<BlockPos> plates = new ArrayList<>();
        List<BlockPos> kitchenware = new ArrayList<>();
        List<BlockPos> counters = new ArrayList<>();
        List<BlockPos> cupboards = new ArrayList<>();
        List<BlockPos> silverChests = new ArrayList<>();
        List<BlockPos> barrels = new ArrayList<>();
        for (BlockEntity blockEntity : WorkWorld.blockEntities(level, scan)) {
            BlockPos pos = blockEntity.getBlockPos();
            if (blockEntity instanceof PlateBlockEntity) {
                plates.add(pos.immutable());
            } else if (blockEntity instanceof KitchenwareBlockEntity) {
                kitchenware.add(pos.immutable());
            } else if (blockEntity instanceof WorkshopStorageBlockEntity counter) {
                if (counter.workshopKind() != WorkshopKind.CONNECTED_COUNTER) {
                    continue;
                }
                RestaurantBlockEntity bound = counter.restaurant();
                if (bound != null && bound.id().equals(bell.id())) {
                    counters.add(pos.immutable());
                }
            } else if (blockEntity instanceof CupboardBlockEntity) {
                cupboards.add(pos.immutable());
            } else if (blockEntity instanceof CustomChestBlockEntity
                    && level.getBlockState(pos).is(RDBlocks.SILVER_CHEST_BLOCK.getChestBlock().asBlock())) {
                silverChests.add(pos.immutable());
            } else if (blockEntity instanceof BarrelBlockEntity) {
                barrels.add(pos.immutable());
            }
        }
        List<BlockPos> tables = new ArrayList<>();
        List<BlockPos> chairs = new ArrayList<>();
        for (BlockPos pos : scan.positions()) {
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            if (state.is(RDBlocks.TABLE.asBlock())) {
                tables.add(pos.immutable());
            } else if (state.is(RDBlocks.CHAIR.asBlock())) {
                chairs.add(pos.immutable());
            }
        }
        List<UUID> customers = new ArrayList<>();
        for (NPCSimpleEntity customer : level.getEntitiesOfClass(NPCSimpleEntity.class, scan.box(), NPCSimpleEntity::isAlive)) {
            if (!customer.getCustomerContainer().isSeatedFor(bell)) {
                continue;
            }
            customers.add(customer.getUUID());
        }
        return new RestaurantScene(bell.getBlockPos(), bell.id(), area, scan,
                plates, kitchenware, counters, cupboards, silverChests, barrels, tables, chairs, customers);
    }

    public boolean nearCounter(BlockPos pos) {
        for (BlockPos counter : counters) {
            if (counter.distSqr(pos) <= PLATE_COUNTER_DISTANCE_SQR) {
                return true;
            }
        }
        return false;
    }
}
