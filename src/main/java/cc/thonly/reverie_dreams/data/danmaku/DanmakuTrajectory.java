package cc.thonly.reverie_dreams.data.danmaku;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
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
    private RegistryHandler<DanmakuTrajectory> owner;

    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack, Double x, Double y, Double z, float pitch, float yaw, float divergence, float offsetDist, IDanmakuItem pThis) {

    }

    public static DanmakuEntity spawnByItemStack(ServerLevel world, @NotNull LivingEntity livingEntity, ItemStack stack, Float divergence, Float offsetDist) {
        stack = stack.copy();
        double x = livingEntity.getX();
        double y = livingEntity.getY();
        double z = livingEntity.getZ();
        if (offsetDist == null) {
            offsetDist = 1.5f;
        }
        DanmakuProperties properties = stack.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault());
        DanmakuEntity danmakuEntity = new DanmakuEntity(
                livingEntity,
                world,
                x, y, z,
                stack,
                properties,
                livingEntity.getXRot(), livingEntity.getYRot(),
                divergence, offsetDist
        );
        world.addFreshEntity(danmakuEntity);
        return danmakuEntity;
    }

    public static DanmakuEntity spawnByItemStack(ServerLevel world, @Nullable LivingEntity livingEntity, Double x, Double y, Double z, ItemStack stack, Float pitch, Float yaw, Float divergence, Float offsetDist) {
        stack = stack.copy();
        if (offsetDist == null) {
            offsetDist = 1.5f;
        }
        DanmakuProperties properties = stack.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault());
        DanmakuEntity danmakuEntity = new DanmakuEntity(
                livingEntity,
                world,
                x, y, z,
                stack,
                properties,
                pitch, yaw,
                divergence, offsetDist
        );
        world.addFreshEntity(danmakuEntity);
        return danmakuEntity;
    }

    @Override
    public Codec<DanmakuTrajectory> getCodec() {
        return CODEC;
    }
}
