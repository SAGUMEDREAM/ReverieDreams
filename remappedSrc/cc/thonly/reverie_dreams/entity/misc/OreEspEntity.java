package cc.thonly.reverie_dreams.entity.misc;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class OreEspEntity extends Display.BlockDisplay {
    public int lifetime = 100;

    public OreEspEntity(EntityType<?> entityType, Level world ) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        this.lifetime--;
        if (lifetime <= 0||this.level().getBlockState(this.blockPosition()).getBlock()!=this.getBlockState().getBlock()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

}
