package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.ai.goal.*;
import cc.thonly.reverie_dreams.entity.ai.goal.work.*;
import cc.thonly.reverie_dreams.interfaces.IExperienceOrbEntity;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@Getter
@Setter
public class NPCRoleEntity extends BaseNPCLikeEntity implements Leashable {

    public NPCRoleEntity(EntityType<? extends NPCRoleEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(1, new NPCEatFoodDispalyGoal(this, 1, 15, 1));
        this.goalSelector.add(2, new EatGoal(this));
        this.goalSelector.add(3, new SleepAtNightGoal(this, 1.0));

        this.goalSelector.add(4, new NPCTemptGoal(this, 1.2, stack -> stack.isIn(ModTags.ItemTypeTag.ROLE_TAME_FOOD), false));
        //        this.goalSelector.add(4, this.bowAttackGoal);
        //        this.goalSelector.add(4, this.meleeAttackGoal);

        this.goalSelector.add(6, new NPCFollowOwnerGoal(this, 1.0, 2.0f, 10.0f));
        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new NPCWanderAroundFarGoal(this, 1.0));

        this.goalSelector.add(10, new NPCLookAroundGoal(this));
        this.goalSelector.add(10, new NPCLookAtEntityGoal(this, PlayerEntity.class, 8.0f, 0.02f, true));
        this.goalSelector.add(10, new NPCLookAtEntityGoal(this, BaseNPCLikeEntity.class, 8.0f, 0.02f, true));

        this.targetSelector.add(1, new NPCTrackOwnerAttackerGoal(this));
        this.targetSelector.add(1, new NPCCleanMonsterGoal(this));
        this.targetSelector.add(1, new NPCBreedGoal(this));
        this.targetSelector.add(1, new NPCSheepShearGoal(this));
        this.targetSelector.add(2, new NPCAttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());

        this.goalSelector.add(1, new NPCOpenDoorGoal(this));
        this.goalSelector.add(1, new NPCOpenSilverChestGoal(this));
        this.goalSelector.add(1, new NPCSmeltGoal(this));
        this.goalSelector.add(1, new NPCChestClassificationGoal(this));
        this.goalSelector.add(1, new NPCFarmGoal(this));
        this.goalSelector.add(1, new NPCAutoPickItemGoal(this));
        this.goalSelector.add(2, new NPCCloseToCropGoal(this, 1));

        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanSwim(true);
    }

    @Override
    public void tick() {
        World world = this.getWorld();
        if (!world.isClient && world.isDay()) {
            this.wakeUp();
        }
        this.attractNearbyExperienceOrbs();
        super.tick();
    }

    @Override
    protected void drop(ServerWorld world, DamageSource damageSource) {
//        super.drop(world, damageSource);
    }

    public void attractNearbyExperienceOrbs() {
        if (this.getWorld().isClient) return; // 只在服务端处理

        double radius = 7.0;
        List<ExperienceOrbEntity> orbs = this.getWorld().getEntitiesByClass(
                ExperienceOrbEntity.class,
                this.getBoundingBox().expand(radius),
                Entity::isAlive
        );

        for (ExperienceOrbEntity orb : orbs) {
            ((IExperienceOrbEntity) (Object) orb).setNPCTarget(this);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity livingEntity &&
                livingEntity.getStackInHand(Hand.MAIN_HAND).isEmpty() &&
                livingEntity.getStackInHand(Hand.OFF_HAND).isEmpty() &&
                livingEntity.isSneaking() && this.isOwner(livingEntity)
        ) {
            this.setTarget(null);
            this.setAttacker(null);
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        World world = this.getWorld();
        if (world.isClient || !(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayerEntity)) {
            return super.interactMob(player, hand);
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
