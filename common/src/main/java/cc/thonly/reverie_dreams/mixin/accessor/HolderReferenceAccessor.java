package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Holder.Reference.class)
public interface HolderReferenceAccessor<T> {
    @Accessor("value")
    void reverie_dreams$setValue(T value);

    @Accessor("key")
    ResourceKey<T> reverie_dreams$getKeyOrEmpty();

    @Accessor("value")
    T reverie_dreams$getValueOrEmpty();
}
