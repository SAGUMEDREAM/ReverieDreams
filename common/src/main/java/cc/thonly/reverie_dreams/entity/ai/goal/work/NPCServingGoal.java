package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.block.entity.CupboardBlockEntity;
import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.block.entity.WorkshopStorageBlockEntity;
import cc.thonly.reverie_dreams.entity.SeatEntity;
import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.ai.work.WorkClaims;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.inventory.storage.StorageTransfer;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.restaurant.RestaurantScene;
import cc.thonly.reverie_dreams.restaurant.RestaurantSceneCache;
import cc.thonly.reverie_dreams.restaurant.RestaurantService;
import cc.thonly.reverie_dreams.restaurant.RestaurantTicket;
import cc.thonly.reverie_dreams.restaurant.SettlementParty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Fetches drinks and finished dishes for seated preference orders and settles on delivery.
 */
public class NPCServingGoal extends ScheduledWorkGoal {
    private enum Phase {DRINK, DISH, SERVE, COLLECT_COIN, DEPOSIT_COIN}

    private enum Source {NONE, CARGO, COUNTER, CUPBOARD, CHEST}

    private record Drink(Identifier id, Source kind, BlockPos pos, int slot, ItemStack sample, boolean fromCargo) {
    }

    private NPCSimpleEntity customer;
    private RestaurantTicket order;
    private BlockPos bellPos;
    private UUID bellId;
    private BlockPos plate;
    private Identifier dishId;
    private Identifier drinkId;
    private boolean dishTaken;
    private ItemStack platedDish = ItemStack.EMPTY;
    private Source sourceKind = Source.NONE;
    private BlockPos sourcePos;
    private int sourceSlot = -1;
    private ItemStack sourceSample = ItemStack.EMPTY;
    private ItemEntity targetCoin;
    private BlockPos targetDepositPos;
    private long nextCoinSearch;
    private Phase phase = Phase.DRINK;

