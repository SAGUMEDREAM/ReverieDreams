package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCHurtByTargetGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCLookAroundGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCLookAtEntityGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.SmartFlyGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.entity.skill.SkillContainer;
import cc.thonly.reverie_dreams.entity.skill.Skills;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.skin.GensokyoSkinTypes;
import cc.thonly.reverie_dreams.util.FrostWalkerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

@SuppressWarnings({"RedundantMethodOverride", "DuplicatedCode", "resource"})
public class IceFairy extends BaseNPCLikeEntity implements FriendlyFaction {
    private final SkillContainer<IceFairy> skillContainer;
    private BlockPos lastFrostWalkerPos;

    public IceFairy(Level world) {
        this(RDEntityTypes.ICE_FAIRY.get(), world);
    }

    public IceFairy(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
        this.setSkinType(GensokyoSkinTypes.CIRNO);
        this.skillContainer = new SkillContainer<>(this, (entity) -> entity.getTarget() != null, new ArrayList<>());
        this.skillContainer.addSkill(Skills.ICE_FAIRY_SKILL);
        this.enableFairyFlight();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (this.isValidSkillTarget(target)) {
            this.skillContainer.tick();
        } else {
            this.skillContainer.stop();
            if (target != null) {
                this.setTarget(null);
            }
        }
    }

    private boolean isValidSkillTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (this.getOwner() == target) {
            return false;
        }
        if (this.getOwner() != null
                && target instanceof TamableAnimal tameable
                && tameable.getOwner() == this.getOwner()) {
            return false;
        }
        return true;
    }

    @Override
    public void die(DamageSource damageSource) {
        this.skillContainer.stop();
        super.die(damageSource);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // 妖精的轻盈下坠，逃跑/跳跃时不会像石头一样直掉
        Vec3 movement = this.getDeltaMovement();
        if (!this.onGround() && movement.y < 0.0 && !(this.moveControl.getWantedY() < this.getY())) {
            this.setDeltaMovement(movement.multiply(1.0, 0.6, 1.0));
        }

        if (this.level().isClientSide()) {
            return;
        }

        BlockPos current = this.blockPosition();

        if (!current.equals(this.lastFrostWalkerPos)) {
            FrostWalkerHandler.freezeWater(
                    this.level(),
                    current,
                    2
            );

            this.lastFrostWalkerPos = current;
        }
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 被动状态也保持距离：没有战斗目标时，玩家靠近会主动飘走
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(
                this,
                Player.class,
                4.0f,
                1.2,
                1.6,
                player -> this.getTarget() == null && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)
        ));

        this.goalSelector.addGoal(8, new SmartFlyGoal(this, 1.2));

        this.goalSelector.addGoal(10, new NPCLookAroundGoal(this));
        this.goalSelector.addGoal(10, new NPCLookAtEntityGoal(this, Player.class, 8.0f, 0.02f, true));
        this.goalSelector.addGoal(10, new NPCLookAtEntityGoal(this, BaseNPCLikeEntity.class, 8.0f, 0.02f, true));

        // 被动生物：只有被打时才会把攻击者设为目标
        this.targetSelector.addGoal(1, new NPCHurtByTargetGoal(this));
    }

    @Override
    public String getFactionId() {
        return "mob";
    }

    /**
     * 冰精的战斗完全由 IceFairySkill 驱动，不再使用基类的近战/弓箭/弹幕物品 Goal，
     * 否则近战 Goal 会和技能里的走位/逃跑抢导航。
     */
    @Override
    protected void updateAttackType() {
        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.bowAttackGoal);
        this.goalSelector.removeGoal(this.crossBowAttackGoal);
        this.goalSelector.removeGoal(this.danmakuItemGoal);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        this.skillContainer.addAdditionalSaveData(view);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.skillContainer.readAdditionalSaveData(view);
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }

    @Override
    public Boolean consumeHunger() {
        return false;
    }

    @Override
    public Boolean canDamageEquipment() {
        return false;
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    @Override
    public Boolean canFeed() {
        return false;
    }
}
