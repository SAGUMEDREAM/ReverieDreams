package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.entity.RabbitUnit;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the vanilla cage for a single successful lunar patrol wave.
 */
@Mixin(BaseSpawner.class)
public abstract class LunarPatrolSpawnerMixin {
    @Unique
    private static final int REVERIE_PATROL_LIMIT = 7;
    @Unique
    private static final int REVERIE_PATROL_POSITION_ATTEMPTS = 28;

    @Shadow
    private SpawnData nextSpawnData;
    @Shadow
    private int spawnCount;
    @Shadow
    private int maxNearbyEntities;
    @Shadow
    private int spawnRange;
    @Shadow
    private int requiredPlayerRange;
    @Shadow
    private int minSpawnDelay;
    @Shadow
    private int maxSpawnDelay;

    @Inject(method = "serverTick", at = @At("HEAD"))
    private void reverie_dream$preparePatrol(ServerLevel level, BlockPos pos, CallbackInfo ci,
                                             @Share("lunarPatrol") LocalBooleanRef patrol) {
        if (this.nextSpawnData == null || !(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner))
            return;
        var data = this.nextSpawnData.getEntityToSpawn();
        if (!data.getStringOr("id", "").equals("reverie_dreams:rabbit_unit"))
            return;
        if (!data.getBooleanOr("ReverieLunarPatrol", false)) {
            if (this.spawnCount != 4 || this.maxNearbyEntities != 6 || this.spawnRange != 3
                    || this.requiredPlayerRange != 16 || this.minSpawnDelay != 200 || this.maxSpawnDelay != 400
                    || !this.reverie_dream$isLegacyShip(level, pos))
                return;
            data.putBoolean("ReverieLunarPatrol", true);
            spawner.setChanged();
        }
        this.maxNearbyEntities = REVERIE_PATROL_LIMIT;
        patrol.set(true);
    }

    @ModifyExpressionValue(method = "serverTick", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/BaseSpawner;spawnCount:I"))
    private int reverie_dream$tryMorePatrolPositions(int attempts, @Share("lunarPatrol") LocalBooleanRef patrol) {
        return patrol.get() ? REVERIE_PATROL_POSITION_ATTEMPTS : attempts;
    }

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean reverie_dream$equipAndCountPatrol(ServerLevel level, Entity entity, Operation<Boolean> original,
                                                      @Share("lunarPatrol") LocalBooleanRef patrol,
                                                      @Share("patrolSpawned") LocalIntRef spawned) {
        if (patrol.get() && spawned.get() >= REVERIE_PATROL_LIMIT)
            return false;
        if (entity instanceof RabbitUnit unit) {
            // The patrol flag makes this look like a custom entity template to vanilla.
            if (patrol.get() && this.nextSpawnData.getEntityToSpawn().size() == 2) {
                unit.finalizeSpawn(level, level.getCurrentDifficultyAt(unit.blockPosition()), EntitySpawnReason.SPAWNER, null);
            }
            unit.restoreSpawnerLoadout();
        }
        boolean added = original.call(level, entity);
        if (added && patrol.get())
            spawned.set(spawned.get() + 1);
        return added;
    }

    @Inject(method = "serverTick", at = @At("RETURN"))
    private void reverie_dream$exhaustPatrolCage(ServerLevel level, BlockPos pos, CallbackInfo ci,
                                                 @Share("patrolSpawned") LocalIntRef spawned) {
        if (spawned.get() > 0 && level.getBlockState(pos).is(Blocks.SPAWNER))
            level.removeBlock(pos, false);
    }

    @Unique
    private boolean reverie_dream$isLegacyShip(ServerLevel level, BlockPos pos) {
        // Older ships omitted the patrol flag. Match both engine braces before migrating the cage.
        for (Direction axis : new Direction[]{Direction.EAST, Direction.SOUTH}) {
            if (!reverie_dream$hasEngineBrace(level, pos.relative(axis, 2))
                    || !reverie_dream$hasEngineBrace(level, pos.relative(axis.getOpposite(), 2)))
                continue;
            for (Direction cabin : new Direction[]{axis.getClockWise(), axis.getCounterClockWise()}) {
                for (int distance : new int[]{3, 7}) {
                    BlockPos lanternPos = pos.relative(cabin, distance).above();
                    if (!level.hasChunkAt(lanternPos))
                        continue;
                    var lantern = level.getBlockState(lanternPos);
                    BlockPos chainPos = lanternPos.above();
                    if (lantern.is(Blocks.LANTERN) && lantern.getValue(LanternBlock.HANGING)
                            && level.isEmptyBlock(chainPos)
                            && level.getBlockState(chainPos.above()).isFaceSturdy(level, chainPos.above(), Direction.DOWN)) {
                        level.setBlockAndUpdate(chainPos, Blocks.IRON_CHAIN.defaultBlockState());
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Unique
    private static boolean reverie_dream$hasEngineBrace(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).is(Blocks.OXIDIZED_COPPER)
                && level.getBlockState(pos.above()).is(Blocks.COPPER_GRATE);
    }
}
