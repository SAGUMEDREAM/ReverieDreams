package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.entity.AnimationHelper;
import cc.thonly.reverie_dreams.util.entity.ModelUtil;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import java.util.WeakHashMap;

@Getter
public class MushroomMonsterEntity extends PathfinderMob implements AnimatedEntity {
    public static final WeakHashMap<Entity, EntityHolder<MushroomMonsterEntity>> ELEMENTS = new WeakHashMap<>();
    public static final Model MODEL = ModelUtil.loadModel(ReverieDreams.id("mushroom_monster"));
    private final Model hairballModel;
    private EntityHolder<MushroomMonsterEntity> holder;

    protected MushroomMonsterEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        this(entityType, world, MODEL);
    }

    protected MushroomMonsterEntity(EntityType<? extends PathfinderMob> entityType, Level world, Model hairballModel) {
        super(entityType, world);
        this.hairballModel = hairballModel;
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.init();
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
            ((LivingEntity) target).addEffect(new MobEffectInstance(MobEffects.NAUSEA, 15 * 20));
        }
        return super.doHurtTarget(world, target);
    }

    private void init() {
        this.holder = new LivingEntityHolder<>(this, this.hairballModel);
        EntityAttachment.ofTicking(this.holder, this);
        ELEMENTS.put(this, holder);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder);
            AnimationHelper.updateHurtVariant(this, this.holder);
        }
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
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }
}
