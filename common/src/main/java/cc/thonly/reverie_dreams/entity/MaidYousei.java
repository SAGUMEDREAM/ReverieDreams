package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.ai.goal.DanmakuGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.UniversalLivingAngerGoal;
import cc.thonly.reverie_dreams.entity.interfaces.DanmakuShooter;
import cc.thonly.reverie_dreams.entity.interfaces.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.interfaces.VariantData;
import cc.thonly.reverie_dreams.entity.interfaces.Yousei;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariants;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.DelayedTask;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class MaidYousei extends BaseNPCLikeEntity implements Leashable, FriendlyFaction, VariantData, Yousei {
    private YouseiVariant variant = null;

    public MaidYousei(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType,
                world,
                (
                        YouseiVariants.isEmpty()
                                ? (YouseiVariants.REGISTRY.getAny().isPresent() ? YouseiVariants.REGISTRY.getAny().get().value().getSkinType() : YouseiVariants.BLUE.getSkinType())
                                : Objects.requireNonNull(YouseiVariants.random()).getSkinType()
                )
        );
        this.xpReward = 5;
        this.variant = YouseiVariants.getFromProperty(this.getSkin());
        NPCInventoryImpl inventory = this.getInventory();
        inventory.setHead(new ItemStack(RDItems.MAID_HAIRBAND.asItem()));
        inventory.setChest(new ItemStack(RDItems.MAID_UPPER_SKIRT.asItem()));
        inventory.setLegs(new ItemStack(RDItems.MAID_LOWER_SKIRT.asItem()));
        inventory.setFeet(new ItemStack(RDItems.MAID_SHOE.asItem()));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DanmakuGoal(this, (self, target, world) -> {
            final MinecraftServer server = world.getServer();
            final float[] pitchYaw = DanmakuShooter.getPitchYaw(self, target);
            for (Float v : List.of(0.3f, 0.5f, 0.7f)) {
                DelayedTask.repeat(server, 2, v, () -> {
                    ItemStack stack = DanmakuTypes.random(DanmakuTypes.KUNAI).create();
                    DanmakuShooter.StackModifier modifier = origin -> {
                        ItemStack stack0 = DanmakuTypes.random(DanmakuTypes.KUNAI).create();
                        DanmakuProperties properties = stack0.get(RDDataComponents.DANMAKU_PROPERTIES.value());
                        if (properties != null) {
                            origin.set(RDDataComponents.DANMAKU_PROPERTIES.value(), properties.withSpeed(2.2f));
                        }
                        return origin;
                    };
                    DanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1], 0.5f, 5.0f, 0.2f, modifier);
                });
            }

            DelayedTask.repeat(server, 1, 1f, () -> {
                ItemStack knifeStack = new ItemStack(RDEntityHolderItems.KNIFE_DISPLAY.asItem());
                DanmakuProperties properties = knifeStack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
                if (properties != null) {
                    knifeStack.set(RDDataComponents.DANMAKU_PROPERTIES.value(), properties.withSpeed(1.2f));
                }
                DanmakuShooter.spawn(world, self, knifeStack.copy(), pitchYaw[0], pitchYaw[1] - 15.0f, 0.5f, 5.0f, 0.2f);
                DanmakuShooter.spawn(world, self, knifeStack.copy(), pitchYaw[0], pitchYaw[1], 0.5f, 5.0f, 0.2f);
                DanmakuShooter.spawn(world, self, knifeStack.copy(), pitchYaw[0], pitchYaw[1] + 15.0f, 0.5f, 5.0f, 0.2f);
                DanmakuShooter.soundDefault(this);
            });
        }, 45, 75));

        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new UniversalLivingAngerGoal<>(this, false));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public void tick() {
        this.setSkinType(this.variant != null ? this.variant.getSkinType() : YouseiVariants.BLUE.getSkinType());
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
        super.readAdditionalSaveData(view);
        String youseiVariantId = view.getStringOr("YouseiVariant", YouseiVariants.DEFAULT_ID.toString());
        Identifier variantId = Identifier.parse(youseiVariantId);
        this.variant = RegistryImpls.YOUSEI_VARIANT.getValue(variantId);
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
        this.variant = RegistryImpls.YOUSEI_VARIANT.getValue(id);
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
