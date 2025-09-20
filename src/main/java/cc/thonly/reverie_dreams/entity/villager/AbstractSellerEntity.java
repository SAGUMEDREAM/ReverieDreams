package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import eu.pb4.sgui.api.gui.MerchantGui;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;

import java.util.*;

@Slf4j
@Getter
public abstract class AbstractSellerEntity extends WanderingTraderEntity {
    public static final int MAX_LEVEL = 5;
    public static final int[] EXPS = {50, 100, 150, 200, 250};
    private static final Gson GSON = new Gson();
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.DOORS_TO_CLOSE);
    protected final Set<SellerGui> sessions = new HashSet<>();
    protected VillagerData prev;
    protected SellInfo sellInfo = new SellInfo(new Object2ObjectOpenHashMap<>());
    protected int level = 0;
    protected int exp = 0;

    public AbstractSellerEntity(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanSwim(true);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
//        this.goalSelector.add(0, new HoldInHandsGoal<WanderingTraderEntity>(this, PotionContentsComponent.createStack(Items.POTION, Potions.INVISIBILITY), SoundEvents.ENTITY_WANDERING_TRADER_DISAPPEARED, wanderingTrader -> this.getWorld().isNight() && !wanderingTrader.isInvisible()));
//        this.goalSelector.add(0, new HoldInHandsGoal<WanderingTraderEntity>(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.ENTITY_WANDERING_TRADER_REAPPEARED, wanderingTrader -> this.getWorld().isDay() && wanderingTrader.isInvisible()));
        this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(1, new FleeEntityGoal<ZombieEntity>(this, ZombieEntity.class, 8.0f, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<EvokerEntity>(this, EvokerEntity.class, 12.0f, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<VindicatorEntity>(this, VindicatorEntity.class, 8.0f, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<VexEntity>(this, VexEntity.class, 8.0f, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<PillagerEntity>(this, PillagerEntity.class, 15.0f, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<IllusionerEntity>(this, IllusionerEntity.class, 12.0f, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<ZoglinEntity>(this, ZoglinEntity.class, 10.0f, 0.5, 0.5));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 0.5));
        this.goalSelector.add(1, new LookAtCustomerGoal(this));
        this.goalSelector.add(2, new WanderToTargetGoal(this, 2.0, 0.35));
        this.goalSelector.add(4, new GoToWalkTargetGoal(this, 0.35));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.35));
        this.goalSelector.add(9, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0f, 1.0f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0f));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level > MAX_LEVEL) {
            this.level = MAX_LEVEL;
        }
    }

    public void trade(ItemStackWrapper wrapper) {
        World world = this.getWorld();
        Random random = new Random();
        this.exp += random.nextInt(9, 25);
        this.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.sellInfo.sell(this.getVillagerSeed(), wrapper);
        this.tryLevelUp();
    }

    public void tryLevelUp() {
        while (this.level < MAX_LEVEL && this.exp >= EXPS[this.level]) {
            this.exp -= EXPS[this.level];
            this.level++;

            this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP);
        }

        if (this.level >= MAX_LEVEL) {
            this.level = MAX_LEVEL;
            this.exp = Math.min(this.exp, EXPS[MAX_LEVEL - 1]);
        }
    }

    public abstract VillagerData getModifyVillagerData(MinecraftServer server);

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        World baseWorld = player.getWorld();
        ItemStack stack = player.getStackInHand(hand);
        if (!baseWorld.isClient() && baseWorld instanceof ServerWorld world) {
            if (stack.getItem() instanceof ShovelItem && player.isSneaking() && this.canReset()) {
                boolean canceled = this.cancel();
                if (canceled && !player.isInCreativeMode()) {
                    stack.damage(1, player);
                }
                player.swingHand(hand);
                return ActionResult.SUCCESS_SERVER;
            }
            if (this.sessions.isEmpty()) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, player.getPos().add(0, 1, 0));
                this.getNavigation().stop();

                SellerGui sellerGui = new SellerGui(serverPlayer, this);
                sellerGui.open();
                player.swingHand(hand);
                return ActionResult.SUCCESS_SERVER;
            }
        }
        return ActionResult.SUCCESS;
    }

    public static DefaultAttributeContainer.Builder createLivingAttributes() {
        return MobEntity.createMobAttributes();
    }

    @Override
    public void tickMovement() {
        if (!this.getWorld().isClient() && !this.sessions.isEmpty()) {
            return;
        }
        super.tickMovement();
    }

    public boolean cancel() {
        World world = this.getWorld();
        if (this.prev != null) {
            VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, this.getWorld());
            villager.setVillagerData(this.prev);
            villager.setPos(this.getX(), this.getY(), this.getZ());
            this.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN);
            this.discard();
            world.spawnEntity(villager);
            return true;
        }
        return false;
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        Optional<String> prevVillagerData = view.getOptionalString("PrevVillagerData");
        if (prevVillagerData.isPresent()) {
            String jsonString = prevVillagerData.get();
            JsonElement element = JsonParser.parseString(jsonString);
            Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, element);
            DataResult<VillagerData> parse = VillagerData.CODEC.parse(input);
            Optional<VillagerData> result = parse.result();
            result.ifPresent((data) -> this.prev = data);
        }
        this.level = view.getInt("Level",0);
        Optional<String> sellInfoData = view.getOptionalString("SellInfoData");
        if (sellInfoData.isPresent()) {
            String jsonString = sellInfoData.get();
            JsonElement element = JsonParser.parseString(jsonString);
            Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, element);
            DataResult<SellInfo> parse = SellInfo.CODEC.parse(input);
            Optional<SellInfo> result = parse.result();
            result.ifPresent((data) -> this.sellInfo = data);
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (this.prev != null) {
            DataResult<JsonElement> dataResult = VillagerData.CODEC.encodeStart(JsonOps.INSTANCE, this.prev);
            Optional<JsonElement> result = dataResult.result();
            if (result.isPresent()) {
                JsonElement element = result.get();
                view.putString("PrevVillagerData", GSON.toJson(element));
            }
        }
        view.putInt("Level", this.level);
        if (this.sellInfo != null) {
            DataResult<JsonElement> dataResult = SellInfo.CODEC.encodeStart(JsonOps.INSTANCE, this.sellInfo);
            Optional<JsonElement> result = dataResult.result();
            if (result.isPresent()) {
                JsonElement element = result.get();
                view.putString("SellInfoData", GSON.toJson(element));
            }
        }
    }

    public abstract List<TradeOffer> getVillagerOffers();

    public long getVillagerSeed() {
        UUID uuid = this.getUuid();
        World world = this.getWorld();
        long day = world.getTimeOfDay() / 24000L;
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        return mostSigBits + leastSigBits + day;
    }

    @Getter
    public static class SellerGui extends MerchantGui {
        private final AbstractSellerEntity self;

        public SellerGui(ServerPlayerEntity player, AbstractSellerEntity self) {
            super(player, false);
            this.self = self;
            this.init();
        }

        public void init() {
            this.setTitle(this.self.getName());
            List<TradeOffer> villagerOffers = this.self.getVillagerOffers();
            for (TradeOffer offer : villagerOffers) {
                this.addTrade(offer);
            }
            if (villagerOffers.isEmpty()) {
                this.self.discard();
                this.close();
            }
        }

        @Override
        public boolean onTrade(TradeOffer offer) {
            TradeOffer copied = offer.copy();
            this.self.trade(ItemStackWrapper.of(copied.copySellItem()));
            this.self.trade(copied);
            return super.onTrade(offer);
        }

        @Override
        public void onOpen() {
            super.onOpen();
            this.self.getSessions().add(this);
        }

        @Override
        public void onClose() {
            super.onClose();
            this.self.getSessions().remove(this);
        }
    }

    public abstract boolean canReset();
}
