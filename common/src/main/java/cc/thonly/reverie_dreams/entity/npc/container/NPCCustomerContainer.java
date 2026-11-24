package cc.thonly.reverie_dreams.entity.npc.container;

import cc.thonly.reverie_dreams.server.dialog.NPCSpeech;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.restaurant.*;
import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.entity.ai.work.CustomerQueue;
import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.data.*;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.entity.npc.NPCInteractResult;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.inventory.CustomerMealDisplay;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.math.ModMth;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import cc.thonly.reverie_dreams.entity.SeatEntity;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "resource", "OptionalUsedAsFieldOrParameterType", "RedundantIfStatement", "UnnecessaryLocalVariable"})
@Getter
public class NPCCustomerContainer {
    public static final Identifier KEY = ReverieDreams.id("customer");
    public static final int MAX_SCORE = 10;
    public static final int MIN_SCORE = -10;
    public static final int DEFAULT_SCORE = 0;
    public static final String REQUEST_FOOD_KEY = "reverie_dreams.message.request.food.key";
    public static final String REQUEST_BEVERAGE_KEY = "reverie_dreams.message.request.beverage.key";
    public static final String COOLDOWN_KEY = "reverie_dreams.message.request.cooldown.key";

    final NPCSimpleEntity npc;
    final RandomSource randomSource;

    IngredientStack submitFood = IngredientStack.empty();
    IngredientStack submitBeverage = IngredientStack.empty();

    private final CustomerMealDisplay mealDisplay = new CustomerMealDisplay();
    private final NPCMealSchedule meals = new NPCMealSchedule();
    private static final long ARRIVAL_TIMEOUT = 20L * 45;
    int timeout = Math.max(1, ReverieDreams.config().maxCustomerTickTime);
    int cooldownTickTime = -1;
    private RestaurantTicket restaurantOrder;
    private BlockPos visitingBell;
    private UUID visitingBellId;
    private long visitUntil;
    private BlockPos preparedFoodPlate;
    private Identifier preparedFoodId;

    public boolean isManualCustomer() {
        return npc.getNPCState() == NPCStates.WORKING && npc.getWorkMode() == NPCWorkModes.CUSTOMER;
    }

    public boolean hasVisit() {
        return visitingBell != null || hasOpenOrder() || isEatingCooldown();
    }

    public void allowVisitNow(ServerLevel level) {
        meals.allowVisitNow(level, npc.getUUID());
    }

    private boolean freeToVisit() {
        return npc.isAlive() && npc.isTame() && npc.getTarget() == null && !npc.isLeashed()
                && npc.canTakeRestaurantBreak()
                && (npc.getNPCState() == NPCStates.NORMAL || isManualCustomer());
    }

    private void tryStartVisit(ServerLevel level) {
        if (npc.isPassenger() || !freeToVisit() || (isManualCustomer() && !NPCWorkModes.canAssign(NPCWorkModes.CUSTOMER))
                || !meals.ready(level, npc.getUUID()))
            return;
        BlockPos anchor = isManualCustomer() ? npc.getWorkingPos() : npc.blockPosition();
        var shop = RestaurantService.find(npc, anchor, candidate -> candidate.bell().open() && candidate.bell().ready()
                && !menuChoices(candidate, false).isEmpty() && !menuChoices(candidate, true).isEmpty()).orElse(null);
        if (shop == null) {
            if (isManualCustomer())
                npc.getWorkController().status("restaurant_closed");
            meals.retryLater(level, npc.getUUID());
            return;
        }
        visitingBell = shop.bell().getBlockPos().immutable();
        visitingBellId = shop.bell().id();
        visitUntil = level.getGameTime() + ARRIVAL_TIMEOUT;
        if (npc.isSleeping())
            npc.stopSleeping();
    }

    public boolean hasOpenOrder() {
        return restaurantOrder != null;
    }

    public boolean isSeatedFor(RestaurantBlockEntity bell) {
        return this.isCustomerMode() && this.restaurantOrder != null
                && this.restaurantOrder.bellId().equals(bell.id())
                && this.npc.getVehicle() instanceof SeatEntity seat && bell.area().contains(seat.blockPosition());
    }

    public Optional<RestaurantService.Shop> findRestaurant() {
        if (!(npc.level() instanceof ServerLevel level))
            return Optional.empty();
        var pos = restaurantOrder == null ? visitingBell : restaurantOrder.bellPos();
        var id = restaurantOrder == null ? visitingBellId : restaurantOrder.bellId();
        if (pos != null && level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof RestaurantBlockEntity bell
                && bell.id().equals(id) && bell.isBell() && bell.linked() != null
                && (restaurantOrder != null ? availability() == RestaurantTicket.Availability.READY
                : bell.open() && bell.ready())) {
            return Optional.of(new RestaurantService.Shop(bell, bell.linked()));
        }
        return Optional.empty();
    }

