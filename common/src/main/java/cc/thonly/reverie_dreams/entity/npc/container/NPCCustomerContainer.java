package cc.thonly.reverie_dreams.entity.npc.container;


import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.*;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.entity.npc.NPCInteractResult;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
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
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    int timeout = ReverieDreams.config().maxCustomerTickTime;
    int cooldownTickTime = -1;

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

    private void spawnCoinsToPlayer(Player player, int price) {
        if (price <= 0) {
            return;
        }

        if (!(this.npc.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        List<ItemStack> itemStacks = ItemUtils.calculateCoins(price);

        for (ItemStack itemStack : itemStacks) {
            if (itemStack.isEmpty()) {
                continue;
            }

            // NPC 位置
            double x = this.npc.getX();
            double y = this.npc.getEyeY();
            double z = this.npc.getZ();

            ItemEntity entity = new ItemEntity(
                    serverLevel,
                    x,
                    y,
                    z,
                    itemStack
            );

            // NPC -> 玩家
            double dx = player.getX() - x;
            double dy = (player.getY() + player.getBbHeight() * 0.5) - y;
            double dz = player.getZ() - z;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > 0.001) {
                dx /= distance;
                dy /= distance;
                dz /= distance;
            }

            // 非常轻的抛出速度
            double velocity = 0.23;
            entity.setPickUpDelay(40);
            entity.setDeltaMovement(
                    dx * velocity,
                    dy * velocity + 0.04,
                    dz * velocity
            );

            serverLevel.addFreshEntity(entity);
        }
    }

    public void triggerSuccess(ServerPlayer player) {
        int price = this.priceOutput;
        this.spawnCoinsToPlayer(player, price);
        SoundEventPlayUtils.playSound(player, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        FavorabilityContainer favorabilityContainer = this.npc.getFavorabilityContainer();
        favorabilityContainer.add(player.getUUID(), (double) this.score / (this.randomSource.nextBoolean() ? 2 : 4));
        if (ReverieDreams.config().debugMode) {
            player.sendSystemMessage(
                    Component.literal("§c调试信息：成功 分数：%s".formatted(this.score))
            );
        }
    }

    public void triggerTooExpensive(ServerPlayer player) {
        int price = this.priceOutput;
        this.spawnCoinsToPlayer(player, price);
        SoundEventPlayUtils.playSound(player, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
        FavorabilityContainer favorabilityContainer = this.npc.getFavorabilityContainer();
        favorabilityContainer.add(player.getUUID(), -((double) this.score / (this.randomSource.nextBoolean() ? 2 : 4)));
        if (ReverieDreams.config().debugMode) {
            player.sendSystemMessage(
                    Component.literal("失败/昂贵 分数：%s/%s".formatted(this.score, this.priceOutput))
            );
        }
    }

    public void triggerAngry(ServerPlayer player) {
        int price = (int) (this.priceOutput / 3);
        this.spawnCoinsToPlayer(player, price);
        SoundEventPlayUtils.playSound(player, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 0.65f);
        FavorabilityContainer favorabilityContainer = this.npc.getFavorabilityContainer();
        favorabilityContainer.add(player.getUUID(), -((double) this.score / (this.randomSource.nextBoolean() ? 3 : 6)));
        if (ReverieDreams.config().debugMode) {
            player.sendSystemMessage(
                    Component.literal("失败 分数：%s".formatted(this.score))
            );
        }
    }

    public void tick() {
        if (!this.isCustomerMode()) {
            this.npc.setLockSlot(false);
            return;
        }

        if (this.cooldownTickTime == -1) {
            this.clearHands();
            this.nextOrder();
            this.cooldownTickTime = -2;
            return;
        }

        if (this.cooldownTickTime >= 0) {
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
                this.clearHands();
                this.nextOrder();
                this.cooldownTickTime = -2;
            }

            return;
        }

        this.timeout--;

        if (this.timeout <= 0) {
            this.clearHands();
            this.nextOrder();
            this.timeout = ReverieDreams.config().maxCustomerTickTime;
            this.cooldownTickTime = -2;
        }
    }

    private ParticleOptions getParticle(boolean isMainHand) {
        ItemStack item = isMainHand ? this.npc.getMainHandItem() : this.npc.getOffhandItem();
        return item.isEmpty()
                ? ParticleTypes.ITEM_SNOWBALL
                : new ItemParticleOption(ParticleTypes.ITEM, item);
    }

    public boolean isCustomerMode() {
        if (!this.npc.getNpcState().equals(NPCStates.WORKING)) {
            return false;
        }
        if (!this.npc.getWorkMode().equals(NPCWorkModes.CUSTOMER)) {
            return false;
        }
        return true;
    }

    public void insertStackToHand(
            InteractionHand hand,
            ItemStack stack
    ) {
        if (!(this.npc.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        NPCInventoryImpl inventory = this.npc.getInventory();
        boolean remained = false;
        if (!stack.isEmpty()) {
            ItemStack oldStack = inventory.getHand(hand);
            if (!oldStack.isEmpty()) {
                ItemStack remaining = oldStack.copy();
                inventory.insertStack(remaining);

                if (!remaining.isEmpty()) {
                    this.npc.spawnAtLocation(serverLevel, remaining);
                }
            }
        } else {
            ItemStack oldStack = inventory.getHand(hand);
            if (oldStack.getCount() > 1) {
                oldStack.shrink(1);
                remained = true;
            }
        }
        if (remained) {
            return;
        }
        inventory.setHand(hand, stack);
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
            player.sendSystemMessage(Component.translatable(COOLDOWN_KEY));
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

    public void processSubmitOrder(ServerPlayer player) {
        if (!this.isSubmitFullStack()) {
            return;
        }

        this.score = DEFAULT_SCORE;

        if (this.fixedFood) {
            if (this.submitFood.is(this.fixedRequiredFood)) {
                this.score += 5;
            } else {
                this.score -= 5;
            }
        }

        Collection<FoodProperty> foodProperties =
                FoodProperties.get(this.submitFood);

        for (FoodProperty foodProperty : foodProperties) {
            if (this.likes.contains(foodProperty)) {
                this.score += 1;
            }

            if (this.dislikes.contains(foodProperty)) {
                this.score -= 2;
            }
        }

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

        CustomerBudget budgetInstance = this.getCustomer().budget();
        int max = budgetInstance.max();

        int overBudget = Math.max(0, budget - max);
        boolean tooExpensive = overBudget > 0;

        this.score -= (int) (overBudget * 0.5);
        this.score = Math.max(
                MIN_SCORE,
                Math.min(this.score, MAX_SCORE)
        );

        this.priceOutput = Math.min(budget, max);

        IngredientStack food = this.submitFood.copy();
        IngredientStack beverage = this.submitBeverage.copy();
        Runnable action = () -> {
            food.build().finishUsingItem(this.npc.level(), this.npc);
            beverage.build().finishUsingItem(this.npc.level(), this.npc);
        };
        action.run();

        if (this.score >= 0) {
            this.triggerSuccess(player);
        } else {
            if (tooExpensive) {
                this.triggerTooExpensive(player);
            } else {
                this.triggerAngry(player);
            }
        }
        player.giveExperiencePoints(this.randomSource.nextInt(2, 16));
        SimpleTriggerFactory.create(SimpleTriggerKeys.WAITER).trigger(player);

        CustomerEvaluation evaluation = this.getCustomer().evaluation();
        this.evaluationFeedback = evaluation.getEvaluationFeedback(this.score, overBudget);
        if (this.evaluationFeedback.isPresent()) {
            String msg = this.evaluationFeedback.get();
            this.tell(player, Component.translatable(msg));
        } else {
            CustomerEvaluation randomEvaluation = CustomerEvaluation.getRandomEvaluation(this.randomSource);
            this.evaluationFeedback = randomEvaluation.getEvaluationFeedback(this.score, overBudget);
            this.evaluationFeedback.ifPresent(msg -> this.tell(player, Component.translatable(msg)));
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

        this.cooldownTickTime = ReverieDreams.config().maxCustomerCooldownTickTime;
    }

    public void tell(ServerPlayer player, Component component) {
        MutableComponent body = Component.empty()
                .append(this.npc.getName())
                .append(": ")
                .append(component);

        player.sendSystemMessage(body);
    }

    public void tellOrder(ServerPlayer player) {
        for (Component line : this.components) {
            this.tell(player, line);
        }
    }

    public Customer getCustomer() {
        RoleType roleType = this.npc.getRoleType();
        return roleType.getCustomer();
    }

    private boolean canSubmitOrder() {
        return this.isCustomerMode()
                && !this.isEatingCooldown()
                && this.npc.isPassenger();
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

    public boolean isSubmitFullStack() {
        return !this.submitFood.isEmpty()
                && !this.submitBeverage.isEmpty();
    }

    private Component getItemName(Item item) {
        return item.getDefaultInstance().getHoverName();
    }

    public void nextOrder() {
        // 修改：新订单开始前统一清理上一单手持物品
        this.clearHands();
        this.resetOrder();

        Customer customer = this.getCustomer();

        List<FoodProperty> likes = customer.likes();
        List<FoodProperty> dislikes = customer.dislikes();
        List<BeverageProperty> beverages = customer.beverages();

        if (likes.isEmpty()) {
            this.fixedFood = true;

            List<Holder<Item>> list = ModMth.toList(
                    BuiltInRegistries.ITEM.getTagOrEmpty(RDItemTags.CUISINE)
            );

            if (!list.isEmpty()) {
                this.fixedRequiredFood =
                        ModMth.getRandomElement(
                                this.randomSource,
                                list
                        ).value();
            } else {
                this.fixedRequiredFood =
                        RDCuisineItems.SEAFOOD_MISO_SOUP.asItem();
            }

            this.components.add(
                    Component.translatable(
                            REQUEST_FOOD_KEY,
                            this.getItemName(this.fixedRequiredFood)
                    )
            );
        } else {
            this.fixedFood = false;
            this.likes = new ArrayList<>(likes);
            this.dislikes = new ArrayList<>(dislikes);

            List<Component> orders = customer.orders();

            if (!orders.isEmpty()) {
                Component element =
                        ModMth.getRandomElement(
                                this.randomSource,
                                orders
                        );

                this.components.add(element);
            } else {
                MutableComponent componentList =
                        Component.empty();

                for (int i = 0; i < likes.size(); i++) {
                    if (!(i == 0 || i == likes.size() - 1)) {
                        componentList.append(
                                Component.literal("/")
                        );
                    }

                    componentList.append(
                            Component.translatable(
                                    likes.get(i).translateKey()
                            )
                    );
                }

                MutableComponent component =
                        Component.translatable(
                                REQUEST_FOOD_KEY,
                                componentList
                        );

                this.components.add(component);
            }
        }

        if (beverages.isEmpty()) {
            this.fixedBeverage = true;

            List<Holder<Item>> list = ModMth.toList(
                    BuiltInRegistries.ITEM.getTagOrEmpty(RDItemTags.BEVERAGE)
            );

            if (!list.isEmpty()) {
                this.fixedRequiredBeverage =
                        ModMth.getRandomElement(
                                this.randomSource,
                                list
                        ).value();
            } else {
                this.fixedRequiredBeverage =
                        RDBeverageItems.GREEN_TEA.asItem();
            }

            this.components.add(
                    Component.translatable(
                            REQUEST_BEVERAGE_KEY,
                            this.getItemName(this.fixedRequiredBeverage)
                    )
            );
        } else {
            this.fixedBeverage = false;

            this.beverageProperty =
                    ModMth.getRandomElement(
                            this.randomSource,
                            beverages
                    );

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

    public void clearHands() {
        this.insertStackToHand(
                InteractionHand.MAIN_HAND,
                ItemStack.EMPTY
        );

        this.insertStackToHand(
                InteractionHand.OFF_HAND,
                ItemStack.EMPTY
        );

        this.npc.setLockSlot(false);
    }

    public void resetOrder() {
        this.submitFood = IngredientStack.empty();
        this.submitBeverage = IngredientStack.empty();

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
        this.rebuildOrderComponents();
    }

    public void addAdditionalSaveData(
            ValueOutput view
    ) {
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