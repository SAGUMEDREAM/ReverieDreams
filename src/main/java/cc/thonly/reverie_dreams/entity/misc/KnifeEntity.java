package cc.thonly.reverie_dreams.entity.misc;


import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@ToString
@SuppressWarnings("unchecked")
public class KnifeEntity extends DanmakuEntity {

    public KnifeEntity(@Nullable Entity livingEntity,
                       ServerLevel world,
                       Double x, Double y, Double z,
                       ItemStack stack,
                       Float pitch, Float yaw,
                       DanmakuProperties properties,
                       Float divergence, Float offsetDist) {
        super(livingEntity, world, x, y, z, stack, properties, pitch, yaw, divergence, offsetDist);
    }

    public KnifeEntity(EntityType<KnifeEntity> knifeEntityEntityType, Level world) {
        super((EntityType<DanmakuEntity>) (Object) knifeEntityEntityType, world);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModEntityHolders.KNIFE_DISPLAY.getDefaultInstance();
    }
}
