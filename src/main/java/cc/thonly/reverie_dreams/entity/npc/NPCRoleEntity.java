package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.entity.ai.goal.*;
import cc.thonly.reverie_dreams.entity.ai.goal.work.*;
import cc.thonly.reverie_dreams.inf.IExperienceOrbEntity;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

@Getter
@Setter
public class NPCRoleEntity extends BaseNPCLikeEntity implements Leashable {

    public NPCRoleEntity(EntityType<? extends NPCRoleEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new NPCEatFoodDispalyGoal(this, 1, 15, 1));
        this.goalSelector.addGoal(2, new EatGoal(this));
        this.goalSelector.addGoal(3, new SleepAtNightGoal(this, 1.0));

        this.goalSelector.addGoal(4, new NPCTemptGoal(this, 1.2, stack -> stack.is(RDItemTags.ROLE_TAME_FOOD), false));
        //        this.goalSelector.add(4, this.bowAttackGoal);
        //        this.goalSelector.add(4, this.meleeAttackGoal);

        this.goalSelector.addGoal(6, new NPCFollowOwnerGoal(this, 1.0, 2.0f, 10.0f));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new NPCWanderAroundFarGoal(this, 1.0));

        this.goalSelector.addGoal(10, new NPCLookAroundGoal(this));
        this.goalSelector.addGoal(10, new NPCLookAtEntityGoal(this, Player.class, 8.0f, 0.02f, true));
        this.goalSelector.addGoal(10, new NPCLookAtEntityGoal(this, BaseNPCLikeEntity.class, 8.0f, 0.02f, true));

        this.targetSelector.addGoal(1, new NPCTrackOwnerAttackerGoal(this));
        this.targetSelector.addGoal(1, new NPCCleanMonsterGoal(this));
        this.targetSelector.addGoal(1, new NPCBreedGoal(this));
        this.targetSelector.addGoal(1, new NPCSheepShearGoal(this));
        this.targetSelector.addGoal(2, new NPCAttackWithOwnerGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());

        this.goalSelector.addGoal(1, new NPCOpenDoorGoal(this));
        this.goalSelector.addGoal(1, new NPCOpenSilverChestGoal(this));
        this.goalSelector.addGoal(1, new NPCSmeltGoal(this));
        this.goalSelector.addGoal(1, new NPCChestClassificationGoal(this));
        this.goalSelector.addGoal(1, new NPCFarmGoal(this));
        this.goalSelector.addGoal(1, new NPCAutoPickItemGoal(this));
        this.goalSelector.addGoal(2, new NPCCloseToCropGoal(this, 1));

        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
    }

    @Override
    public void tick() {
        Level world = this.level();
        if (!world.isClientSide() && world.isBrightOutside()) {
            this.stopSleeping();
        }
        this.attractNearbyExperienceOrbs();
        super.tick();
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel world, DamageSource damageSource) {
//        super.drop(world, damageSource);
    }

    public void attractNearbyExperienceOrbs() {
        if (this.level().isClientSide()) return; // 只在服务端处理

        double radius = 7.0;
        List<ExperienceOrb> orbs = this.level().getEntitiesOfClass(
                ExperienceOrb.class,
                this.getBoundingBox().inflate(radius),
                Entity::isAlive
        );

        for (ExperienceOrb orb : orbs) {
            ((IExperienceOrbEntity) (Object) orb).setNPCTarget(this);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingEntity &&
                livingEntity.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() &&
                livingEntity.getItemInHand(InteractionHand.OFF_HAND).isEmpty() &&
                livingEntity.isShiftKeyDown() && this.isOwnedBy(livingEntity)
        ) {
            this.setTarget(null);
            this.setLastHurtByMob(null);
            return false;
        }
        return super.hurtServer(world, source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        Level world = this.level();
        if (world.isClientSide() || !(world instanceof ServerLevel serverWorld) || !(player instanceof ServerPlayer serverPlayerEntity)) {
            return super.mobInteract(player, hand);
        }
        return NPCRoleInteractionEvents.emit(serverWorld, serverPlayerEntity, hand, this);
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
