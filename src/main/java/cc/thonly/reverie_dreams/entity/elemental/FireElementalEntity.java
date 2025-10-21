package cc.thonly.reverie_dreams.entity.elemental;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.skin.MobSkins;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class FireElementalEntity extends NPCEntityImpl implements ElementalMob {
    public int aTick = 10;

    public FireElementalEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world, MobSkins.FIRE_ELEMENTAL::get);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));

        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));

        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, NPCEntityImpl.class, 8.0f));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, false));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        DamageSources damageSources = world.getDamageSources();
        if (source.getType().equals(damageSources.inFire().getType())) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3 * 20));
            this.setFireTicks(20);
            if (this.aTick > 10) {
                boolean bl1 = this.isTouchingWater();
                boolean bl2 = serverWorld.getFluidState(this.getBlockPos()).isOf(Fluids.LAVA) ||
                        serverWorld.getBlockState(this.getBlockPos()).isOf(Blocks.FIRE);

                if (bl1) {
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 3 * 20));
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 3 * 20));
                }
                if (bl2) {
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 3 * 20));
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 3 * 20));
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 3 * 20));
                }
                this.aTick = -1;
            }

            this.aTick++;
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        if (target instanceof LivingEntity) {
            target.setFireTicks(20 * 6);
        }
        return super.tryAttack(world, target);
    }

    public static DefaultAttributeContainer createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.22)
                .add(EntityAttributes.ATTACK_KNOCKBACK, 0.25)
                .add(EntityAttributes.ATTACK_DAMAGE, 3.5)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.TEMPT_RANGE, 10.0)
                .add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3)
                .build();
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }
}
