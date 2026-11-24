package cc.thonly.reverie_dreams.data.danmaku;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.api.registry.SerializableProvider;
import cc.thonly.reverie_dreams.api.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DanmakuTrajectory implements SerializableProvider<DanmakuTrajectory>, RegistryEntryOwnerBindable<DanmakuTrajectory>, BuiltinObject {
    /**
     * The registry codec stores the behavior identity only; numeric per-item
     * parameters are stored by {@code RDDataComponentTypes.DANMAKU_TRAJECTORY_CONFIG}.
     */
    public static final Codec<DanmakuTrajectory> CODEC = UnitCodec.unit(DanmakuTrajectory::new);
    private RegistryProviderLookup<DanmakuTrajectory> owner;

    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack, double x, double y, double z, float xRot, float yRot, float divergence, float offsetDist, IDanmakuItem pThis) {
        this.run(world, livingEntity, stack, x, y, z, xRot, yRot, divergence, offsetDist, pThis, DanmakuTrajectoryConfig.EMPTY);
    }

    /**
     * @param config per-item parameters; use the implementation's defaults when fields are unset
     */
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack, double x, double y, double z, float xRot, float yRot, float divergence, float offsetDist, IDanmakuItem pThis, DanmakuTrajectoryConfig config) {

    }

    /**
     * Parameters written into newly created template items. Old items without
     * the config component fall back to the same values at runtime.
     */
    public DanmakuTrajectoryConfig defaultConfig() {
        return DanmakuTrajectoryConfig.EMPTY;
    }

    public static DanmakuEntity spawnByItemStack(ServerLevel world, @Nullable LivingEntity livingEntity, double x, double y, double z, ItemStack stack, float xRot, float yRot, Float inaccuracy, Float offsetDist) {
        if (!DanmakuVolleyBudget.consume()) {
            return null;
        }
        // The caller owns the origin now. Callers that want eye-level shots pass eyeY.
        // This is required for vertical patterns and view-aligned rings.
        Vec3 viewDirection = direction(xRot, yRot);
        float offset = offsetDist == null ? 0.0f : offsetDist;
        stack = stack.copy();
        DanmakuEntity danmakuEntity = DanmakuEntity.create(world, livingEntity, stack,
                x + viewDirection.x * offset,
                y + viewDirection.y * offset,
                z + viewDirection.z * offset,
                xRot, yRot, inaccuracy == null ? 0.0f : inaccuracy);
        world.addFreshEntity(danmakuEntity);
        return danmakuEntity;
    }

    /**
     * Minecraft view direction (positive pitch looks down).
     */
    public static Vec3 direction(float xRot, float yRot) {
        double yaw = Math.toRadians(yRot);
        double pitch = Math.toRadians(xRot);
        return new Vec3(
                -Math.sin(yaw) * Math.cos(pitch),
                -Math.sin(pitch),
                Math.cos(yaw) * Math.cos(pitch)
        );
    }

    @Override
    public Codec<DanmakuTrajectory> getCodec() {
        return CODEC;
    }
}