    private RestaurantTicket.Availability availability() {
        if (restaurantOrder == null || !(npc.level() instanceof ServerLevel level))
            return RestaurantTicket.Availability.REMOVED;
        return restaurantOrder.availability(level::hasChunkAt, pos -> level.getBlockEntity(pos) instanceof RestaurantBlockEntity be
                ? new RestaurantTicket.Facility(be.id(), be.linkedId(), be.isBell()) : null);
    }

    private boolean returnUnsettledItems() {
        if (!(npc.level() instanceof ServerLevel level))
            return false;
        if (!submitFood.isEmpty() && refund(level, submitFood))
            submitFood = IngredientStack.empty();
        if (!submitBeverage.isEmpty() && refund(level, submitBeverage))
            submitBeverage = IngredientStack.empty();
        return submitFood.isEmpty() && submitBeverage.isEmpty();
    }

    private boolean refund(ServerLevel level, IngredientStack ingredient) {
        var drop = new ItemEntity(level, npc.getX(), npc.getY() + .5, npc.getZ(), ingredient.build().copy());
        drop.setPickUpDelay(20);
        return level.addFreshEntity(drop);
    }

    private void cancelOrder() {
        if (!returnUnsettledItems()) {
            npc.getWorkController().status("restaurant_refund_wait");
            return;
        }
        resetOrder();
        cooldownTickTime = -1;
    }

    private void endVisit(ServerLevel level, boolean retry) {
        boolean wasVisiting = hasVisit();
        if (isEatingCooldown())
            clearHands();
        if (hasOpenOrder() || !isSubmitEmpty())
            cancelOrder();
        cooldownTickTime = -1;
        visitingBell = null;
        visitingBellId = null;
        visitUntil = 0;
        if (wasVisiting && npc.getVehicle() instanceof SeatEntity)
            npc.stopRiding();
        CustomerQueue.of(level).leave(npc.getUUID());
        if (retry)
            meals.retryLater(level, npc.getUUID());
    }

    boolean fixedFood = false;
    List<FoodProperty> likes = new ArrayList<>();
    List<FoodProperty> dislikes = new ArrayList<>();
    Item fixedRequiredFood = Items.AIR;

    boolean fixedBeverage = false;
    BeverageProperty beverageProperty = BeverageProperties.UNDEFINED;
    Item fixedRequiredBeverage = Items.AIR;

    List<Component> components = new ArrayList<>();
    int score = DEFAULT_SCORE;
    Optional<String> evaluationFeedback = Optional.empty();

    int priceOutput = 0;
    int orderBudget = 0;

    public NPCCustomerContainer(NPCSimpleEntity npc, RandomSource randomSource) {
        this.npc = npc;
        this.randomSource = randomSource;
    }

    public int calculateFoodPrice(ItemStack itemStack) {
        Collection<FoodProperty> foodProperties = FoodProperties.get(itemStack);
        int base = 3 + foodProperties.size();
        return (int) (base * 1.25 * 2);
    }

    public int calculateBeveragePrice(ItemStack itemStack) {
        Collection<BeverageProperty> beverageProperties = BeverageProperties.get(itemStack);
        int base = 3 + beverageProperties.size() / 2;
        int price = BeverageProperties.getPriceCalculationTable().getOrDefault(itemStack.getItem(), 8);
        return (int) ((base + price) * 1.25);
    }

    public int calculateOrderPrice() {
        int foodBudget = this.calculateFoodPrice(this.submitFood.build());
        int beverageBudget = this.calculateBeveragePrice(this.submitBeverage.build());
        int price = foodBudget + beverageBudget;
//        System.out.println(price);
        return price;
    }

    private int rollBudget(CustomerBudget budget) {
        int min = Math.max(0, budget.min());
        int max = Math.max(min, budget.max());
        long span = (long) max - min + 1L;
        if (span <= 0L) {
            return min;
        }
        return min + this.randomSource.nextInt((int) Math.min(Integer.MAX_VALUE, span));
    }

    public boolean isSubmitEmpty() {
        return this.submitFood.isEmpty() && this.submitBeverage.isEmpty();
    }

    /**
     * Chefs and waiters only handle untouched preference orders; fixed dishes and drinks stay manual.
     */
    public boolean isAutomationEligible() {
        return this.isCustomerMode()
                && !this.isEatingCooldown()
                && this.npc.getVehicle() instanceof SeatEntity
                && this.restaurantOrder != null
                && this.availability() == RestaurantTicket.Availability.READY
                && !this.fixedFood
                && !this.fixedBeverage
                && this.isSubmitEmpty();
    }

