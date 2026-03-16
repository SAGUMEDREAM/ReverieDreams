package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.ai.goal.DanmakuGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.UniversalLivingAngerGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.NPCWeaponOfTheMoonGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.RangedAttackUtil;
import cc.thonly.reverie_dreams.entity.interfaces.DanmakuShooter;
import cc.thonly.reverie_dreams.entity.interfaces.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.interfaces.VariantData;
import cc.thonly.reverie_dreams.entity.interfaces.Yousei;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitVariant;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitVariants;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.DelayedTask;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Objects;

@Setter
@Getter
public class RabbitUnitEntity extends BaseNPCLikeEntity implements Leashable, FriendlyFaction, VariantData, Yousei {
    private RabbitUnitVariant variant = null;
    private final NPCWeaponOfTheMoonGoal<RabbitUnitEntity> weaponOfTheMoonGoal = new NPCWeaponOfTheMoonGoal<>(this, 4, 8);

    public RabbitUnitEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType,
                world,
                (
                        RabbitUnitVariants.isEmpty()
                                ? (RabbitUnitVariants.REGISTRY.getAny().isPresent() ? RabbitUnitVariants.REGISTRY.getAny().get().value().getSkinType() : RabbitUnitVariants.RABBIT_UNIT_0.getSkinType())
                                : Objects.requireNonNull(RabbitUnitVariants.random()).getSkinType()
                )
        );
        this.xpReward = 5;
        this.variant = RabbitUnitVariants.getFromProperty(this.getSkin());
        if (ReverieDreams.RD.nextBoolean()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(RDItems.WEAPON_OF_THE_MOON));
        } else {
            if (ReverieDreams.RD.nextBoolean()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(RDItems.SILVER_SWORD));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(RDItems.SILVER_SPEAR));
            }
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        }
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new UniversalLivingAngerGoal<>(this, false));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Skeleton.class, true));

    }

    @Override
    public void tick() {
        this.skinType = this.variant != null ? this.variant.getSkinType() : RabbitUnitVariants.RABBIT_UNIT_0.getSkinType();
        if (this.getTarget() instanceof RabbitUnitEntity) {
            this.setTarget(null);
        }
        super.tick();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    protected void updateAttackType() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.weaponOfTheMoonGoal);
        this.goalSelector.removeGoal(this.bowAttackGoal);
        this.goalSelector.removeGoal(this.crossBowAttackGoal);
        this.goalSelector.removeGoal(this.danmakuItemGoal);

        if (RangedAttackUtil.getArrowStack(this) != null && (this.inventory.findHand((stack -> stack.is(Items.BOW) || stack.getItem() instanceof BowItem)) != null)) {
            int i = this.getRegularAttackInterval();
            this.bowAttackGoal.setAttackInterval(i);
            this.goalSelector.addGoal(4, this.bowAttackGoal);
        } else if (RangedAttackUtil.getCrossBowAmmoStack(this) != null && (this.inventory.findHand((stack -> stack.is(Items.CROSSBOW) || stack.getItem() instanceof CrossbowItem)) != null)) {
            this.goalSelector.addGoal(4, this.crossBowAttackGoal);
        } else if (RangedAttackUtil.isDanmakuInHand(this)) {
            this.goalSelector.addGoal(4, this.danmakuItemGoal);
        } else if (RangedAttackUtil.isWeaponOfTheMoonInHand(this)) {
            this.goalSelector.addGoal(4, this.weaponOfTheMoonGoal);
        } else {
            this.goalSelector.addGoal(4, this.meleeAttackGoal);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        String youseiVariantId = view.getStringOr("UnitVariant", RabbitUnitVariants.DEFAULT_ID.toString());
        Identifier variantId = Identifier.parse(youseiVariantId);
        this.variant = RegistryHandlers.RABBIT_UNIT_VARIANT.getValue(variantId);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putString("UnitVariant", this.variant.getId().toString());
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    @Override
    public String getFactionId() {
        return "rabbit_unit_" + this.getId();
    }

    @Override
    public void setVariantData(Identifier id) {
        this.variant = RegistryHandlers.RABBIT_UNIT_VARIANT.getValue(id);
        if (this.variant != null) {
            this.skinType = this.variant.getSkinType();
        }
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
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
