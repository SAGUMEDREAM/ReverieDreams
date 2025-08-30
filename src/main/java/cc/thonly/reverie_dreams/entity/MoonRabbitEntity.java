package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.ai.goal.UniversalLivingAngerGoal;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MoonRabbitEntity extends RabbitEntity {
    public MoonRabbitEntity(EntityType<? extends RabbitEntity> entityType, World world) {
        super(entityType, world);
        this.setVariant(Variant.WHITE);
        AttributeContainer attributeContainer = this.getAttributes();
        if (attributeContainer != null) {
            EntityAttributeInstance scale = attributeContainer.getCustomInstance(EntityAttributes.SCALE);
            if (scale != null) {
                scale.setBaseValue(1.8);
            }
        }
    }

    public MoonRabbitEntity(World world) {
        this(ModEntities.MOON_RABBIT_ENTITY_TYPE, world);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        this.setVariant(Variant.WHITE);
        return data;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new PowderSnowJumpGoal(this, this.getWorld()));
//        this.goalSelector.add(1, new EscapeDangerGoal(this, 2.2));
        this.goalSelector.add(2, new AnimalMateGoal(this, 0.8));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.add(3, new TemptGoal(this, 1.0, stack -> stack.isIn(ItemTags.RABBIT_FOOD), false));
//        this.goalSelector.add(4, new FleeGoal<PlayerEntity>(this, PlayerEntity.class, 8.0f, 2.2, 2.2));
        this.goalSelector.add(4, new FleeGoal<WolfEntity>(this, WolfEntity.class, 10.0f, 2.2, 2.2));
//        this.goalSelector.add(4, new FleeGoal<HostileEntity>(this, HostileEntity.class, 4.0f, 2.2, 2.2));
        this.goalSelector.add(5, new EatCarrotCropGoal(this));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.6));
        this.goalSelector.add(11, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));

        this.targetSelector.add(3, new RevengeGoal(this));
        this.targetSelector.add(3, new UniversalLivingAngerGoal<>(this, false));

    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
    }



    @Override
    public Variant getVariant() {
        return Variant.WHITE;
    }

    @Override
    public @Nullable RabbitEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        return ModEntities.MOON_RABBIT_ENTITY_TYPE.create(serverWorld, SpawnReason.BREEDING);
    }

}
