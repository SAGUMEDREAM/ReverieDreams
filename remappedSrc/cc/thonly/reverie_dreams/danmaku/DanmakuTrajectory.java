package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@NoArgsConstructor
public class DanmakuTrajectory implements CodecStep<DanmakuTrajectory>, OwnerBinding<DanmakuTrajectory>, BuiltinObject {
    public static final Codec<DanmakuTrajectory> CODEC = Codec.unit(DanmakuTrajectory::new);
    private IntrinsicalRegister<DanmakuTrajectory> owner;

    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack, Double x, Double y, Double z, float pitch, float yaw, float speed, float acceleration, float divergence, float offsetDist, IDanmakuItem pThis) {

    }

    public static DanmakuEntity spawnByItemStack(ServerLevel world, @NotNull LivingEntity livingEntity, ItemStack stack, Float speed, Float acceleration, Float divergence, Float offsetDist) {
        stack = stack.copy();
        double x = livingEntity.getX();
        double y = livingEntity.getY();
        double z = livingEntity.getZ();
        if (offsetDist == null) {
            offsetDist = 1.5f;
        }
        DanmakuEntity danmakuEntity = new DanmakuEntity(
                livingEntity,
                world,
                x, y, z,
                stack,
                livingEntity.getXRot(),
                livingEntity.getYRot(),
                speed,
                acceleration,
                divergence,
                offsetDist
        );
        world.addFreshEntity(danmakuEntity);
        return danmakuEntity;
    }

    public static DanmakuEntity spawnByItemStack(ServerLevel world, @Nullable LivingEntity livingEntity, Double x, Double y, Double z, ItemStack stack, Float pitch, Float yaw, Float speed, Float acceleration, Float divergence, Float offsetDist) {
        stack = stack.copy();
        if (offsetDist == null) {
            offsetDist = 1.5f;
        }
        DanmakuEntity danmakuEntity = new DanmakuEntity(
                livingEntity,
                world,
                x, y, z,
                stack,
                pitch,
                yaw,
                speed,
                acceleration,
                divergence,
                offsetDist
        );
        world.addFreshEntity(danmakuEntity);
        return danmakuEntity;
    }

    @Override
    public Codec<DanmakuTrajectory> getCodec() {
        return CODEC;
    }
}
