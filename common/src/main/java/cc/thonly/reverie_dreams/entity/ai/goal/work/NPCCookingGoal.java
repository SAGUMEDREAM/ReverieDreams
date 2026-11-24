package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.inventory.storage.StorageTransfer;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.ItemComparatorView;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.restaurant.RestaurantScene;
import cc.thonly.reverie_dreams.restaurant.RestaurantSceneCache;
import cc.thonly.reverie_dreams.restaurant.RestaurantService;
import cc.thonly.reverie_dreams.restaurant.RestaurantTicket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Cooks menu dishes for seated preference orders and parks them on plates near a bound counter.
 */
public class NPCCookingGoal extends ScheduledWorkGoal {
    private static final int KITCHEN_SLOTS = 5;
    private static final long COOK_OUTPUT_GRACE_TICKS = 20L * 5;

    private enum Phase {WITHDRAW, COOK, COLLECT, RECOVER, PLACE}

    private NPCSimpleEntity customer;
    private RestaurantTicket order;
    private BlockPos bellPos;
    private UUID bellId;
    private BlockPos kitchen;
    private BlockPos plate;
    private KitchenRecipe recipe;
    private Identifier dishId;
    private BlockPos chestPos;
    private int chestSlot = -1;
    private ItemStack chestSample = ItemStack.EMPTY;
    private Phase phase = Phase.WITHDRAW;
    private long expectedOutputAt;

