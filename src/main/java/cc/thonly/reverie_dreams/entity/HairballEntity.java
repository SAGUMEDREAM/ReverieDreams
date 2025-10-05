package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.util.entity.AnimationHelper;
import cc.thonly.reverie_dreams.util.entity.ModelUtil;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.WeakHashMap;

@Getter
public class HairballEntity extends PathAwareEntity implements AnimatedEntity {
    public static final WeakHashMap<Entity, EntityHolder<HairballEntity>> ELEMENTS = new WeakHashMap<>();
    public static final Model BLUE = ModelUtil.loadModel(Touhou.id("hairball"));
    private final Model hairballModel;
    private EntityHolder<HairballEntity> holder;

    protected HairballEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        this(entityType, world, BLUE);
    }

    protected HairballEntity(EntityType<? extends PathAwareEntity> entityType, World world, Model hairballModel) {
        super(entityType, world);
        this.hairballModel = hairballModel;
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
        this.init();
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(5, new LookAroundGoal(this));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));

    }

    private void init() {
        this.holder = new LivingEntityHolder<>(this, this.hairballModel);
        EntityAttachment.ofTicking(this.holder, this);
        ELEMENTS.put(this,holder);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder); // util methods, see below
            AnimationHelper.updateHurtVariant(this, this.holder); // util methods
        }
    }

    public static DefaultAttributeContainer createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.MAX_HEALTH, 15)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.ATTACK_DAMAGE, 3.5)
                .add(EntityAttributes.FOLLOW_RANGE, 15)
                .build();
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }
}
