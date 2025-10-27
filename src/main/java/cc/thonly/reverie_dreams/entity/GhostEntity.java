package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.ai.goal.GhostStatusEffectTargetGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCFollowOwnerGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.skin.MobSkinTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;

public class GhostEntity extends BaseNPCLikeEntity {
    protected int particleTick = 0;
    protected int survivalTime = 0;

    public GhostEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world, MobSkinTypes.GHOST);
    }

    public GhostEntity(World world) {
        super(ModEntities.GHOST_ENTITY_TYPE, world, MobSkinTypes.GHOST);
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getWorld();
        this.particleTick++;
        if(!world.isClient()) {
            if (!this.hasCustomName()
                    && !this.hasVehicle()
                    && this.getPassengerList().isEmpty()
                    && this.survivalTime > 600 * 20
            ) {
                this.discard();
            }
            if(this.survivalTime <= 600 * 20) {
                this.survivalTime++;
            }

            if (this.particleTick > 3) {
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(
                            ParticleTypes.WHITE_ASH,
                            this.getPos().x,
                            this.getPos().y,
                            this.getPos().z,
                            1,
                            0,
                            1,
                            0,
                            0.1
                    );
                }
                this.particleTick = 0;
            }
            if (world.isDay()) {
                StatusEffectInstance currentEffect = this.getStatusEffect(StatusEffects.INVISIBILITY);
                if (currentEffect == null || currentEffect.getDuration() <= 20) {
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, false, false));
                }
            }
        }
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));

        this.goalSelector.add(6, new NPCFollowOwnerGoal(this, 1.0, 2.0f, 10.0f));
        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));

        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, BaseNPCLikeEntity.class, 8.0f));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(2, new GhostStatusEffectTargetGoal<>(this, PlayerEntity.class, true, ModStatusEffects.MENTAL_DISORDER));
        this.targetSelector.add(2, new GhostStatusEffectTargetGoal<>(this, MobEntity.class, true, ModStatusEffects.MENTAL_DISORDER));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("SurvivalTime", this.survivalTime);
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.survivalTime = view.getInt("SurvivalTime", 0);
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.NOT_DROP_ANY;
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }
}
