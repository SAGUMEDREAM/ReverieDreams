package cc.thonly.reverie_dreams.entity.elemental;

import cc.thonly.reverie_dreams.api.entity.type.ElementalMob;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class IceElementalEntity extends BaseNPCLikeEntity implements ElementalMob {
    public int aTick = 0;

    public IceElementalEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world, MobSkinTypes.ICE_ELEMENTAL);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));

        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, BaseNPCLikeEntity.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public void tick() {
        super.tick();
        Level world = this.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            if (this.aTick > 10) {
                BlockState blockState = serverWorld.getBlockState(this.blockPosition());
                Block block = blockState.getBlock();
                boolean bl1 = block.equals(Blocks.WATER);
                boolean bl2 = block.equals(Blocks.LAVA) || block.equals(Blocks.FIRE);
                if (bl1) {
                    this.addEffect(new MobEffectInstance(MobEffects.SPEED, 3 * 20));
                    this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 3 * 20));
                    this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3 * 20));
                }
                if (bl2) {
                    this.addEffect(new MobEffectInstance(MobEffects.WITHER, 3 * 20));
                }
                this.aTick = -1;
            }
            this.aTick++;
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, Entity target) {
        if (target instanceof LivingEntity) {
            ((LivingEntity) target).addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 15, 1));
            ((LivingEntity) target).addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 15));
        }
        return super.doHurtTarget(world, target);
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_KNOCKBACK, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 3.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.TEMPT_RANGE, 10.0)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        Level world = this.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            ItemEntity itemEntity = new ItemEntity(serverWorld, this.getX(), this.getY(), this.getZ(), new ItemStack(RDBlocks.MAGIC_ICE_BLOCK, RandomSource.create().nextIntBetweenInclusive(0, 5)), 0, 0.1, 0);
            world.addFreshEntity(itemEntity);
        }
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    public static boolean canSpawn(
            EntityType<? extends Mob> type,
            ServerLevelAccessor world,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        ServerLevel serverWorld = world.getLevel();
        // 脚下方块
        BlockState ground = world.getBlockState(pos.below());
        boolean isSnowBlock = ground.is(Blocks.SNOW_BLOCK)
                || ground.is(Blocks.SNOW)
                || ground.is(Blocks.POWDER_SNOW)
                || (ground.is(Blocks.GRASS_BLOCK) && ground.getValue(BlockStateProperties.SNOWY)); // 有雪覆盖的草方块

        if (!isSnowBlock) {
            return false;
        }

        // 光照条件（火把等会提高亮度）
        int light = world.getRawBrightness(pos, 0);

        // 世界时间（0~23999，0~12000 白天，12000~23999 夜晚）
        long timeOfDay = serverWorld.getGameTime() % 24000L;
        boolean isNight = timeOfDay >= 13000 && timeOfDay <= 23000; // 晚上时间段

        return isNight && light <= 7;
    }

}
