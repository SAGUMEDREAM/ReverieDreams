package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.ai.goal.GhostStatusEffectTargetGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.NPCFollowOwnerGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Ghost extends BaseNPCLikeEntity {
    protected int particleTick = 0;
    protected int survivalTime = 0;

    public Ghost(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world, MobSkinTypes.GHOST);
    }

    public Ghost(Level world) {
        super(RDEntityTypes.GHOST.value(), world, MobSkinTypes.GHOST);
    }

    @Override
    public void tick() {
        super.tick();
        Level world = this.level();
        this.particleTick++;
        if(!world.isClientSide()) {
            if (!this.hasCustomName()
                    && !this.isPassenger()
                    && this.getPassengers().isEmpty()
                    && this.survivalTime > 600 * 20
            ) {
                this.discard();
            }
            if(this.survivalTime <= 600 * 20) {
                this.survivalTime++;
            }

            if (this.particleTick > 3) {
                if (world instanceof ServerLevel serverWorld) {
                    serverWorld.sendParticles(
                            ParticleTypes.WHITE_ASH,
                            this.position().x,
                            this.position().y,
                            this.position().z,
                            1,
                            0,
                            1,
                            0,
                            0.1
                    );
                }
                this.particleTick = 0;
            }
            if (world.isBrightOutside()) {
                MobEffectInstance currentEffect = this.getEffect(MobEffects.INVISIBILITY);
                if (currentEffect == null || currentEffect.getDuration() <= 20) {
                    this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
                }
            }
        }
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));

        this.goalSelector.addGoal(6, new NPCFollowOwnerGoal(this, 1.0, 2.0f, 10.0f));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, BaseNPCLikeEntity.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new GhostStatusEffectTargetGoal<>(this, Player.class, true, RDStatusEffects.MENTAL_DISORDER.builtInHolder()));
        this.targetSelector.addGoal(2, new GhostStatusEffectTargetGoal<>(this, Mob.class, true, RDStatusEffects.MENTAL_DISORDER.builtInHolder()));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putInt("SurvivalTime", this.survivalTime);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.survivalTime = view.getIntOr("SurvivalTime", 0);
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
