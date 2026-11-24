package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.block.kitchen.ChairBlock;
import cc.thonly.reverie_dreams.entity.SeatEntity;
import cc.thonly.reverie_dreams.entity.ai.work.*;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.restaurant.RestaurantService;

public class NPCCustomerGoal extends ScheduledWorkGoal {
    private BlockPos chair;
    private long sitAt;
    private RestaurantService.Shop restaurant;
    private final NPCSimpleEntity guest;

    public NPCCustomerGoal(NPCSimpleEntity npc) {
        super(npc, NPCWorkModes.CUSTOMER);
        this.guest = npc;
    }

    @Override
    protected boolean canSchedule() {
        return guest.getCustomerContainer().hasVisit() && guest.getCustomerContainer().isCustomerMode();
    }

    @Override
    protected boolean sameSite() {
        return !guest.getCustomerContainer().isManualCustomer() || super.sameSite();
    }

    private boolean ownSeat(BlockPos pos) {
        return npc.getVehicle() instanceof SeatEntity seat && seat.blockPosition().equals(pos);
    }

    private boolean available(ServerLevel level, BlockPos pos) {
        if (restaurant == null || !restaurant.bell().area().contains(pos) || !level.hasChunkAt(pos) || !site.contains(pos) || !work().available(pos, level.getGameTime()))
            return false;
        var state = level.getBlockState(pos);
        return state.is(RDBlocks.CHAIR.asBlock()) && (ownSeat(pos) || !state.getValue(ChairBlock.OCCUPIED) || ChairBlock.unlockIfFree(state, level, pos));
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        var queue = CustomerQueue.of(level);
        long now = level.getGameTime();
        var customer = guest.getCustomerContainer();
        restaurant = customer.findRestaurant().orElse(null);
        if (restaurant == null) {
            queue.leave(npc.getUUID());
            work().status("restaurant_closed");
            return false;
        }
        if (!customer.isManualCustomer())
            site = restaurant.bell().area();
        if (npc.getVehicle() instanceof SeatEntity seat && available(level, seat.blockPosition())) {
            chair = seat.blockPosition().immutable();
            queue.leave(npc.getUUID());
            return true;
        }
        queue.join(npc.getUUID(), site, now, restaurant.bell().id());
        chair = search.find(site, now, level::hasChunkAt, p -> available(level, p) && queue.turn(npc.getUUID(), p, now));
        if (chair == null) {
            work().status("customer_queue");
            return false;
        }
        queue.leave(npc.getUUID());
        sitAt = 0;
        return true;
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return restaurant != null && level.hasChunkAt(restaurant.bell().getBlockPos()) && level.getBlockEntity(restaurant.bell().getBlockPos()) == restaurant.bell()
                && restaurant.bell().linked() == restaurant.book() && (restaurant.bell().open()
                || guest.getCustomerContainer().hasOpenOrder() || guest.getCustomerContainer().getCooldownTickTime() >= 0)
                && chair != null && available(level, chair);
    }

    @Override
    protected List<?> resources() {
        return chair == null ? List.of() : List.of(chair);
    }

    @Override
    protected void runTask(ServerLevel level) {
        if (ownSeat(chair)) {
            if (guest.getCustomerContainer().hasOpenOrder())
                work().status("customer_seated");
            return;
        }
        if (!reachInteraction(chair, 2.5))
            return;
        if (sitAt == 0)
            sitAt = level.getGameTime();
        if (level.getGameTime() - sitAt > 40) {
            search.reject(chair, level.getGameTime());
            work().reject(chair, level.getGameTime());
            finish("unreachable", 20);
            return;
        }
        if (level.getGameTime() % 10 == 0)
            ChairBlock.sit(level.getBlockState(chair), level, chair, npc);
    }

    @Override
    protected void clearTask() {
        chair = null;
        restaurant = null;
    }
}
