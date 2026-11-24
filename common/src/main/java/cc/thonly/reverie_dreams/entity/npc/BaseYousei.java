package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.world.LandEntitySpawnRules;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;


public abstract class BaseYousei extends BaseNPCLikeEntity {
    private static final String NATURAL_SPAWN = "NaturalFairySpawn";
    private boolean naturalSpawn;

    public BaseYousei(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    public BaseYousei(EntityType<? extends TamableAnimal> entityType, Level world, SkinType skinType) {
        super(entityType, world, skinType);
    }

    public static <T extends BaseYousei> boolean getSpawnPredicate(EntityType<T> entityType,
                                                                   ServerLevelAccessor world,
                                                                   EntitySpawnReason reason,
                                                                   BlockPos pos,
                                                                   RandomSource randomSource) {
        return LandEntitySpawnRules.fairy(entityType, world, reason, pos, randomSource);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (!super.checkSpawnRules(level, reason))
            return false;
        // Recheck current occupancy when the natural spawner admits the mob, without rerolling the placement chance.
        return !LandEntitySpawnRules.isNatural(reason) || !(level instanceof ServerLevelAccessor serverLevel)
                || LandEntitySpawnRules.hasRoomForFairy(this.getType(), serverLevel, this.blockPosition());
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData groupData) {
        this.naturalSpawn = LandEntitySpawnRules.isNatural(reason);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return this.naturalSpawn && !this.isTame() && !this.hasCustomName() && !this.isLeashed()
                && !this.isPersistenceRequired() && !this.isPassenger() && !this.isVehicle();
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Legacy dream fairies had no origin flag. Vanilla despawn can drain the old crowd gradually.
        this.naturalSpawn = input.getBooleanOr(NATURAL_SPAWN, this.level().dimension().equals(RDBuiltinLevels.DREAM_WORLD));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(NATURAL_SPAWN, this.naturalSpawn);
    }

}
