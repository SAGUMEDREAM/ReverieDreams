package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HolderSet.Named.class)
public interface NamedAccessor<T> {
    @Invoker("<init>")
    static <T> HolderSet.Named<T> callNew(HolderOwner<T> holderOwner, TagKey<T> tagKey) {
        throw new AssertionError();
    }
}
