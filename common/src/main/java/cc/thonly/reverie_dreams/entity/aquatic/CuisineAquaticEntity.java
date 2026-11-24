package cc.thonly.reverie_dreams.entity.aquatic;

import cc.thonly.reverie_dreams.registry.content.item.RDAquaticItems;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/** Shared fish/bucket lifecycle with deliberately small behavior differences for benthic species. */
public class CuisineAquaticEntity extends AbstractFish implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.swim");
    private static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.flop");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.walk");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final AquaticSpecies species;
    private Vec3 beforeDryFlop;

    public CuisineAquaticEntity(EntityType<? extends AbstractFish> type, Level level, AquaticSpecies species) {
        super(type, level);
        this.species = Objects.requireNonNull(species, "species");
        // Mob registers its vanilla goals during super construction, before species can be assigned.
        if (species.bottomDweller()) {
            this.goalSelector.removeAllGoals(goal -> true);
            this.moveControl = new AquaticBottomMoveControl(this);
            if (!species.stationary()) this.goalSelector.addGoal(4, new AquaticBottomWanderGoal(this));
        }
    }

    public AquaticSpecies species() { return this.species; }

    @Override public int getMaxSpawnClusterSize() {
        return this.species == AquaticSpecies.SUPREME_TUNA || this.species == AquaticSpecies.OCTOPUS ? 1 : 2;
    }

    public static AttributeSupplier.Builder createAttributes(AquaticSpecies species) {
        return AbstractFish.createAttributes().add(Attributes.MAX_HEALTH, species.health())
                .add(Attributes.MOVEMENT_SPEED, species.speed());
    }

    public String animationName() {
        Vec3 velocity = this.getDeltaMovement();
        boolean moving = this.species.habitat() == AquaticSpecies.Habitat.BOTTOM_WALKER
                ? velocity.horizontalDistanceSqr() > .0001 : velocity.lengthSqr() > .0001;
        return this.species.animationName(this.isInWater(), moving);
    }

    @Override public ItemStack getBucketItemStack() {
        // Runtime lookup only: item and entity factories may reference one another during registration.
        return new ItemStack(RDAquaticItems.bucket(this.species));
    }

    @Override protected SoundEvent getFlopSound() { return this.species.flops() ? SoundEvents.COD_FLOP : SoundEvents.EMPTY; }
    @Override protected SoundEvent getHurtSound(DamageSource source) {
        return this.species == AquaticSpecies.OCTOPUS ? SoundEvents.SQUID_HURT : SoundEvents.COD_HURT;
    }
    @Override protected SoundEvent getDeathSound() {
        return this.species == AquaticSpecies.OCTOPUS ? SoundEvents.SQUID_DEATH : SoundEvents.COD_DEATH;
    }

    @Override public void aiStep() {
        // AbstractFish adds a random .4Y impulse before delegating to normal movement physics.
        // Keep ordinary gravity/air handling, but cancel that fish-only impulse for non-flopping species.
        if (!this.species.flops() && !this.isInWater() && this.onGround() && this.verticalCollision)
            this.beforeDryFlop = this.getDeltaMovement();
        try { super.aiStep(); }
        finally {
            // NoAI and non-authoritative clients can skip travel, so also discard the unused fish impulse.
            if (this.beforeDryFlop != null) this.setDeltaMovement(this.beforeDryFlop);
            this.beforeDryFlop = null;
        }
    }

    @Override public void travel(Vec3 input) {
        if (this.beforeDryFlop != null) {
            this.setDeltaMovement(this.beforeDryFlop);
            this.beforeDryFlop = null;
        }
        super.travel(input);
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("aquatic", 2, state -> state.setAndContinue(switch (this.animationName()) {
            case "animation.swim" -> SWIM;
            case "animation.flop" -> FLOP;
            case "animation.walk" -> WALK;
            default -> IDLE;
        })));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.geoCache; }
}
