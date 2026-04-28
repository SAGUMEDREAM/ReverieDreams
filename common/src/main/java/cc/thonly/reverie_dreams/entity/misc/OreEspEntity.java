package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.mixin.accessor.BlockDisplayAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class OreEspEntity extends Display.BlockDisplay {
    public int lifetime = 100;

    public OreEspEntity(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();
        BlockDisplayAccessor accessor = (BlockDisplayAccessor) this;
        if (this.level().isClientSide()) return;
        this.lifetime--;
        if (lifetime <= 0 || this.level().getBlockState(this.blockPosition()).getBlock() != accessor.reverie_dreams$getBlockState().getBlock()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

}