    public NPCServingGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.SERVING);
    }

    @Override
    protected int searchInterval() {
        return 20;
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        clearTask();
        RestaurantService.Shop shop = RestaurantService.findForStaff(npc).orElse(null);
        if (shop == null) {
            work().status("restaurant_closed");
            return false;
        }
        bellPos = shop.bell().getBlockPos().immutable();
        bellId = shop.bell().id();
        RestaurantScene scene = RestaurantSceneCache.get(level, shop.bell());
        long now = level.getGameTime();
        if (findCoinTask(level, scene, now)) {
            return true;
        }
        List<NPCSimpleEntity> candidates = customers(level, scene);
        if (candidates.isEmpty()) {
            work().status("no_orders");
            return false;
        }
        boolean waitingDish = false;
        boolean waitingDrink = false;
        for (NPCSimpleEntity candidate : candidates) {
            NPCCustomerContainer container = candidate.getCustomerContainer();
            RestaurantTicket ticket = container.getRestaurantOrder();
            if (ticket == null)
                continue;
            BlockPos preparedPlate = null;
            Identifier foodId;
            if (container.hasPreparedFood()) {
                preparedPlate = container.preparedFoodPlate().orElse(null);
                foodId = container.preparedFoodId().orElse(null);
                if (preparedPlate == null || foodId == null || !ticket.allows(foodId, false))
                    continue;
                if (!level.hasChunkAt(preparedPlate) || !work().available(preparedPlate, now))
                    continue;
                boolean onPlate = plateHolds(level, preparedPlate, foodId, candidate.getUUID());
                boolean carried = NPCWorkInventory.find(npc.getInventory(), stack -> matchesFood(stack, foodId)) >= 0;
                if (!onPlate && !carried) {
                    if (level.hasChunkAt(preparedPlate) && work().available(preparedPlate, now)) {
                        container.clearPreparedFood();
                    }
                    continue;
                }
                if (!onPlate)
                    preparedPlate = null;
            } else {
                foodId = cargoDish(ticket);
                if (foodId == null) {
                    waitingDish = true;
                    continue;
                }
            }
            Drink drink = findDrink(level, scene, container, ticket, now);
            if (drink == null) {
                waitingDrink = true;
                continue;
            }
            select(candidate, preparedPlate, foodId, drink);
            phase = drink.fromCargo() ? (preparedPlate != null ? Phase.DISH : Phase.SERVE) : Phase.DRINK;
            return true;
        }
        if (waitingDish)
            work().status("waiting_for_dish");
        else if (waitingDrink)
            work().status("need_beverage");
        else
            work().status("no_orders");
        return false;
    }

    private List<NPCSimpleEntity> customers(ServerLevel level, RestaurantScene scene) {
        List<NPCSimpleEntity> result = new ArrayList<>();
        for (UUID id : scene.customers()) {
            if (!(level.getEntity(id) instanceof NPCSimpleEntity simple))
                continue;
            var container = simple.getCustomerContainer();
            if (!work().available(id, level.getGameTime()) || !container.isAutomationEligible()
                    || !bellId.equals(container.getRestaurantOrder().bellId()))
                continue;
            if (!(simple.getVehicle() instanceof SeatEntity seat) || !search.available(seat.blockPosition(), level.getGameTime()))
                continue;
            result.add(simple);
        }
        result.sort(Comparator.comparingDouble(simple -> simple.distanceToSqr(npc)));
        return result;
    }

    private Identifier cargoDish(RestaurantTicket ticket) {
        for (int slot = 0; slot < NPCWorkInventory.CARGO_SIZE; slot++) {
            ItemStack stack = npc.getInventory().getItem(slot);
            if (stack.isEmpty() || !stack.is(RDItemTags.CUISINE))
                continue;
            Identifier id = idOf(stack);
            if (ticket.allows(id, false))
                return id;
        }
        return null;
    }

    private Drink findDrink(ServerLevel level, RestaurantScene scene, NPCCustomerContainer container,
                            RestaurantTicket ticket, long now) {
        for (boolean preferred : new boolean[]{true, false}) {
            int cargo = drinkSlot(container, ticket, preferred);
            if (cargo >= 0) {
                ItemStack stack = npc.getInventory().getItem(cargo);
                return new Drink(idOf(stack), Source.CARGO, null, cargo, stack.copy(), true);
            }
            for (Source kind : new Source[]{Source.COUNTER, Source.CUPBOARD, Source.CHEST}) {
                for (BlockPos pos : positions(scene, kind)) {
                    if (!work().available(pos, now) || !level.hasChunkAt(pos))
                        continue;
                    Container source = containerAt(level, pos, kind);
                    if (source == null)
                        continue;
                    for (int slot = 0; slot < source.getContainerSize(); slot++) {
                        ItemStack stack = source.getItem(slot);
                        if (stack.isEmpty() || !stack.is(RDItemTags.BEVERAGE))
                            continue;
                        if (!ticket.allows(idOf(stack), true))
                            continue;
                        if (preferred && !container.prefersBeverage(stack))
                            continue;
                        return new Drink(idOf(stack), kind, pos.immutable(), slot, stack.copy(), false);
                    }
                }
            }
        }
        return null;
    }

    private int drinkSlot(NPCCustomerContainer container, RestaurantTicket ticket, boolean preferred) {
        for (int slot = 0; slot < NPCWorkInventory.CARGO_SIZE; slot++) {
            ItemStack stack = npc.getInventory().getItem(slot);
            if (stack.isEmpty() || !stack.is(RDItemTags.BEVERAGE))
                continue;
            if (!ticket.allows(idOf(stack), true))
                continue;
            if (preferred && !container.prefersBeverage(stack))
                continue;
            return slot;
        }
        return -1;
    }

    private static List<BlockPos> positions(RestaurantScene scene, Source kind) {
        return switch (kind) {
            case COUNTER -> scene.counters();
            case CUPBOARD -> scene.cupboards();
            case CHEST -> scene.silverChests();
            default -> List.of();
        };
    }

    private Container containerAt(ServerLevel level, BlockPos pos, Source kind) {
        if (!level.hasChunkAt(pos))
            return null;
        return switch (kind) {
            case COUNTER -> {
                if (!(level.getBlockEntity(pos) instanceof WorkshopStorageBlockEntity counter))
                    yield null;
                RestaurantBlockEntity bound = counter.restaurant();
                yield bound != null && bellId.equals(bound.id()) ? counter : null;
            }
            case CUPBOARD ->
                    level.getBlockEntity(pos) instanceof CupboardBlockEntity cupboard ? cupboard.getInventory() : null;
            case CHEST -> level.getBlockEntity(pos) instanceof CustomChestBlockEntity chest ? chest : null;
            default -> null;
        };
    }

    private boolean plateHolds(ServerLevel level, BlockPos pos, Identifier id, UUID customerId) {
        if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof PlateBlockEntity block))
            return false;
        ItemStack stack = block.getItem().build();
        return block.canTakeDish(bellId, customerId) && matchesFood(stack, id);
    }

    private boolean matchesFood(ItemStack stack, Identifier id) {
        return !stack.isEmpty() && stack.is(RDItemTags.CUISINE) && idOf(stack).equals(id);
    }

    private static Identifier idOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private void select(NPCSimpleEntity candidate, BlockPos plate, Identifier foodId, Drink drink) {
        this.customer = candidate;
        this.order = candidate.getCustomerContainer().getRestaurantOrder();
        this.plate = plate;
        this.dishId = foodId;
        this.drinkId = drink.id();
        this.dishTaken = false;
        this.platedDish = ItemStack.EMPTY;
        this.sourceKind = drink.kind();
        this.sourcePos = drink.pos();
        this.sourceSlot = drink.slot();
        this.sourceSample = drink.sample();
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        if (bellPos == null || !level.hasChunkAt(bellPos))
            return false;
        if (!(level.getBlockEntity(bellPos) instanceof RestaurantBlockEntity bell) || !bell.id().equals(bellId))
            return false;
        if (phase == Phase.COLLECT_COIN) {
            return targetCoin != null && targetCoin.isAlive() && !targetCoin.isRemoved() && isCoin(targetCoin.getItem());
        }
        if (phase == Phase.DEPOSIT_COIN) {
            if (targetDepositPos == null || !level.hasChunkAt(targetDepositPos))
                return false;
            Container container = getDepositContainer(level, targetDepositPos);
            return container != null && containsAnyCoin(container) && hasCoinInCargo();
        }
        if (customer == null || !customer.isAlive() || customer.level() != level)
            return false;
        var container = customer.getCustomerContainer();
        if (container.getRestaurantOrder() != order || !container.isSeatedFor(bell) || !container.isAutomationEligible())
            return false;
        if (plate != null && (!level.hasChunkAt(plate) || !(level.getBlockEntity(plate) instanceof PlateBlockEntity)))
            return false;
        if (sourceKind != Source.NONE && sourceKind != Source.CARGO
                && (sourcePos == null || containerAt(level, sourcePos, sourceKind) == null))
            return false;
        return true;
    }

    @Override
    protected List<?> resources() {
        List<Object> resources = new ArrayList<>(5);
        if (customer != null)
            resources.add(customer.getUUID());
        if (plate != null)
            resources.add(plate);
        if (sourcePos != null)
            resources.add(sourcePos);
        if (targetCoin != null)
            resources.add(targetCoin.getUUID());
        if (targetDepositPos != null)
            resources.add(targetDepositPos);
        return resources;
    }

    @Override
    protected void clearTask() {
        restoreDish();
        customer = null;
        order = null;
        bellPos = null;
        bellId = null;
        plate = null;
        dishId = null;
        drinkId = null;
        dishTaken = false;
        platedDish = ItemStack.EMPTY;
        sourceKind = Source.NONE;
        sourcePos = null;
        sourceSlot = -1;
        sourceSample = ItemStack.EMPTY;
        targetCoin = null;
        targetDepositPos = null;
        phase = Phase.DRINK;
    }

    @Override
    protected void runTask(ServerLevel level) {
        switch (phase) {
            case DRINK -> takeDrink(level);
            case DISH -> takeDish(level);
            case SERVE -> serve(level);
            case COLLECT_COIN -> collectCoin(level);
            case DEPOSIT_COIN -> depositCoin(level);
        }
    }

    private void takeDrink(ServerLevel level) {
        if (sourceKind == Source.CARGO) {
            advance();
            return;
        }
        if (sourcePos == null || sourceSlot < 0 || sourceKind == Source.NONE) {
            finish("need_beverage", 40);
            return;
        }
        if (!reachInteraction(sourcePos, 2.5))
            return;
        Container source = containerAt(level, sourcePos, sourceKind);
        if (source == null || sourceSlot >= source.getContainerSize()) {
            finish("need_beverage", 20);
            return;
        }
        ItemStack current = source.getItem(sourceSlot);
        if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, sourceSample)) {
            finish("need_beverage", 20);
            return;
        }
        int moved = StorageTransfer.move(source, sourceSlot, sourceSample, npc.getInventory(), NPCWorkInventory.CARGO_SIZE, 1);
        if (moved <= 0) {
            finish("inventory_full", 20);
            return;
        }
        if (sourceKind == Source.CUPBOARD && level.getBlockEntity(sourcePos) instanceof CupboardBlockEntity cupboard) {
            cupboard.setChanged();
        }
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        clearSource();
        advance();
    }

    private void takeDish(ServerLevel level) {
        if (plate == null || dishTaken) {
            advance();
            return;
        }
        if (!reachInteraction(plate, 2.5))
            return;
        if (!(level.getBlockEntity(plate) instanceof PlateBlockEntity block)) {
            finish("dish_missing", 20);
            return;
        }
        ItemStack current = block.getItem().build();
        if (!block.canTakeDish(bellId, customer.getUUID()) || !matchesFood(current, dishId)) {
            finish("dish_missing", 20);
            return;
        }
        ItemStack one = current.copyWithCount(1);
        if (NPCWorkInventory.insert(npc.getInventory(), one.copy()) <= 0) {
            finish("inventory_full", 20);
            return;
        }
        int remaining = current.getCount() - 1;
        block.setItem(remaining > 0 ? IngredientStack.of(current.copyWithCount(remaining)) : IngredientStack.empty());
        dishTaken = true;
        platedDish = one;
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        phase = Phase.SERVE;
    }

    private void serve(ServerLevel level) {
        NPCCustomerContainer container = customer.getCustomerContainer();
        if (!container.isAutomationEligible()) {
            finish("customer_left", 20);
            return;
        }
        if (!(customer.getVehicle() instanceof SeatEntity seat) || !reachInteraction(seat.blockPosition(), 2.0))
            return;
        int foodSlot = NPCWorkInventory.find(npc.getInventory(), this::matchesCarriedDish);
        int drinkSlot = NPCWorkInventory.find(npc.getInventory(),
                stack -> !stack.isEmpty() && stack.is(RDItemTags.BEVERAGE) && idOf(stack).equals(drinkId));
        if (foodSlot < 0 || drinkSlot < 0) {
            finish("serving_failed", 20);
            return;
        }
        RestaurantBlockEntity bell = bellPos != null && level.getBlockEntity(bellPos) instanceof RestaurantBlockEntity found
                ? found : null;
        if (bell == null || !bell.id().equals(bellId)) {
            finish("restaurant_closed", 20);
            return;
        }
        SettlementParty party = party(level, bell);
        if (party == null) {
            finish("serving_failed", 20);
            return;
        }
        ItemStack food = npc.getInventory().getItem(foodSlot).copyWithCount(1);
        ItemStack drink = npc.getInventory().getItem(drinkSlot).copyWithCount(1);
        if (!container.acceptDelivery(party, beneficiary(bell), food, drink)) {
            boolean eligible = container.isAutomationEligible();
            if (eligible)
                restoreDish();
            finish(eligible ? "serving_failed" : "customer_left", 20);
            return;
        }
        shrink(foodSlot);
        if (drinkSlot != foodSlot)
            shrink(drinkSlot);
        platedDish = ItemStack.EMPTY;
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        finish("delivered", 10);
    }

    private SettlementParty party(ServerLevel level, RestaurantBlockEntity bell) {
        return npc instanceof SettlementParty waiter ? waiter : null;
    }

    private boolean findCoinTask(ServerLevel level, RestaurantScene scene, long now) {
        if (now < nextCoinSearch) {
            return false;
        }
        ItemEntity coin = findTargetCoin(level, scene, now);
        if (coin == null) {
            return false;
        }
        ItemStack coinItem = coin.getItem();
        if (StorageTransfer.capacity(npc.getInventory(), coinItem, NPCWorkInventory.CARGO_SIZE) > 0) {
            this.targetCoin = coin;
            this.phase = Phase.COLLECT_COIN;
            work().status("collecting_coins");
            return true;
        }
        if (hasCoinInCargo()) {
            BlockPos depositPos = findCoinContainer(level, scene, now);
            if (depositPos != null) {
                this.targetCoin = coin;
                this.targetDepositPos = depositPos;
                this.phase = Phase.DEPOSIT_COIN;
                work().status("depositing_coins");
                return true;
            }
        }
        nextCoinSearch = now + 60L;
        return false;
    }

    private ItemEntity findTargetCoin(ServerLevel level, RestaurantScene scene, long now) {
        List<ItemEntity> items = level.getEntitiesOfClass(
                ItemEntity.class,
                scene.scan().box(),
                item -> item.isAlive() && !item.isRemoved() && !item.hasPickUpDelay()
                        && isCoin(item.getItem()) && work().available(item.getUUID(), now)
        );
        if (items.isEmpty()) {
            return null;
        }
        items.sort(Comparator.comparingDouble(item -> item.distanceToSqr(npc)));
        return items.get(0);
    }

    private BlockPos findCoinContainer(ServerLevel level, RestaurantScene scene, long now) {
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : scene.barrels()) {
            if (isValidCoinContainer(level, pos, now)) {
                candidates.add(pos);
            }
        }
        for (BlockPos pos : scene.silverChests()) {
            if (isValidCoinContainer(level, pos, now)) {
                candidates.add(pos);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        candidates.sort(Comparator.comparingDouble(pos -> pos.distSqr(npc.blockPosition())));
        return candidates.get(0);
    }

    private boolean isValidCoinContainer(ServerLevel level, BlockPos pos, long now) {
        if (!level.hasChunkAt(pos) || !work().available(pos, now)) {
            return false;
        }
        Container container = getDepositContainer(level, pos);
        if (container == null) {
            return false;
        }
        if (!containsAnyCoin(container)) {
            return false;
        }
        return containerHasSpaceForCoins(container);
    }

    private Container getDepositContainer(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BarrelBlockEntity barrel) {
            return barrel;
        }
        if (be instanceof CustomChestBlockEntity chest) {
            return chest;
        }
        return null;
    }

    private static boolean isCoin(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(RDItemTags.COIN) || stack.is(RDItemTags.COMMON_COIN));
    }

    private static boolean containsAnyCoin(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isCoin(stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean containerHasSpaceForCoins(Container container) {
        for (int slot = 0; slot < NPCWorkInventory.CARGO_SIZE; slot++) {
            ItemStack carried = npc.getInventory().getItem(slot);
            if (isCoin(carried) && StorageTransfer.capacity(container, carried, container.getContainerSize()) > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCoinInCargo() {
        for (int i = 0; i < NPCWorkInventory.CARGO_SIZE; i++) {
            ItemStack stack = npc.getInventory().getItem(i);
            if (isCoin(stack)) {
                return true;
            }
        }
        return false;
    }

    private void collectCoin(ServerLevel level) {
        if (targetCoin == null || !targetCoin.isAlive() || targetCoin.isRemoved()) {
            finish("collected_coins", 10);
            return;
        }
        if (!reach(targetCoin, 1.2)) {
            return;
        }
        ItemStack coinStack = targetCoin.getItem();
        if (coinStack.isEmpty() || !isCoin(coinStack)) {
            finish("collected_coins", 10);
            return;
        }
        int inserted = NPCWorkInventory.insert(npc.getInventory(), coinStack);
        if (inserted <= 0) {
            finish("inventory_full", 20);
            return;
        }
        npc.take(targetCoin, inserted);
        if (coinStack.isEmpty()) {
            targetCoin.discard();
        } else {
            targetCoin.setItem(coinStack);
        }
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        finish("collected_coins", 10);
    }

    private void depositCoin(ServerLevel level) {
        if (targetDepositPos == null) {
            finish("need_coin_container", 20);
            return;
        }
        if (!reachInteraction(targetDepositPos, 2.5)) {
            return;
        }
        Container container = getDepositContainer(level, targetDepositPos);
        if (container == null || !containsAnyCoin(container)) {
            finish("need_coin_container", 20);
            return;
        }
        int movedTotal = 0;
        for (int slot = 0; slot < NPCWorkInventory.CARGO_SIZE; slot++) {
            ItemStack carried = npc.getInventory().getItem(slot);
            if (!isCoin(carried)) continue;
            int count = carried.getCount();
            int moved = StorageTransfer.move(npc.getInventory(), slot, carried, container, container.getContainerSize(), count);
            if (moved > 0) {
                movedTotal += moved;
            }
        }
        if (movedTotal <= 0) {
            finish("coin_container_full", 40);
            return;
        }
        if (level.getBlockEntity(targetDepositPos) instanceof BlockEntity be) {
            be.setChanged();
        }
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        finish("deposited_coins", 10);
    }

    private UUID beneficiary(RestaurantBlockEntity bell) {
        if (bell.owner() != null)
            return bell.owner();
        return npc instanceof NPCSimpleEntity simple ? simple.getOwnerUuid() : null;
    }

    private void shrink(int slot) {
        ItemStack stack = npc.getInventory().getItem(slot);
        npc.getInventory().setItem(slot, stack.getCount() > 1 ? stack.copyWithCount(stack.getCount() - 1) : ItemStack.EMPTY);
    }

    private void advance() {
        phase = plate != null && !dishTaken ? Phase.DISH : Phase.SERVE;
    }

    private void clearSource() {
        sourceKind = Source.NONE;
        sourcePos = null;
        sourceSlot = -1;
        sourceSample = ItemStack.EMPTY;
    }

    /**
     * A stopped waiter puts the carried dish back where the customer still expects it; never drops it.
     */
    private void restoreDish() {
        if (platedDish.isEmpty() || !dishTaken || plate == null || dishId == null || customer == null)
            return;
        if (!(npc.level() instanceof ServerLevel level) || !level.hasChunkAt(plate))
            return;
        NPCCustomerContainer container = customer.getCustomerContainer();
        if (container.getRestaurantOrder() != order || !container.preparedFoodPlate().map(plate::equals).orElse(false)
                || !container.preparedFoodId().map(dishId::equals).orElse(false))
            return;
        if (!(level.getBlockEntity(plate) instanceof PlateBlockEntity block) || !block.getItem().isEmpty())
            return;
        int slot = NPCWorkInventory.find(npc.getInventory(), this::matchesCarriedDish);
        if (slot < 0)
            return;
        ItemStack stack = npc.getInventory().getItem(slot);
        ItemStack one = stack.copyWithCount(1);
        block.setRestaurantDish(IngredientStack.of(one), bellId, customer.getUUID());
        npc.getInventory().setItem(slot, stack.getCount() > 1 ? stack.copyWithCount(stack.getCount() - 1) : ItemStack.EMPTY);
    }

    private boolean matchesCarriedDish(ItemStack stack) {
        return matchesFood(stack, dishId)
                && (platedDish.isEmpty() || ItemStack.isSameItemSameComponents(stack, platedDish));
    }
}
