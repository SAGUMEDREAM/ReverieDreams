package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("DATA_CUSTOM_NAME_VISIBLE")
    static EntityDataAccessor<Boolean> getNameVisible() {
        throw new UnsupportedOperationException();
    }
}