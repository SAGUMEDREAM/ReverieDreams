package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.ai.goal.UniversalLivingAngerGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.attack.RangedAttackUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.variant.OniVariant;
import cc.thonly.reverie_dreams.entity.variant.OniVariants;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Objects;

@SuppressWarnings({"RedundantMethodOverride", "DuplicatedCode", "ConstantValue", "resource"})
public class Oni extends BaseNPCLikeEntity implements FriendlyFaction {
    private OniVariant variant;

    public Oni(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world, (
                OniVariants.isEmpty()
                        ? (OniVariants.REGISTRY.getAny().isPresent() ? OniVariants.REGISTRY.getAny().get().value().getSkinType() : OniVariants.GREEN.getSkinType())
                        : Objects.requireNonNull(OniVariants.random()).getSkinType()
                )
        );
        this.xpReward = 5;
        this.variant = OniVariants.getFromProperty(this.getSkin());
        NPCInventoryImpl inventory = this.getInventory();
        inventory.setMainHand(RDItems.IRON_BAR.createStack());
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
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    protected void updateAttackType() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.goalSelector.removeGoal(this.meleeAttackGoal);
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
        } else {
            this.goalSelector.addGoal(4, this.meleeAttackGoal);
        }
    }

    @Override
    public void tick() {
        this.setSkinType(this.variant != null ? this.variant.getSkinType() : OniVariants.GREEN.getSkinType());
        super.tick();
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        String variantId = view.getStringOr("Variant", OniVariants.GREEN.getId().toString());
        Identifier variantIdentifier = Identifier.parse(variantId);
        this.variant = RegistryImpls.ONI_VARIANT.getValue(variantIdentifier);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putString("Variant", this.variant.getId().toString());
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }

    @Override
    public String getFactionId() {
        return "oni";
    }
}
