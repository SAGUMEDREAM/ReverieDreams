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
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

import java.util.List;

@Getter
public class HairballEntity extends PathfinderMob implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private OverlayEntityHolder<HairballEntity, AnimatedEntity> holder;
    public HairballEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        PolymerEntityHelper.addEntityHolderModel(this);
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

    @Override
    public void tick() {
        super.tick();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 15)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 3.5)
                .add(Attributes.FOLLOW_RANGE, 15);
    }

    public static boolean checkSpawnRules(
            EntityType<HairballEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        int max = 5;
        int radius = 32;

        List<HairballEntity> nearby = level.getEntitiesOfClass(
                HairballEntity.class,
                new AABB(pos).inflate(radius)
        );

        return nearby.size() < max;
    }

    @Override
    public void onCreated() {
        this.holder = new OverlayLivingEntityHolder<>(this, this, PolymerEntityHelper.HAIRBALL_MODEL);
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
    public HairballEntity getEntity() {
        return this;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }

}
