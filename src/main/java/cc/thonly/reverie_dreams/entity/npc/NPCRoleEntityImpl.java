package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.entity.ai.goal.*;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCCleanMonsterGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCCloseToCropGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFarmGoal;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Getter
@Setter
public class NPCRoleEntityImpl extends NPCEntityImpl implements Leashable {

    public NPCRoleEntityImpl(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public NPCRoleEntityImpl(EntityType<? extends TameableEntity> entityType, World world, Property skin) {
        super(entityType, world, skin);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(1, new NPCEatFoodDispalyGoal(this,1,15,1));
        this.goalSelector.add(2, new EatGoal(this));
//        this.goalSelector.add(2, new WakeUpGoal(this));
        this.goalSelector.add(3, new SleepAtNightGoal(this, 1.0));

        this.goalSelector.add(4, new NPCTemptGoal(this, 1.2, stack -> stack.isOf(TAME_FOOD_ITEM), false));
        //        this.goalSelector.add(4, this.bowAttackGoal);
        //        this.goalSelector.add(4, this.meleeAttackGoal);

        this.goalSelector.add(6, new NPCFollowOwnerGoal(this, 1.0, 2.0f, 10.0f));
        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new NPCWanderAroundFarGoal(this, 1.0));

        this.goalSelector.add(10, new NPCLookAroundGoal(this));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f, 0.02f, true));
        this.goalSelector.add(10, new LookAtEntityGoal(this, NPCEntityImpl.class, 8.0f, 0.02f, true));

        this.targetSelector.add(1, new NPCTrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new NPCAttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(1, new NPCCleanMonsterGoal(this));

        this.goalSelector.add(1, new NPCFarmGoal(this));
        this.goalSelector.add(2, new NPCCloseToCropGoal(this,1));
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanSwim(true);
    }

    @Override
    public void tick() {
        World world = this.getWorld();
        if (!world.isClient && world.isDay()) {
            this.wakeUp();
        }
        super.tick();
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.ARCHIVED;
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public Boolean canFeed() {
        return true;
    }

    @Override
    public Boolean canDamageEquipment() {
        return true;
    }

    @Override
    public Boolean consumeHunger() {
        return true;
    }
}
