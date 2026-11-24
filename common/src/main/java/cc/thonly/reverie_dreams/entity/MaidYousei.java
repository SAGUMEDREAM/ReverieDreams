package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.api.entity.type.VariantData;
import cc.thonly.reverie_dreams.api.entity.type.YouseiType;
import cc.thonly.reverie_dreams.entity.ai.goal.DanmakuGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.SmartFlyGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.UniversalLivingAngerGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseYousei;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.registry.content.variant.YouseiVariants;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.util.entity.EntityHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyActions;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyEntity;

@Setter
@Getter
public class MaidYousei extends BaseYousei implements Leashable, FriendlyFaction, VariantData, YouseiType, GensokyoFairyEntity {
    public static final String SCARLET_NAME = "adventure.reverie_dreams.scarlet.maid";
    private final GensokyoFairyActions fairyActions = new GensokyoFairyActions();

    @Override public GensokyoFairyActions fairyActions() { return this.fairyActions; }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private YouseiVariant variant = null;

    public MaidYousei(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType,
                world,
                (
                        YouseiVariants.isEmpty()
                                ? (BuiltInRegistryProviders.YOUSEI_VARIANT.getAny().isPresent() ? BuiltInRegistryProviders.YOUSEI_VARIANT.getAny().get().value().getSkinType() : YouseiVariants.BLUE.getSkinType())
                                : Objects.requireNonNull(YouseiVariants.random()).getSkinType()
                )
        );
        this.xpReward = 5;
        this.variant = YouseiVariants.getFromSkinType(this.getSkinType());
        NPCInventoryImpl inventory = this.getInventory();
        inventory.setHead(new ItemStack(RDItems.MAID_HAIRBAND.asItem()));
        inventory.setChest(new ItemStack(RDItems.MAID_UPPER_SKIRT.asItem()));
        inventory.setLegs(new ItemStack(RDItems.MAID_LOWER_SKIRT.asItem()));
        inventory.setFeet(new ItemStack(RDItems.MAID_SHOE.asItem()));
        this.enableFairyFlight();
    }

    public boolean hasScarletName() {
        var name = this.getCustomName();
        return name != null && name.getContents() instanceof TranslatableContents title && SCARLET_NAME.equals(title.getKey());
    }

    @Override
    public void updateName() {
        if (this.hasScarletName())
            this.setCustomNameVisible(false);
        else
            super.updateName();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        GensokyoFairyActions.registerControllers(this, controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DanmakuGoal(this, (self, target, world) -> {
            MinecraftServer server = world.getServer();

            // Re-aim for every shot, with a velocity lead and a much tighter fan.
            for (int i = 0; i < 3; i++) {
                int shotIndex = i;
                DelayedTask.createFromSecond(server, 0.12F * shotIndex, () -> {
                    if (!self.isAlive() || !(target instanceof LivingEntity livingTarget) || !livingTarget.isAlive()) {
                        return;
                    }
                    float[] aim = DanmakuShooter.getPitchYaw(self, livingTarget, 2.2F);
                    ItemStack kunai = DanmakuTypes.random(DanmakuTypes.KUNAI).create();
                    DanmakuShooter.spawn(world, self, kunai,
                            aim[0], aim[1] + (shotIndex - 1) * 4.0F,
                            2.2F, 0.05F, 0.8F);
                    DanmakuShooter.soundDefault(self);
                });
            }

            // The knife display is a valid projectile item now; fire a narrow fan
            // after the kunai burst, again using a fresh prediction.
            DelayedTask.createFromSecond(server, 0.45F, () -> {
                if (!self.isAlive() || !(target instanceof LivingEntity livingTarget) || !livingTarget.isAlive()) {
                    return;
                }
                float[] aim = DanmakuShooter.getPitchYaw(self, livingTarget, 2.0F);
                ItemStack knife = new ItemStack(RDEntityHolderItems.KNIFE_DISPLAY.asItem());
                for (int i = -1; i <= 1; i++) {
                    DanmakuShooter.spawn(world, self, knife.copy(),
                            aim[0], aim[1] + i * 8.0F,
                            2.0F, 0.04F, 0.8F);
                }
                DanmakuShooter.soundDefault(self);
            });
        }, 45, 75));

        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(8, new SmartFlyGoal(this, 1.2));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new UniversalLivingAngerGoal<>(this, false));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));

        EntityHelper.registerHostilityAllRabbit(this, this.targetSelector);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            this.setSkinType(this.variant != null ? this.variant.getSkinType() : YouseiVariants.BLUE.getSkinType());
        }
        super.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 vec3d = getDeltaMovement();
        if (!onGround() && vec3d.y < 0.0 && !(moveControl.getWantedY() < getY())) {
            setDeltaMovement(vec3d.multiply(1.0, 0.6, 1.0));
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        String youseiVariantId = view.getStringOr("YouseiVariant", YouseiVariants.DEFAULT_ID.toString());
        Identifier variantId = Identifier.parse(youseiVariantId);
        this.variant = BuiltInRegistryProviders.YOUSEI_VARIANT.getValue(variantId);
        super.readAdditionalSaveData(view);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putString("YouseiVariant", this.variant.getId().toString());
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }

    @Override
    public String getFactionId() {
        return "mob";
    }

    @Override
    public void setVariantData(Identifier id) {
        this.variant = BuiltInRegistryProviders.YOUSEI_VARIANT.getValue(id);
        if (this.variant != null) {
            this.setSkinType(this.variant.getSkinType());
        }
    }

    @Override
    public Identifier getVariantData() {
        return this.variant.getId();
    }

    @Override
    public boolean requiresCustomPersistence() {
        return false;
    }
}
