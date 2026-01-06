package cc.thonly.reverie_dreams.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.polymer.entity.PolymerHolderEntity;
import cc.thonly.polymer.entity.TickHolderEntity;
import cc.thonly.polymer.entity.bil.OverlayEntityHolder;
import cc.thonly.polymer.entity.bil.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.util.entity.AnimationHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

@Getter
public class MushroomMonsterEntity extends PathfinderMob implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private OverlayEntityHolder<MushroomMonsterEntity, AnimatedEntity> holder;

    public MushroomMonsterEntity(EntityType<MushroomMonsterEntity> mushroomMonsterEntityEntityType, Level level) {
        super(mushroomMonsterEntityEntityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.xpReward = 3;
        PolymerEntityHelper.addEntityHolderModel(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 16.0f));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, Entity target) {
        if (target instanceof LivingEntity) {
            ((LivingEntity) target).addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20));
            ((LivingEntity) target).addEffect(new MobEffectInstance(MobEffects.CONFUSION, 15 * 20));
        }
        return super.doHurtTarget(world, target);
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 3.5)
                .add(Attributes.FOLLOW_RANGE, 15)
                .build();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        Level world = this.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            RandomSource random = RandomSource.create();
            int count = random.nextInt(4);
            Item item = random.nextBoolean() ? Items.RED_MUSHROOM : Items.BROWN_MUSHROOM;
            ItemStack stack = new ItemStack(item, count);
            ItemEntity itemEntity = new ItemEntity(serverWorld, this.getX(), this.getY(), this.getZ(), stack, 0, 0.1, 0);
            world.addFreshEntity(itemEntity);
        }
    }
    @Override
    public void onCreated() {
        this.holder = new OverlayLivingEntityHolder<>(this, this, PolymerEntityHelper.MUSHROOM_MONSTER_MODEL);
        TickHolderEntity.addTickHolder(this);
        TickHolderEntity.addElementBind(this, this.holder);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public void onTick() {
        if (this.holder == null) {
            return;
        }
        if (this.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder);
            AnimationHelper.updateHurtVariant(this, this.holder);
        }
    }

    @Override
    public MushroomMonsterEntity getEntity() {
        return this;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }

}