    public NPCCookingGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.COOKING);
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
        if (scene.counters().isEmpty()) {
            work().status("restaurant_counter_unbound");
            return false;
        }
        List<NPCSimpleEntity> candidates = awaiting(level, scene);
        if (candidates.isEmpty()) {
            work().status("no_orders");
            return false;
        }
        long now = level.getGameTime();
        for (NPCSimpleEntity candidate : candidates) {
            RestaurantTicket ticket = candidate.getCustomerContainer().getRestaurantOrder();
            for (BlockPos pos : scene.plates()) {
                if (!scene.nearCounter(pos) || !work().available(pos, now) || !level.hasChunkAt(pos))
                    continue;
                if (!(level.getBlockEntity(pos) instanceof PlateBlockEntity plateBlock) || !plateBlock.isUnassignedDish(bellId))
                    continue;
                ItemStack stack = plateBlock.getItem().build();
                Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (!stack.is(RDItemTags.CUISINE) || !ticket.allows(id, false))
                    continue;
                if (candidate.getCustomerContainer().attachPreparedFood(pos, id)) {
                    plateBlock.setRestaurantDish(plateBlock.getItem(), bellId, candidate.getUUID());
                }
                break;
            }
        }
        candidates.removeIf(candidate -> candidate.getCustomerContainer().hasPreparedFood());
        if (candidates.isEmpty()) {
            work().status("dish_ready");
            return false;
        }
        BlockPos freePlate = emptyPlate(level, scene, now);
        if (freePlate == null) {
            work().status("waiting_for_plate");
            return false;
        }
        for (NPCSimpleEntity candidate : candidates) {
            Identifier docked = cargoDish(candidate);
            if (docked == null)
                continue;
            select(candidate, freePlate, null, docked);
            phase = Phase.PLACE;
            return true;
        }
        for (BlockPos pos : scene.kitchenware()) {
            if (!work().available(pos, now) || !level.hasChunkAt(pos))
                continue;
            if (!(level.getBlockEntity(pos) instanceof KitchenwareBlockEntity live) || live.isRemoved())
                continue;
            if (!live.getPreOutput().isEmpty())
                continue;
            ItemStack output = live.getItem(KitchenwareBlockEntity.OUTPUT_SLOT);
            if (output.isEmpty() || !output.is(RDItemTags.CUISINE))
                continue;
            Identifier id = BuiltInRegistries.ITEM.getKey(output.getItem());
            NPCSimpleEntity candidate = ordered(candidates, id);
            if (candidate == null)
                continue;
            select(candidate, freePlate, null, id);
            kitchen = pos.immutable();
            phase = Phase.RECOVER;
            return true;
        }
        boolean anyKitchen = false;
        boolean anyEmpty = false;
        for (BlockPos pos : scene.kitchenware()) {
            anyKitchen = true;
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof KitchenwareBlockEntity live && emptyKitchen(live)) {
                anyEmpty = true;
            }
        }
        for (NPCSimpleEntity candidate : candidates) {
            RestaurantTicket ticket = candidate.getCustomerContainer().getRestaurantOrder();
            if (ticket == null)
                continue;
            var orderedFoods = ticket.foods().stream()
                    .filter(id -> RestaurantService.valid(id, false))
                    .sorted(Comparator.comparingInt((Identifier id) ->
                            candidate.getCustomerContainer().foodPreference(RestaurantService.item(id).getDefaultInstance())).reversed())
                    .toList();
            for (Identifier foodId : orderedFoods) {
                Item item = RestaurantService.item(foodId);
                for (BlockPos pos : scene.kitchenware()) {
                    if (!work().available(pos, now) || !level.hasChunkAt(pos))
                        continue;
                    if (!(level.getBlockEntity(pos) instanceof KitchenwareBlockEntity live) || !emptyKitchen(live))
                        continue;
                    KitchenRecipeType.TypeInstance type = live.getTypeInstance();
                    if (type == null)
                        continue;
                    KitchenRecipe match = recipeFor(level, scene, type, item);
                    if (match == null)
                        continue;
                    select(candidate, freePlate, match, foodId);
                    kitchen = pos.immutable();
                    phase = Phase.WITHDRAW;
                    return true;
                }
            }
        }
        if (!anyKitchen)
            work().status("no_kitchenware");
        else if (!anyEmpty)
            work().status("kitchen_busy");
        else
            work().status("need_ingredients");
        return false;
    }

    private List<NPCSimpleEntity> awaiting(ServerLevel level, RestaurantScene scene) {
        List<NPCSimpleEntity> result = new ArrayList<>();
        for (UUID id : scene.customers()) {
            if (!(level.getEntity(id) instanceof NPCSimpleEntity simple))
                continue;
            if (!work().available(id, level.getGameTime()))
                continue;
            NPCCustomerContainer container = simple.getCustomerContainer();
            if (!container.isAutomationEligible() || !bellId.equals(container.getRestaurantOrder().bellId()))
                continue;
            if (container.hasPreparedFood()) {
                BlockPos prepared = container.preparedFoodPlate().orElseThrow();
                Identifier food = container.preparedFoodId().orElseThrow();
                if (!level.hasChunkAt(prepared) || !work().available(prepared, level.getGameTime()))
                    continue;
                if (level.getBlockEntity(prepared) instanceof PlateBlockEntity plateBlock
                        && plateBlock.canTakeDish(bellId, id) && !plateBlock.getItem().isEmpty()
                        && plateBlock.getItem().is(RestaurantService.item(food)))
                    continue;
                container.clearPreparedFood();
            }
            result.add(simple);
        }
        result.sort(Comparator.comparingDouble(simple -> simple.distanceToSqr(npc)));
        return result;
    }

    private NPCSimpleEntity ordered(List<NPCSimpleEntity> candidates, Identifier dish) {
        for (NPCSimpleEntity candidate : candidates) {
            RestaurantTicket ticket = candidate.getCustomerContainer().getRestaurantOrder();
            if (ticket != null && ticket.allows(dish, false))
                return candidate;
        }
        return null;
    }

    private Identifier cargoDish(NPCSimpleEntity candidate) {
        RestaurantTicket ticket = candidate.getCustomerContainer().getRestaurantOrder();
        if (ticket == null)
            return null;
        for (int slot = 0; slot < NPCWorkInventory.CARGO_SIZE; slot++) {
            ItemStack stack = npc.getInventory().getItem(slot);
            if (stack.isEmpty() || !stack.is(RDItemTags.CUISINE))
                continue;
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (ticket.allows(id, false))
                return id;
        }
        return null;
    }

    private BlockPos emptyPlate(ServerLevel level, RestaurantScene scene, long now) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : scene.plates()) {
            if (!scene.nearCounter(pos) || !work().available(pos, now) || !level.hasChunkAt(pos))
                continue;
            if (!(level.getBlockEntity(pos) instanceof PlateBlockEntity plateBlock) || !plateBlock.getItem().isEmpty())
                continue;
            double distance = pos.distToCenterSqr(npc.position());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

    private boolean emptyKitchen(KitchenwareBlockEntity live) {
        if (!live.getPreOutput().isEmpty())
            return false;
        if (!live.getItem(KitchenwareBlockEntity.OUTPUT_SLOT).isEmpty())
            return false;
        for (int slot = 0; slot < KITCHEN_SLOTS; slot++) {
            if (!live.getItem(slot).isEmpty())
                return false;
        }
        return true;
    }

    private KitchenRecipe recipeFor(ServerLevel level, RestaurantScene scene, KitchenRecipeType.TypeInstance type, Item output) {
        KitchenRecipeType kitchen = KitchenRecipeType.getInstance();
        if (kitchen == null)
            return null;
        for (KitchenRecipe recipe : kitchen.getRecipeView(type).values()) {
            List<IngredientStack> ingredients = recipe.getIngredients();
            if (ingredients.isEmpty() || ingredients.size() > KITCHEN_SLOTS)
                continue;
            if (recipe.getOutput().getItem() != output)
                continue;
            boolean single = true;
            for (IngredientStack ingredient : ingredients) {
                if (ingredient.getCount() != 1) {
                    single = false;
                    break;
                }
            }
            if (single && supplied(level, scene, recipe))
                return recipe;
        }
        return null;
    }

    private boolean supplied(ServerLevel level, RestaurantScene scene, KitchenRecipe recipe) {
        List<ItemStack> pool = cargoPool();
        long now = level.getGameTime();
        for (BlockPos pos : scene.silverChests()) {
            if (!work().available(pos, now) || !level.hasChunkAt(pos)
                    || !(level.getBlockEntity(pos) instanceof CustomChestBlockEntity chest))
                continue;
            appendPool(pool, chest);
        }
        return consume(pool, recipe);
    }

    private boolean cargoSupplied(KitchenRecipe recipe) {
        return consume(cargoPool(), recipe);
    }

    private IngredientStack missingIngredient(KitchenRecipe recipe) {
        List<ItemStack> pool = cargoPool();
        for (IngredientStack ingredient : recipe.getIngredients()) {
            if (!take(pool, ingredient))
                return ingredient;
        }
        return null;
    }

    private boolean consume(List<ItemStack> pool, KitchenRecipe recipe) {
        for (IngredientStack ingredient : recipe.getIngredients()) {
            if (!take(pool, ingredient))
                return false;
        }
        return true;
    }

    private boolean take(List<ItemStack> pool, IngredientStack ingredient) {
        for (ItemStack stack : pool) {
            if (!stack.isEmpty() && ItemComparatorView.of(ingredient).test(stack)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private List<ItemStack> cargoPool() {
        List<ItemStack> pool = new ArrayList<>();
        for (int slot = 0; slot < NPCWorkInventory.CARGO_SIZE; slot++) {
            ItemStack stack = npc.getInventory().getItem(slot);
            if (!stack.isEmpty())
                pool.add(stack.copy());
        }
        return pool;
    }

    private void appendPool(List<ItemStack> pool, net.minecraft.world.Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty())
                pool.add(stack.copy());
        }
    }

    private boolean locateChest(ServerLevel level, RestaurantScene scene, IngredientStack ingredient, long now) {
        for (BlockPos pos : scene.silverChests()) {
            if (!work().available(pos, now) || !level.hasChunkAt(pos))
                continue;
            if (!(level.getBlockEntity(pos) instanceof CustomChestBlockEntity chest))
                continue;
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                ItemStack stack = chest.getItem(slot);
                if (stack.isEmpty() || !ItemComparatorView.of(ingredient).test(stack))
                    continue;
                chestPos = pos.immutable();
                chestSlot = slot;
                chestSample = stack.copy();
                return true;
            }
        }
        return false;
    }

    private void select(NPCSimpleEntity customer, BlockPos plate, KitchenRecipe recipe, Identifier dishId) {
        this.customer = customer;
        this.order = customer.getCustomerContainer().getRestaurantOrder();
        this.plate = plate;
        this.recipe = recipe;
        this.dishId = dishId;
        this.chestPos = null;
        this.chestSlot = -1;
        this.chestSample = ItemStack.EMPTY;
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        if (bellPos == null || !level.hasChunkAt(bellPos))
            return false;
        if (!(level.getBlockEntity(bellPos) instanceof RestaurantBlockEntity bell) || !bell.id().equals(bellId))
            return false;
        if (customer == null || !customer.isAlive() || customer.level() != level)
            return false;
        var container = customer.getCustomerContainer();
        if (container.getRestaurantOrder() != order || !container.isSeatedFor(bell) || !container.awaitingAutomationFood())
            return false;
        if (plate == null || !level.hasChunkAt(plate) || !(level.getBlockEntity(plate) instanceof PlateBlockEntity))
            return false;
        if (kitchen != null && (!level.hasChunkAt(kitchen) || !(level.getBlockEntity(kitchen) instanceof KitchenwareBlockEntity)))
            return false;
        return true;
    }

    @Override
    protected List<?> resources() {
        List<Object> resources = new ArrayList<>(3);
        if (customer != null)
            resources.add(customer.getUUID());
        if (kitchen != null)
            resources.add(kitchen);
        if (plate != null)
            resources.add(plate);
        return resources;
    }

    @Override
    protected void clearTask() {
        customer = null;
        order = null;
        bellPos = null;
        bellId = null;
        kitchen = null;
        plate = null;
        recipe = null;
        dishId = null;
        chestPos = null;
        chestSlot = -1;
        chestSample = ItemStack.EMPTY;
        phase = Phase.WITHDRAW;
        expectedOutputAt = 0;
    }

    @Override
    protected void runTask(ServerLevel level) {
        switch (phase) {
            case WITHDRAW -> withdraw(level);
            case COOK -> cook(level);
            case COLLECT -> collect(level);
            case RECOVER -> recover(level);
            case PLACE -> place(level);
        }
    }

    private void withdraw(ServerLevel level) {
        IngredientStack missing = missingIngredient(recipe);
        if (missing == null) {
            phase = Phase.COOK;
            return;
        }
        long now = level.getGameTime();
        if (chestPos == null) {
            RestaurantScene scene = scene(level);
            if (scene == null || !locateChest(level, scene, missing, now)) {
                finish("need_ingredients", 40);
                return;
            }
        }
        if (!reachInteraction(chestPos, 2.5))
            return;
        if (!(level.getBlockEntity(chestPos) instanceof CustomChestBlockEntity chest) || chestSlot < 0) {
            finish("need_ingredients", 20);
            return;
        }
        ItemStack current = chest.getItem(chestSlot);
        if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, chestSample)) {
            finish("need_ingredients", 20);
            return;
        }
        int moved = StorageTransfer.move(chest, chestSlot, chestSample, npc.getInventory(), NPCWorkInventory.CARGO_SIZE, 1);
        chestPos = null;
        chestSlot = -1;
        chestSample = ItemStack.EMPTY;
        if (moved > 0) {
            npc.swing(InteractionHand.MAIN_HAND);
            work().action();
        } else {
            finish("inventory_full", 20);
        }
    }

    private void cook(ServerLevel level) {
        if (!reachInteraction(kitchen, 2.5))
            return;
        if (!(level.getBlockEntity(kitchen) instanceof KitchenwareBlockEntity live) || live.isRemoved() || !emptyKitchen(live)) {
            finish("kitchen_busy", 20);
            return;
        }
        KitchenRecipeType.TypeInstance type = live.getTypeInstance();
        if (type == null || recipe.getTypeInstance() == null || !type.is(recipe.getTypeInstance())) {
            finish("kitchen_busy", 20);
            return;
        }
        if (!cargoSupplied(recipe)) {
            phase = Phase.WITHDRAW;
            return;
        }
        int kitchenSlot = 0;
        for (IngredientStack ingredient : recipe.getIngredients()) {
            int cargoSlot = NPCWorkInventory.find(npc.getInventory(), stack -> !stack.isEmpty() && ItemComparatorView.of(ingredient).test(stack));
            if (cargoSlot < 0) {
                phase = Phase.WITHDRAW;
                return;
            }
            while (kitchenSlot < KITCHEN_SLOTS && !live.getItem(kitchenSlot).isEmpty())
                kitchenSlot++;
            if (kitchenSlot >= KITCHEN_SLOTS) {
                finish("kitchen_busy", 20);
                return;
            }
            ItemStack source = npc.getInventory().getItem(cargoSlot);
            live.setItem(kitchenSlot, source.copyWithCount(1));
            npc.getInventory().setItem(cargoSlot, source.getCount() > 1 ? source.copyWithCount(source.getCount() - 1) : ItemStack.EMPTY);
            kitchenSlot++;
        }
        live.consume(recipe.getIngredients());
        live.setOutput(recipe.getOutput(), recipe.getCostTime() * 20.0 + 20 * 0.25 * KITCHEN_SLOTS);
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        expectedOutputAt = level.getGameTime() + (long) Math.ceil(live.getTickLeft());
        phase = Phase.COLLECT;
    }

    private void collect(ServerLevel level) {
        if (!reachInteraction(kitchen, 2.5))
            return;
        if (!(level.getBlockEntity(kitchen) instanceof KitchenwareBlockEntity live) || live.isRemoved()) {
            finish("no_kitchenware", 20);
            return;
        }
        if (!live.getPreOutput().isEmpty()) {
            work().status("cooking");
            return;
        }
        ItemStack output = live.getItem(KitchenwareBlockEntity.OUTPUT_SLOT);
        if (output.isEmpty()) {
            if (level.getGameTime() - expectedOutputAt > COOK_OUTPUT_GRACE_TICKS) {
                finish("cook_failed", 20);
                return;
            }
            work().status("cooking");
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(output.getItem());
        if (!dishId.equals(id)) {
            finish("cook_failed", 40);
            return;
        }
        int moved = StorageTransfer.move(live, KitchenwareBlockEntity.OUTPUT_SLOT, output, npc.getInventory(), NPCWorkInventory.CARGO_SIZE, 1);
        if (moved <= 0) {
            finish("inventory_full", 20);
            return;
        }
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        phase = Phase.PLACE;
    }

    private void recover(ServerLevel level) {
        if (!reachInteraction(kitchen, 2.5))
            return;
        if (!(level.getBlockEntity(kitchen) instanceof KitchenwareBlockEntity live) || live.isRemoved()) {
            finish("no_output", 20);
            return;
        }
        if (!live.getPreOutput().isEmpty()) {
            work().status("cooking");
            return;
        }
        ItemStack output = live.getItem(KitchenwareBlockEntity.OUTPUT_SLOT);
        if (output.isEmpty() || !output.is(RDItemTags.CUISINE)) {
            finish("no_output", 20);
            return;
        }
        NPCCustomerContainer container = customer.getCustomerContainer();
        RestaurantTicket ticket = container.getRestaurantOrder();
        Identifier id = BuiltInRegistries.ITEM.getKey(output.getItem());
        if (!container.isAutomationEligible() || ticket == null || !ticket.allows(id, false)) {
            finish("customer_left", 20);
            return;
        }
        int moved = StorageTransfer.move(live, KitchenwareBlockEntity.OUTPUT_SLOT, output, npc.getInventory(), NPCWorkInventory.CARGO_SIZE, 1);
        if (moved <= 0) {
            finish("inventory_full", 20);
            return;
        }
        dishId = id;
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        phase = Phase.PLACE;
    }

    private void place(ServerLevel level) {
        NPCCustomerContainer container = customer.getCustomerContainer();
        if (!container.isAutomationEligible()) {
            finish("customer_left", 20);
            return;
        }
        if (!reachInteraction(plate, 2.5))
            return;
        if (!(level.getBlockEntity(plate) instanceof PlateBlockEntity plateBlock)) {
            finish("waiting_for_plate", 20);
            return;
        }
        if (!plateBlock.getItem().isEmpty()) {
            RestaurantScene scene = scene(level);
            BlockPos next = scene == null ? null : emptyPlate(level, scene, level.getGameTime());
            if (next == null) {
                finish("waiting_for_plate", 40);
                return;
            }
            plate = next;
            return;
        }
        int cargoSlot = NPCWorkInventory.find(npc.getInventory(), stack -> !stack.isEmpty()
                && stack.is(RDItemTags.CUISINE)
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(dishId));
        if (cargoSlot < 0) {
            finish("cook_failed", 20);
            return;
        }
        ItemStack dish = npc.getInventory().getItem(cargoSlot);
        ItemStack one = dish.copyWithCount(1);
        if (!container.attachPreparedFood(plate, dishId)) {
            finish("customer_left", 20);
            return;
        }
        plateBlock.setRestaurantDish(IngredientStack.of(one), bellId, customer.getUUID());
        npc.getInventory().setItem(cargoSlot, dish.getCount() > 1 ? dish.copyWithCount(dish.getCount() - 1) : ItemStack.EMPTY);
        npc.swing(InteractionHand.MAIN_HAND);
        work().action();
        finish("dish_ready", 10);
    }

    private RestaurantScene scene(ServerLevel level) {
        if (bellPos == null || !level.hasChunkAt(bellPos))
            return null;
        return level.getBlockEntity(bellPos) instanceof RestaurantBlockEntity bell ? RestaurantSceneCache.get(level, bell) : null;
    }
}
