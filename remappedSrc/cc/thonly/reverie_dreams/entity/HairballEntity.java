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
import net.minecraft.entity.ai.goal.*;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import java.util.WeakHashMap;

@Getter
public class HairballEntity extends PathfinderMob implements AnimatedEntity {
    public static final WeakHashMap<Entity, EntityHolder<HairballEntity>> ELEMENTS = new WeakHashMap<>();
    public static final Model BLUE = ModelUtil.loadModel(Touhou.id("hairball"));
    private final Model hairballModel;
    private EntityHolder<HairballEntity> holder;

    protected HairballEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        this(entityType, world, BLUE);
    }

    protected HairballEntity(EntityType<? extends PathfinderMob> entityType, Level world, Model hairballModel) {
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
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 12.0f));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));

    }

    private void init() {
        this.holder = new LivingEntityHolder<>(this, this.hairballModel);
        EntityAttachment.ofTicking(this.holder, this);
        ELEMENTS.put(this,holder);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder); // util methods, see below
            AnimationHelper.updateHurtVariant(this, this.holder); // util methods
        }
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 15)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 3.5)
                .add(Attributes.FOLLOW_RANGE, 15)
                .build();
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }
}
