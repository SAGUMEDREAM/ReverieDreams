package cc.thonly.reverie_dreams.entity.elemental;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.skin.MobSkinTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class IceElementalEntity extends BaseNPCLikeEntity implements ElementalMob {
    public int aTick = 0;

    public IceElementalEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world, MobSkinTypes.ICE_ELEMENTAL);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));

        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));

        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(10, new LookAtEntityGoal(this, BaseNPCLikeEntity.class, 8.0f));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, false));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            if (this.aTick > 10) {
                BlockState blockState = serverWorld.getBlockState(this.getBlockPos());
                Block block = blockState.getBlock();
                boolean bl1 = block.equals(Blocks.WATER);
                boolean bl2 = block.equals(Blocks.LAVA) || block.equals(Blocks.FIRE);
                if (bl1) {
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 3 * 20));
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 3 * 20));
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 3 * 20));
                }
                if (bl2) {
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 3 * 20));
                }
                this.aTick = -1;
            }
            this.aTick++;
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        if (target instanceof LivingEntity) {
            ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 15, 1));
            ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20 * 15));
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
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        World world = this.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            ItemEntity itemEntity = new ItemEntity(serverWorld, this.getX(), this.getY(), this.getZ(), new ItemStack(ModBlocks.MAGIC_ICE_BLOCK, Random.create().nextBetween(0, 5)), 0, 0.1, 0);
            world.spawnEntity(itemEntity);
        }
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    public static boolean canSpawn(
            EntityType<? extends MobEntity> type,
            ServerWorldAccess world,
            SpawnReason spawnReason,
            BlockPos pos,
            Random random
    ) {
        ServerWorld serverWorld = world.toServerWorld();
        // 脚下方块
        BlockState ground = world.getBlockState(pos.down());
        boolean isSnowBlock = ground.isOf(Blocks.SNOW_BLOCK)
                || ground.isOf(Blocks.SNOW)
                || ground.isOf(Blocks.POWDER_SNOW)
                || (ground.isOf(Blocks.GRASS_BLOCK) && ground.get(Properties.SNOWY)); // 有雪覆盖的草方块

        if (!isSnowBlock) {
            return false;
        }

        // 光照条件（火把等会提高亮度）
        int light = world.getBaseLightLevel(pos, 0);

        // 世界时间（0~23999，0~12000 白天，12000~23999 夜晚）
        long timeOfDay = serverWorld.getTimeOfDay() % 24000L;
        boolean isNight = timeOfDay >= 13000 && timeOfDay <= 23000; // 晚上时间段

        return isNight && light <= 7;
    }

}
