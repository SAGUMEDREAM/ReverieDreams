package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DyedItemColor.class)
public interface DyedItemColorAccessor {
    @Accessor("rgb")
    void setRgb(int rgb);
}
