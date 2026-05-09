package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.component.DataComponentInitializers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(DataComponentInitializers.class)
public interface DataComponentInitializersAccessor {
    @Accessor("initializers")
    List<DataComponentInitializers.InitializerEntry<?>> getInitializers();
}
