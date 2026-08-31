package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(HolderSet.Direct.class)
public interface HolderSetDirectAccessor<T> {
    @Invoker("<init>")
    public static <T> HolderSet.Direct<T> invokeInit(List<Holder<T>> contents) {
        throw new AssertionError("By Mixin");
    }
}