    public boolean hasPreparedFood() {
        return this.preparedFoodPlate != null && this.preparedFoodId != null;
    }

    public boolean awaitingAutomationFood() {
        return this.isAutomationEligible() && !this.hasPreparedFood();
    }

    public Optional<BlockPos> preparedFoodPlate() {
        return Optional.ofNullable(this.preparedFoodPlate);
    }

    public Optional<Identifier> preparedFoodId() {
        return Optional.ofNullable(this.preparedFoodId);
    }

    public boolean attachPreparedFood(BlockPos plate, Identifier item) {
        if (!this.isAutomationEligible() || this.hasPreparedFood()) {
            return false;
        }
        this.preparedFoodPlate = plate.immutable();
        this.preparedFoodId = item;
        return true;
    }

    public void clearPreparedFood() {
        if (this.preparedFoodPlate != null && npc.level() instanceof ServerLevel level
                && level.hasChunkAt(this.preparedFoodPlate)
                && level.getBlockEntity(this.preparedFoodPlate) instanceof PlateBlockEntity plate) {
            plate.releaseDish(npc.getUUID());
        }
        this.preparedFoodPlate = null;
        this.preparedFoodId = null;
    }

    public int foodPreference(ItemStack food) {
        int score = 0;
        for (FoodProperty property : FoodProperties.get(food)) {
            if (this.likes.contains(property))
                score++;
            if (this.dislikes.contains(property))
                score -= 2;
        }
        return score;
    }

    /**
     * Waiter hands over a cooked dish and a beverage taken from restaurant storage.
     */
    public boolean acceptDelivery(SettlementParty party, UUID beneficiary, ItemStack food, ItemStack beverage) {
        if (!this.isAutomationEligible() || !this.isSubmitEmpty() || this.restaurantOrder == null) {
            return false;
        }
        if (food.isEmpty() || beverage.isEmpty() || !this.isFood(food) || !this.isBeverage(beverage)) {
            return false;
        }
        Identifier foodId = BuiltInRegistries.ITEM.getKey(food.getItem());
        Identifier beverageId = BuiltInRegistries.ITEM.getKey(beverage.getItem());
        if (!this.restaurantOrder.allows(foodId, false) || !this.restaurantOrder.allows(beverageId, true)) {
            return false;
        }
        this.submitFood = IngredientStack.of(food.copyWithCount(1));
        this.submitBeverage = IngredientStack.of(beverage.copyWithCount(1));
        if (this.processSubmitOrder(party, beneficiary)) {
            return true;
        }
        this.submitFood = IngredientStack.empty();
        this.submitBeverage = IngredientStack.empty();
        return false;
    }

    public void tick() {
        if (!(npc.level() instanceof ServerLevel level))
            return;
        if (!hasOpenOrder() && !isEatingCooldown() && npc.getWorkMode() == NPCWorkModes.CUSTOMER
                && !NPCWorkModes.canAssign(NPCWorkModes.CUSTOMER)) {
            if (hasVisit() || !isSubmitEmpty())
                endVisit(level, false);
            if (isManualCustomer())
                npc.setNPCState(NPCStates.NORMAL);
            npc.setWorkMode(NPCWorkModes.COMBAT);
        }
        if (!hasVisit()) {
            if (!isSubmitEmpty())
                cancelOrder();
            if (isSubmitEmpty() && npc.tickCount % 20 == 0)
                tryStartVisit(level);
            return;
        }
        if (!freeToVisit()) {
            endVisit(level, true);
            return;
        }
        if (this.cooldownTickTime >= 0) {
            if (this.cooldownTickTime > 0)
                this.cooldownTickTime--;

            if (this.cooldownTickTime > 0
                    && this.cooldownTickTime % 20 == 0) {
                boolean isMainHand = this.randomSource.nextBoolean();
                SoundEventPlayUtils.playSound(
                        this.npc,
                        isMainHand ? SoundEvents.GENERIC_EAT.value() : SoundEvents.GENERIC_DRINK.value(),
                        SoundSource.PLAYERS,
                        1.0f,
                        1.0f
                );
                if (isMainHand) {
                    this.npc.swing(InteractionHand.MAIN_HAND);
                } else {
                    this.npc.swing(InteractionHand.OFF_HAND);
                }
                ParticleOptions particle = this.getParticle(isMainHand);

                for (int i = 0; i < 8; i++) {
                    this.npc.level().addParticle(particle, this.npc.getX(), this.npc.getY(), this.npc.getZ(), 0.0, 0.0, 0.0);
                }
            }

            if (this.cooldownTickTime <= 0) {
                endVisit(level, false);
            }

            return;
        }


        if (restaurantOrder == null) {
            if (level.getGameTime() >= visitUntil || findRestaurant().isEmpty()) {
                endVisit(level, true);
            } else if (npc.getVehicle() instanceof SeatEntity) {
                nextOrder();
            }
            return;
        }
        var state = availability();
        if (state == RestaurantTicket.Availability.UNLOADED) {
            if (--this.timeout <= 0)
                endVisit(level, true);
            npc.getWorkController().status("restaurant_unloaded");
            return;
        }
        if (state == RestaurantTicket.Availability.REMOVED) {
            endVisit(level, true);
            npc.getWorkController().status("restaurant_removed");
            return;
        }
        if (!(this.npc.getVehicle() instanceof SeatEntity)) {
            endVisit(level, true);
            return;
        }
        this.timeout--;
        if (this.timeout <= 0) {
            endVisit(level, true);
        }
    }

