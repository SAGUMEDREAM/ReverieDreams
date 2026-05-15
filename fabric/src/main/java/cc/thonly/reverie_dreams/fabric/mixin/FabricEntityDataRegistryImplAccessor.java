package cc.thonly.reverie_dreams.fabric.mixin;

import net.fabricmc.fabric.impl.object.builder.FabricTrackedDataRegistryImpl;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FabricTrackedDataRegistryImpl.class)
public interface FabricEntityDataRegistryImplAccessor {
    @Accessor("handlerRegistry")
    static Registry<EntityDataSerializer<?>> getHandlerRegistry() {
        throw new AssertionError();
    }
}