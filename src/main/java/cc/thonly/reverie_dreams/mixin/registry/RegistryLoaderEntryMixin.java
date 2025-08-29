package cc.thonly.reverie_dreams.mixin.registry;

//import cc.thonly.minecraft.api.DynamicRegistryManagerCallback;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.registry.RegistryLoader;
import org.spongepowered.asm.mixin.Mixin;

@Slf4j
@Mixin(RegistryLoader.Entry.class)
public class RegistryLoaderEntryMixin {
//    @Inject(method = "getLoader", at = @At("RETURN"), cancellable = true)
//    private static<T> void getLoader(Lifecycle lifecycle, Map<RegistryKey<?>, Exception> errors, CallbackInfoReturnable<RegistryLoader.Loader<T>> cir) {
//        RegistryLoader.Loader<T> loader = cir.getReturnValue();
//        MutableRegistry<T> mutableRegistry = loader.registry();
//        if (mutableRegistry instanceof SimpleRegistry<T> registry) {
//            DynamicRegistryManagerCallback.start(registry);
//        }
////        System.out.println(mutableRegistry.getKey());
//    }
}
