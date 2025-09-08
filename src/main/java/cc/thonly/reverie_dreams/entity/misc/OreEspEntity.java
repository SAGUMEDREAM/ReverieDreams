package cc.thonly.reverie_dreams.entity.misc;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.world.World;

public class OreEspEntity extends DisplayEntity.BlockDisplayEntity {
    public int lifetime = 100;

    public OreEspEntity(EntityType<?> entityType, World world ) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;
        this.lifetime--;
        if (lifetime <= 0||this.getWorld().getBlockState(this.getBlockPos()).getBlock()!=this.getBlockState().getBlock()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

}
