package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.ai.goal.NPCHurtByTargetGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCLookAroundGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCLookAtEntityGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCWanderAroundFarGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.entity.skill.SkillContainer;
import cc.thonly.reverie_dreams.entity.skill.Skills;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.skin.GensokyoSkinTypes;
import cc.thonly.reverie_dreams.util.FrostWalkerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;

@SuppressWarnings({"RedundantMethodOverride", "DuplicatedCode", "resource"})
public class IceFairy extends BaseNPCLikeEntity {
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
    }

    @Override
    public void tick() {
        super.tick();
        this.skillContainer.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide()) {
            return;
        }

        BlockPos current = this.blockPosition();

        if (!current.equals(this.lastFrostWalkerPos)) {
            FrostWalkerUtil.freezeWater(
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

        this.goalSelector.addGoal(4, new NPCHurtByTargetGoal(this));

        this.goalSelector.addGoal(8, new NPCWanderAroundFarGoal(this, 1.0));

        this.goalSelector.addGoal(10, new NPCLookAroundGoal(this));
        this.goalSelector.addGoal(10, new NPCLookAtEntityGoal(this, Player.class, 8.0f, 0.02f, true));
        this.goalSelector.addGoal(10, new NPCLookAtEntityGoal(this, BaseNPCLikeEntity.class, 8.0f, 0.02f, true));
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
