package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Holder.Reference.class)
public interface HolderReferenceAccessors<T> {
    @Accessor("value")
    void reverie_dreams$setValue(T value);
}
