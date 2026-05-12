package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.google.common.collect.ImmutableList;
import eu.pb4.sgui.api.gui.MerchantGui;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.BalmSafeClientAccess;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("resource")
@Slf4j
@Getter
public abstract class AbstractSeller extends WanderingTrader {
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(AbstractSeller.class, EntityDataSerializers.VILLAGER_DATA);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.DOORS_TO_CLOSE);
    public static final int MAX_LEVEL = 5;
    public static final int[] EXPS = {50, 100, 150, 200, 250};
    @Setter
    @Getter
    private @Nullable VillagerData villagerDataCache = null;
    protected VillagerData prev;
    protected final Set<SellerGui> sessions = new HashSet<>();
    protected SellInfo sellInfo = new SellInfo(new Object2ObjectOpenHashMap<>());
    protected int level = 0;
    protected int exp = 0;

    public AbstractSeller(EntityType<? extends WanderingTrader> entityType, Level world) {
        super(entityType, world);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VILLAGER_DATA, Villager.createDefaultVillagerData());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
//        this.goalSelector.add(0, new HoldInHandsGoal<WanderingTraderEntity>(this, PotionContentsComponent.createStack(Items.POTION, Potions.INVISIBILITY), SoundEvents.ENTITY_WANDERING_TRADER_DISAPPEARED, wanderingTrader -> this.getWorld().isNight() && !wanderingTrader.isInvisible()));
//        this.goalSelector.add(0, new HoldInHandsGoal<WanderingTraderEntity>(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.ENTITY_WANDERING_TRADER_REAPPEARED, wanderingTrader -> this.getWorld().isDay() && wanderingTrader.isInvisible()));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Zombie>(this, Zombie.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Evoker>(this, Evoker.class, 12.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Vindicator>(this, Vindicator.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Vex>(this, Vex.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Pillager>(this, Pillager.class, 15.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Illusioner>(this, Illusioner.class, 12.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Zoglin>(this, Zoglin.class, 10.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WanderToPositionGoal(this, 2.0, 0.35));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
    }

    @Deprecated
    public void setVillagerData(VillagerData p_479728_) {
        VillagerData villagerdata = this.getVillagerData();
        if (!villagerdata.profession().equals(p_479728_.profession())) {
            this.offers = null;
        }

        this.entityData.set(DATA_VILLAGER_DATA, p_479728_);
    }

    @Deprecated
    public VillagerData getVillagerData() {
        return this.entityData.get(DATA_VILLAGER_DATA);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level > MAX_LEVEL) {
            this.level = MAX_LEVEL;
        }
    }

    public void trade(IngredientStack wrapper) {
        Level world = this.level();
        this.exp += ReverieDreams.RD.nextInt(9, 25);
        this.makeSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
        this.sellInfo.sell(this.getVillagerSeed(), wrapper);
        this.tryLevelUp();
    }

    public void tryLevelUp() {
        while (this.level < MAX_LEVEL && this.exp >= EXPS[this.level]) {
            this.exp -= EXPS[this.level];
            this.level++;

            this.makeSound(SoundEvents.PLAYER_LEVELUP);
        }

        if (this.level >= MAX_LEVEL) {
            this.level = MAX_LEVEL;
            this.exp = Math.min(this.exp, EXPS[MAX_LEVEL - 1]);
        }
    }

    public @Nullable VillagerData tryGetModifyVillagerData() {
        if (this.villagerDataCache != null) {
            return this.villagerDataCache;
        }
        try {
            MinecraftServer server = ReverieDreams.getServer();
            if (server != null) {
                VillagerData modifyVillagerData = this.getModifyVillagerData(server.registryAccess());
                this.villagerDataCache = modifyVillagerData;
                return modifyVillagerData;
            }
            BalmSafeClientAccess clientAccess = Balm.safeClientAccess();
            Player clientPlayer = clientAccess.getClientPlayer();
            if (clientPlayer != null) {
                RegistryAccess registryAccess = clientPlayer.registryAccess();
                VillagerData modifyVillagerData = this.getModifyVillagerData(registryAccess);
                this.villagerDataCache = modifyVillagerData;
                return modifyVillagerData;
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return null;
    }

    public abstract VillagerData getModifyVillagerData(RegistryAccess registryManager);

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        Level baseWorld = player.level();
        ItemStack stack = player.getItemInHand(hand);
        if (ItemUtils.shouldPass(player, hand)) {
            return InteractionResult.PASS;
        }
        if (!baseWorld.isClientSide() && baseWorld instanceof ServerLevel world) {
            if (stack.getItem() instanceof ShovelItem && player.isShiftKeyDown() && this.canReset()) {
                boolean canceled = this.cancel();
                if (canceled && !player.hasInfiniteMaterials()) {
                    stack.hurtWithoutBreaking(1, player);
                }
                player.swing(hand);
                return InteractionResult.SUCCESS_SERVER;
            }
            Iterator<SellerGui> iterator = this.sessions.iterator();
            while (iterator.hasNext()) {
                SellerGui next = iterator.next();
                next.close();
                iterator.remove();
            }
            ServerPlayer serverPlayer = (ServerPlayer) player;

            this.lookAt(EntityAnchorArgument.Anchor.EYES, player.position().add(0, 1, 0));
            this.getNavigation().stop();

            SellerGui sellerGui = new SellerGui(serverPlayer, this);
            sellerGui.open();
            player.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    public boolean cancel() {
        Level world = this.level();
        if (this.prev != null) {
            Villager villager = new Villager(EntityType.VILLAGER, this.level());
            villager.setVillagerData(this.prev);
            villager.setPosRaw(this.getX(), this.getY(), this.getZ());
            this.makeSound(SoundEvents.BOOK_PAGE_TURN);
            this.discard();
            world.addFreshEntity(villager);
            return true;
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        view.read("PrevVillagerData", VillagerData.CODEC).ifPresent(villagerData -> {
            this.prev = villagerData;
        });
        this.level = view.getIntOr("Level", 0);
        view.read("SellInfoData", SellInfo.CODEC).ifPresent(value -> {
            this.sellInfo = value;
        });
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.storeNullable("PrevVillagerData", VillagerData.CODEC, this.prev);
        view.putInt("Level", this.level);
        view.storeNullable("SellInfoData", SellInfo.CODEC, this.sellInfo);
    }

    public abstract List<MerchantOffer> getVillagerOffers();

    public long getVillagerSeed() {
        UUID uuid = this.getUUID();
        Level world = this.level();
        long day = world.getGameTime() / 24000L;
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        return mostSigBits + leastSigBits + day;
    }

    @Getter
    public static class SellerGui extends MerchantGui {
        private final AbstractSeller self;

        public SellerGui(ServerPlayer player, AbstractSeller self) {
            super(player, false);
            this.self = self;
            this.init();
        }

        public void init() {
            this.setTitle(this.self.getName());
            List<MerchantOffer> villagerOffers = this.self.getVillagerOffers();
            for (MerchantOffer offer : villagerOffers) {
                this.addTrade(offer);
            }
            if (villagerOffers.isEmpty()) {
                this.self.discard();
                this.close();
            }
        }

        @Override
        public boolean onTrade(MerchantOffer offer) {
            MerchantOffer copied = offer.copy();
            this.self.trade(IngredientStack.of(copied.assemble()));
            this.self.notifyTrade(copied);
            return super.onTrade(offer);
        }

        @Override
        public void onOpen() {
            super.onOpen();
            this.self.getSessions().add(this);
        }

        @Override
        public void onTick() {
            super.onTick();
            this.self.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 20, false, false));
        }
    }

    public abstract boolean canReset();
}
