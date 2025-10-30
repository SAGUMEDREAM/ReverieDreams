package cc.thonly.mystias_izakaya.entity;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PigVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WildPigEntity extends Pig {
    public static final ResourceKey<PigVariant> VARIANT = ResourceKey.create(Registries.PIG_VARIANT, MystiasIzakaya.id("wild_pig"));
    public WildPigEntity(EntityType<? extends Pig> entityType, Level world) {
        super(entityType, world);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
//        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25));

        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, (stack) -> {
            return stack.is(Items.CARROT_ON_A_STICK);
        }, false));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, (stack) -> {
            return stack.is(ItemTags.PIG_FOOD);
        }, false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Nullable
    public WildPigEntity getBreedOffspring(ServerLevel serverWorld, AgeableMob passiveEntity) {
        return MIEntities.WILD_PIG.create(serverWorld, EntitySpawnReason.BREEDING);
    }


}