    private ParticleOptions getParticle(boolean isMainHand) {
        InteractionHand hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack item = this.mealDisplay.meal(hand);
        if (item.isEmpty())
            item = isMainHand ? this.npc.getMainHandItem() : this.npc.getOffhandItem();
        return item.isEmpty()
                ? ParticleTypes.ITEM_SNOWBALL
                : new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(item));
    }

    public boolean isCustomerMode() {
        return freeToVisit() && (hasVisit() || isManualCustomer() && NPCWorkModes.canAssign(NPCWorkModes.CUSTOMER));
    }

    public void insertStackToHand(
            InteractionHand hand,
            ItemStack stack
    ) {
        if (!(this.npc.level() instanceof ServerLevel))
            return;
        if (stack.isEmpty()) {
            this.mealDisplay.clear(this.npc.getInventory());
        } else {
            this.mealDisplay.show(this.npc.getInventory(), hand, stack);
        }
    }

    public NPCInteractResult triggerInteraction(
            ServerPlayer player,
            InteractionHand hand,
            ItemStack stack
    ) {
        if (!this.isCustomerMode()) {
            return NPCInteractResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            return NPCInteractResult.PASS;
        }

        if (!this.npc.isPassenger()) {
            return NPCInteractResult.PASS;
        }

        if (this.isEatingCooldown()) {
            this.tell(player, Component.translatable(COOLDOWN_KEY));
            return NPCInteractResult.PASS;
        }

        if (!canSubmitOrder()) {
            this.tell(player, Component.translatable("restaurant.reverie_dreams.no_order"));
            return NPCInteractResult.PASS;
        }
        if ((!submitFood.isEmpty() || !submitBeverage.isEmpty()) && availability() == RestaurantTicket.Availability.REMOVED) {
            cancelOrder();
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);
            this.tellOrder(player);
            player.swing(hand);
            return NPCInteractResult.PASS;
        }

        if (this.isSubmitFullStack()) {
            this.processSubmitOrder(player);
            return NPCInteractResult.SUCCESS;
        }

        if ((this.isFood(stack) && this.submitFood.isEmpty() && !restaurantOrder.allows(BuiltInRegistries.ITEM.getKey(stack.getItem()), false))
                || (this.isBeverage(stack) && this.submitBeverage.isEmpty() && !restaurantOrder.allows(BuiltInRegistries.ITEM.getKey(stack.getItem()), true))) {
            this.tell(player, Component.translatable("restaurant.reverie_dreams.off_menu"));
            return NPCInteractResult.PASS;
        }
        boolean submitItem = false;

        if (this.isFood(stack) && this.submitFood.isEmpty()) {
            this.submitFood = IngredientStack.of(stack.copyWithCount(1));
            stack.shrink(1);

            SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);

            submitItem = true;
        }

        if (this.isBeverage(stack) && this.submitBeverage.isEmpty()) {
            this.submitBeverage = IngredientStack.of(stack.copyWithCount(1));
            stack.shrink(1);

            SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);

            submitItem = true;
        }

        if (!submitItem) {
            return NPCInteractResult.PASS;
        }

        if (this.isSubmitFullStack()) {
            SoundEventPlayUtils.playUISound(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            this.processSubmitOrder(player);
            return NPCInteractResult.SUCCESS;
        }

        return NPCInteractResult.PASS;
    }

    /**
     * Counter consumes real stock; the existing order scorer is the only settlement path.
     */
    public boolean acceptFromCounter(ServerPlayer player, Container stock, RestaurantBlockEntity bell) {
        if (!canSubmitOrder() || !bell.canEdit(player) || restaurantOrder == null || !restaurantOrder.bellId().equals(bell.id()))
            return false;
        boolean accepted = false;
        for (boolean beverage : new boolean[]{false, true}) {
            if (!(beverage ? submitBeverage : submitFood).isEmpty())
                continue;
            for (int slot = 0; slot < stock.getContainerSize(); slot++) {
                var candidate = stock.getItem(slot);
                if (candidate.isEmpty() || !(beverage ? isBeverage(candidate) : isFood(candidate)) || !restaurantOrder.allows(BuiltInRegistries.ITEM.getKey(candidate.getItem()), beverage))
                    continue;
                var taken = stock.removeItem(slot, 1);
                if (taken.isEmpty())
                    continue;
                if (beverage)
                    submitBeverage = IngredientStack.of(taken);
                else
                    submitFood = IngredientStack.of(taken);
                accepted = true;
                break;
            }
        }
        if (isSubmitFullStack())
            processSubmitOrder(player);
        return accepted;
    }

    public void processSubmitOrder(ServerPlayer player) {
        this.processSubmitOrder(SettlementParty.of(player), player.getUUID());
    }

    public boolean processSubmitOrder(SettlementParty party, UUID beneficiary) {
        if (!this.isSubmitFullStack() || !canSubmitOrder())
            return false;
        this.score = DEFAULT_SCORE;

        if (this.fixedFood) {
            if (this.submitFood.is(this.fixedRequiredFood)) {
                this.score += 5;
            } else {
                this.score -= 5;
            }
        }

        this.score += this.foodPreference(this.submitFood.build());

        if (this.fixedBeverage) {
            if (this.submitBeverage.is(this.fixedRequiredBeverage)) {
                this.score += 5;
            } else {
                this.score -= 8;
            }
        } else {
            List<BeverageProperty> beverageProperties =
                    BeverageProperties.get(this.submitBeverage);

            boolean find = false;

            for (BeverageProperty property : beverageProperties) {
                if (property.is(this.beverageProperty)) {
                    find = true;
                    break;
                }
            }

            if (find) {
                this.score += 5;
            } else {
                this.score -= 8;
            }
        }
        int budget = this.calculateOrderPrice();

        int max = this.orderBudget > 0 ? this.orderBudget : this.rollBudget(this.getCustomer().budget());

        int overBudget = Math.max(0, budget - max);
        boolean tooExpensive = overBudget > 0;

        this.score -= (int) (overBudget * 0.5);
        this.score = Math.max(
                MIN_SCORE,
                Math.min(this.score, MAX_SCORE)
        );

        // 超出预算时只支付一小部分，避免用低价食材换满额预算。
        this.priceOutput = tooExpensive ? Math.max(0, max / 3) : Math.min(budget, max);

        IngredientStack food = this.submitFood.copy();
        IngredientStack beverage = this.submitBeverage.copy();
        Runnable action = () -> {
            food.build().finishUsingItem(this.npc.level(), this.npc);
            beverage.build().finishUsingItem(this.npc.level(), this.npc);
        };
        action.run();

        Settlement.Outcome outcome = this.score >= 0 ? Settlement.Outcome.SUCCESS
                : tooExpensive ? Settlement.Outcome.TOO_EXPENSIVE : Settlement.Outcome.ANGRY;
        int coins = outcome == Settlement.Outcome.ANGRY ? this.priceOutput / 3 : this.priceOutput;
        if (beneficiary != null) {
            double delta = switch (outcome) {
                case SUCCESS -> (double) this.score / (this.randomSource.nextBoolean() ? 2 : 4);
                case TOO_EXPENSIVE -> (double) this.score / (this.randomSource.nextBoolean() ? 2 : 4);
                case ANGRY -> (double) this.score / (this.randomSource.nextBoolean() ? 3 : 6);
            };
            this.npc.getFavorabilityContainer().add(beneficiary, delta);
        }

        this.evaluationFeedback = this.getCustomer().evaluation().getEvaluationFeedback(this.score, overBudget);
        if (this.evaluationFeedback.isEmpty()) {
            this.evaluationFeedback = CustomerEvaluation.getRandomEvaluation(this.randomSource)
                    .getEvaluationFeedback(this.score, overBudget);
        }
        party.settle(this.npc, new Settlement(Optional.ofNullable(beneficiary), outcome, coins,
                this.randomSource.nextInt(2, 16), this.evaluationFeedback));
        if (party.getBaseEntity() instanceof ServerPlayer listener) {
            this.evaluationFeedback.ifPresent(msg -> this.tell(listener, Component.translatable(msg)));
        }

        this.priceOutput = 0;

        this.insertStackToHand(
                InteractionHand.MAIN_HAND,
                food.copy().build()
        );

        this.insertStackToHand(
                InteractionHand.OFF_HAND,
                beverage.copy().build()
        );

        this.npc.setLockSlot(true);

        // 修改：resetOrder 不再修改 timeout
        this.resetOrder();

        meals.finishMeal((ServerLevel) npc.level(), npc.getUUID());
        this.cooldownTickTime = Math.max(0, ReverieDreams.config().maxCustomerCooldownTickTime);
        return true;
    }

    public void tell(Player player, Component component) {
        if (player instanceof ServerPlayer listener) {
            NPCSpeech.sayTo(this.npc, listener, component);
        }
    }

    public void tellOrder(Player player) {
        if (!(player instanceof ServerPlayer listener) || this.components.isEmpty()) {
            return;
        }
        MutableComponent order = Component.empty();
        for (Component line : this.components) {
            if (!order.getSiblings().isEmpty()) {
                order.append("\n");
            }
            order.append(line);
        }
        NPCSpeech.sayWithTranscript(this.npc, listener, order,
                Component.empty().append(this.npc.getName()).append(": ").append(order));
    }

    public Customer getCustomer() {
        RoleType roleType = this.npc.getRoleType();
        return roleType.getCustomer();
    }

    private boolean canSubmitOrder() {
        return this.isCustomerMode()
                && !this.isEatingCooldown()
                && this.npc.getVehicle() instanceof SeatEntity && restaurantOrder != null && availability() == RestaurantTicket.Availability.READY;
    }

    private boolean isEatingCooldown() {
        return this.cooldownTickTime >= 0;
    }

    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(RDItemTags.CUISINE);
    }

    public boolean isBeverage(ItemStack itemStack) {
        return itemStack.is(RDItemTags.BEVERAGE);
    }

    /**
     * Waiter hint: the drink satisfies this order's preferred beverage property.
     */
    public boolean prefersBeverage(ItemStack itemStack) {
        if (this.fixedBeverage || !this.isBeverage(itemStack)) {
            return false;
        }
        if (this.beverageProperty.equals(BeverageProperties.UNDEFINED)) {
            return true;
        }
        for (BeverageProperty property : BeverageProperties.get(itemStack)) {
            if (property.is(this.beverageProperty)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSubmitFullStack() {
        return !this.submitFood.isEmpty()
                && !this.submitBeverage.isEmpty();
    }

    private Component getItemName(Item item) {
        return item.getDefaultInstance().getHoverName();
    }

    public void nextOrder() {
        if (!(npc.level() instanceof ServerLevel level) || !hasVisit() || hasOpenOrder()
                || !isCustomerMode() || !(npc.getVehicle() instanceof SeatEntity seat))
            return;
        var shop = findRestaurant().filter(r -> r.bell().open()).orElse(null);
        if (shop == null) {
            npc.getWorkController().status("restaurant_closed");
            endVisit(level, true);
            return;
        }
        if (!shop.bell().area().contains(seat.blockPosition())) {
            endVisit(level, true);
            return;
        }
        Customer customer = this.getCustomer();
        boolean generic = customer.isGeneric();
        var foods = menuChoices(shop, false);
        var drinks = menuChoices(shop, true);
        if (foods.isEmpty() || drinks.isEmpty()) {
            npc.getWorkController().status("restaurant_no_preference");
            endVisit(level, true);
            return;
        }
        timeout = Math.max(1, ReverieDreams.config().maxCustomerTickTime);
        if (!returnUnsettledItems()) {
            npc.getWorkController().status("restaurant_refund_wait");
            return;
        }
        clearHands();
        resetOrder();
        restaurantOrder = RestaurantTicket.of(shop);
        orderBudget = rollBudget(customer.budget());
        likes = new ArrayList<>(customer.likes());
        dislikes = new ArrayList<>(customer.dislikes());
        fixedFood = generic || likes.isEmpty();
        if (fixedFood) {
            fixedRequiredFood = ModMth.getRandomElement(randomSource, foods);
            components.add(Component.translatable(REQUEST_FOOD_KEY, getItemName(fixedRequiredFood)));
        } else if (!customer.orders().isEmpty())
            components.add(ModMth.getRandomElement(randomSource, customer.orders()));
        else {
            var supported = likes.stream().filter(p -> foods.stream().anyMatch(item -> FoodProperties.get(item.getDefaultInstance()).contains(p))).toList();
            MutableComponent names = Component.empty();
            for (int i = 0; i < supported.size(); i++) {
                if (i > 0)
                    names.append("/");
                names.append(Component.translatable(supported.get(i).translateKey()));
            }
            components.add(Component.translatable(REQUEST_FOOD_KEY, names));
        }
        fixedBeverage = generic || customer.beverages().isEmpty();
        if (fixedBeverage) {
            fixedRequiredBeverage = ModMth.getRandomElement(randomSource, drinks);
            components.add(Component.translatable(REQUEST_BEVERAGE_KEY, getItemName(fixedRequiredBeverage)));
        } else {
            var supported = customer.beverages().stream().filter(p -> drinks.stream().anyMatch(item -> BeverageProperties.get(item.getDefaultInstance()).stream().anyMatch(p::is))).toList();
            beverageProperty = ModMth.getRandomElement(randomSource, supported);
            components.add(Component.translatable(REQUEST_BEVERAGE_KEY, Component.translatable(beverageProperty.translateKey())));
        }
        cooldownTickTime = -2;
    }

    private List<Item> menuChoices(RestaurantService.Shop shop, boolean drinks) {
        Customer profile = getCustomer();
        return drinks
                ? CustomerOrderChoice.candidates(shop.items(true), profile.isGeneric(), !profile.beverages().isEmpty(),
                item -> BeverageProperties.get(item.getDefaultInstance()).stream()
                        .anyMatch(property -> profile.beverages().stream().anyMatch(property::is)))
                : CustomerOrderChoice.candidates(shop.items(false), profile.isGeneric(), !profile.likes().isEmpty(),
                item -> FoodProperties.get(item.getDefaultInstance()).stream().anyMatch(profile.likes()::contains));
    }

    public void clearHands() {
        if (this.npc.level() instanceof ServerLevel) {
            this.mealDisplay.clear(this.npc.getInventory());
            this.npc.setLockSlot(false);
        }
    }

    public void resetOrder() {
        this.restaurantOrder = null;
        this.submitFood = IngredientStack.empty();
        this.submitBeverage = IngredientStack.empty();
        this.clearPreparedFood();

        this.fixedFood = false;
        this.fixedRequiredFood = Items.AIR;

        this.likes = new ArrayList<>();
        this.dislikes = new ArrayList<>();

        this.fixedBeverage = false;
        this.fixedRequiredBeverage = Items.AIR;
        this.beverageProperty = BeverageProperties.UNDEFINED;

        this.components = new ArrayList<>();

        this.score = DEFAULT_SCORE;
        this.evaluationFeedback = Optional.empty();
    }

    public Identifier getId() {
        return KEY;
    }

    public void readAdditionalSaveData(
            ValueInput view
    ) {
        this.submitFood = view.read(
                "Customer.SubmitFood",
                IngredientStack.CODEC
        ).orElseGet(IngredientStack::empty);

        this.submitBeverage = view.read(
                "Customer.SubmitBeverage",
                IngredientStack.CODEC
        ).orElseGet(IngredientStack::empty);

        this.timeout = view.getIntOr(
                "Customer.Timeout",
                ReverieDreams.config().maxCustomerTickTime
        );

        this.cooldownTickTime = view.getIntOr(
                "Customer.CooldownTickTime",
                -1
        );

        this.orderBudget = view.getIntOr(
                "Customer.OrderBudget",
                0
        );

        this.fixedFood = view.getBooleanOr(
                "Customer.FixedFood",
                false
        );

        this.fixedRequiredFood = view.read(
                "Customer.FixedRequiredFood",
                BuiltInRegistries.ITEM.byNameCodec()
        ).orElse(Items.AIR);

        this.likes = view.read(
                "Customer.Likes",
                FoodProperty.BY_REGISTRY_CODEC.listOf()
        ).orElseGet(ArrayList::new);

        this.dislikes = view.read(
                "Customer.Dislikes",
                FoodProperty.BY_REGISTRY_CODEC.listOf()
        ).orElseGet(ArrayList::new);

        this.fixedBeverage = view.getBooleanOr(
                "Customer.FixedBeverage",
                false
        );

        this.fixedRequiredBeverage = view.read(
                "Customer.FixedRequiredBeverage",
                BuiltInRegistries.ITEM.byNameCodec()
        ).orElse(Items.AIR);

        this.beverageProperty = view.read(
                "Customer.BeverageProperty",
                BeverageProperty.COMPONENT_CODEC
        ).orElse(BeverageProperties.UNDEFINED);

        this.score = view.getIntOr(
                "Customer.Score",
                DEFAULT_SCORE
        );

        this.components = new ArrayList<>();

        // 修改：读档后恢复当前订单的显示文本
        this.timeout = Math.max(1, this.timeout);
        if (this.cooldownTickTime < -2)
            this.cooldownTickTime = -1;
        this.restaurantOrder = RestaurantTicket.load(view);
        this.visitingBell = view.getLong("Restaurant.VisitPos").map(BlockPos::of).orElse(null);
        this.visitingBellId = RestaurantBlockEntity.uuid(view.getStringOr("Restaurant.VisitId", ""), null);
        this.visitUntil = view.getLongOr("Restaurant.VisitUntil", 0);
        this.meals.load(view);
        this.mealDisplay.load(view);
        this.preparedFoodPlate = view.getLong("Customer.PreparedPlate").map(BlockPos::of).orElse(null);
        String preparedFood = view.getStringOr("Customer.PreparedFood", "");
        this.preparedFoodId = preparedFood.isEmpty() ? null : Identifier.tryParse(preparedFood);
        if (this.preparedFoodPlate == null || this.preparedFoodId == null) {
            this.preparedFoodPlate = null;
            this.preparedFoodId = null;
        }
        if (view.getIntOr("Customer.MealDisplayVersion", 0) == 0 && this.cooldownTickTime >= 0) {
            this.mealDisplay.recoverLegacy(this.npc.getInventory(),
                    this.isFood(this.npc.getMainHandItem()), this.isBeverage(this.npc.getOffhandItem()));
        }
        this.rebuildOrderComponents();
    }

    public void addAdditionalSaveData(
            ValueOutput view
    ) {
        this.mealDisplay.save(view);
        this.meals.save(view);
        if (this.preparedFoodPlate != null && this.preparedFoodId != null) {
            view.putLong("Customer.PreparedPlate", this.preparedFoodPlate.asLong());
            view.putString("Customer.PreparedFood", this.preparedFoodId.toString());
        }
        if (this.restaurantOrder != null)
            this.restaurantOrder.save(view);
        if (visitingBell != null && visitingBellId != null) {
            view.putLong("Restaurant.VisitPos", visitingBell.asLong());
            view.putString("Restaurant.VisitId", visitingBellId.toString());
            view.putLong("Restaurant.VisitUntil", visitUntil);
        }
        view.store(
                "Customer.SubmitFood",
                IngredientStack.CODEC,
                this.submitFood
        );

        view.store(
                "Customer.SubmitBeverage",
                IngredientStack.CODEC,
                this.submitBeverage
        );

        view.putInt(
                "Customer.Timeout",
                this.timeout
        );

        view.putInt(
                "Customer.CooldownTickTime",
                this.cooldownTickTime
        );

        view.putInt(
                "Customer.OrderBudget",
                this.orderBudget
        );

        view.putBoolean(
                "Customer.FixedFood",
                this.fixedFood
        );

        view.store(
                "Customer.FixedRequiredFood",
                BuiltInRegistries.ITEM.byNameCodec(),
                this.fixedRequiredFood
        );

        view.store(
                "Customer.Likes",
                FoodProperty.BY_REGISTRY_CODEC.listOf(),
                this.likes
        );

        view.store(
                "Customer.Dislikes",
                FoodProperty.BY_REGISTRY_CODEC.listOf(),
                this.dislikes
        );

        view.putBoolean(
                "Customer.FixedBeverage",
                this.fixedBeverage
        );

        view.store(
                "Customer.FixedRequiredBeverage",
                BuiltInRegistries.ITEM.byNameCodec(),
                this.fixedRequiredBeverage
        );

        view.store(
                "Customer.BeverageProperty",
                BeverageProperty.COMPONENT_CODEC,
                this.beverageProperty
        );

        view.putInt(
                "Customer.Score",
                this.score
        );
    }

    private void rebuildOrderComponents() {
        this.components = new ArrayList<>();

        if (this.fixedFood) {
            if (!this.fixedRequiredFood.equals(Items.AIR)) {
                this.components.add(
                        Component.translatable(
                                REQUEST_FOOD_KEY,
                                this.getItemName(this.fixedRequiredFood)
                        )
                );
            }
        } else if (!this.likes.isEmpty()) {
            MutableComponent componentList =
                    Component.empty();

            for (int i = 0; i < this.likes.size(); i++) {
                if (i > 0) {
                    componentList.append(
                            Component.literal("/")
                    );
                }

                componentList.append(
                        Component.translatable(
                                this.likes.get(i).translateKey()
                        )
                );
            }

            this.components.add(
                    Component.translatable(
                            REQUEST_FOOD_KEY,
                            componentList
                    )
            );
        }

        if (this.fixedBeverage) {
            if (!this.fixedRequiredBeverage.equals(Items.AIR)) {
                this.components.add(
                        Component.translatable(
                                REQUEST_BEVERAGE_KEY,
                                this.getItemName(
                                        this.fixedRequiredBeverage
                                )
                        )
                );
            }
        } else if (!this.beverageProperty.equals(
                BeverageProperties.UNDEFINED
        )) {
            this.components.add(
                    Component.translatable(
                            REQUEST_BEVERAGE_KEY,
                            Component.translatable(
                                    this.beverageProperty.translateKey()
                            )
                    )
            );
        }
    }

    public void update() {
        this.npc.sendForceSyncPacket();
    }

    public <T> Registry<T> lookupOrThrow(
            ResourceKey<? extends Registry<? extends T>> name
    ) {
        RegistryAccess registryAccess =
                this.npc.registryAccess();

        return registryAccess.lookupOrThrow(name);
    }
}
