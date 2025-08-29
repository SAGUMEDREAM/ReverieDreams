package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleInteractionEvent;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@NoArgsConstructor
public class DanmakuTrajectory implements CodecStep<DanmakuTrajectory>, OwnerBinding<DanmakuTrajectory>, BuiltinObject {
    public static final Codec<DanmakuTrajectory> CODEC = Codec.unit(DanmakuTrajectory::new);
    private IntrinsicalRegister<DanmakuTrajectory> owner;

    public void run(ServerWorld world, @Nullable LivingEntity livingEntity, ItemStack stack, Double x, Double y, Double z, float pitch, float yaw, float speed, float acceleration, float divergence, float offsetDist, IDanmakuItem pThis) {

    }

    public static DanmakuEntity spawnByItemStack(ServerWorld world, @NotNull LivingEntity livingEntity, ItemStack stack, Float speed, Float acceleration, Float divergence, Float offsetDist) {
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
                livingEntity.getPitch(),
                livingEntity.getYaw(),
                speed,
                acceleration,
                divergence,
                offsetDist
        );
        world.spawnEntity(danmakuEntity);
        return danmakuEntity;
    }

    public static DanmakuEntity spawnByItemStack(ServerWorld world, @Nullable LivingEntity livingEntity, Double x, Double y, Double z, ItemStack stack, Float pitch, Float yaw, Float speed, Float acceleration, Float divergence, Float offsetDist) {
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
        world.spawnEntity(danmakuEntity);
        return danmakuEntity;
    }

    @Override
    public Codec<DanmakuTrajectory> getCodec() {
        return CODEC;
    }
}
