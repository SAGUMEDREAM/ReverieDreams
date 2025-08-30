package cc.thonly.reverie_dreams.entity.misc;


import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@ToString
@SuppressWarnings("unchecked")
public class KnifeEntity extends DanmakuEntity {

    public KnifeEntity(@Nullable Entity livingEntity, ServerWorld world, Double x, Double y, Double z, ItemStack stack, Float pitch, Float yaw, Float speed, Float acceleration, Float divergence, Float offsetDist) {
        super(livingEntity, world, x, y, z, stack, pitch, yaw, speed, acceleration, divergence, offsetDist);
    }

    public KnifeEntity(EntityType<KnifeEntity> knifeEntityEntityType, World world) {
        super((EntityType<DanmakuEntity>) (Object) knifeEntityEntityType, world);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return ModEntityHolders.KNIFE_DISPLAY.getDefaultStack();
    }
}
