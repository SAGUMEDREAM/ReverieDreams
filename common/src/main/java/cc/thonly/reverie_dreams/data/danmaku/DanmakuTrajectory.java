package cc.thonly.reverie_dreams.data.danmaku;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DanmakuTrajectory implements CodecStep<DanmakuTrajectory>, RegistryEntryOwnerBindable<DanmakuTrajectory>, BuiltinObject {
    public static final Codec<DanmakuTrajectory> CODEC = UnitCodec.unit(DanmakuTrajectory::new);
    private RegistryImpl<DanmakuTrajectory> owner;

    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack, double x, double y, double z, float xRot, float yRot, float divergence, float offsetDist, IDanmakuItem pThis) {

    }

    public static DanmakuEntity spawnByItemStack(ServerLevel world, @Nullable LivingEntity livingEntity, double x, double y, double z, ItemStack stack, float xRot, float yRot, Float inaccuracy, Float offsetDist) {
        stack = stack.copy();
        DanmakuEntity danmakuEntity = DanmakuEntity.create(world, livingEntity, stack, x, livingEntity == null ? y : livingEntity.getEyeY(), z, xRot, yRot, inaccuracy);
        world.addFreshEntity(danmakuEntity);
        return danmakuEntity;
    }

    @Override
    public Codec<DanmakuTrajectory> getCodec() {
        return CODEC;
    }
}
