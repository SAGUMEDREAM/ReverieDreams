package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.entity.decoration.Mannequin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mannequin.class)
public interface MannequinAccessor {
    @Accessor("ALL_LAYERS")
    public static byte getAllLayers() {
        throw new AssertionError();
    }
}
