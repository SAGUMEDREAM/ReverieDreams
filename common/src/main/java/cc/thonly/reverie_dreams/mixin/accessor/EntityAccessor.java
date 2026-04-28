package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("DATA_CUSTOM_NAME_VISIBLE")
    static EntityDataAccessor<Boolean> getNameVisible() {
        throw new UnsupportedOperationException();
    }

    @Invoker("readAdditionalSaveData")
    void reverie_dreams$readAdditionalSaveData(ValueInput input);

    @Invoker("addAdditionalSaveData")
    void reverie_dreams$addAdditionalSaveData(ValueOutput output);

}