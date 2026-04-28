package cc.thonly.reverie_dreams.neoforge.mixin;

import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
//    @Unique
//    private static final ThreadLocal<Boolean> reverie_dreams$IS_SERVER = ThreadLocal.withInitial(() -> false);
//
//    /**
//     * Sets IS_SERVER flag. Note that this must be reset after call, as the render thread
//     * invokes this method as well.
//     */
//    @WrapOperation(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryDataLoader;load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Ljava/util/List;Ljava/util/List;Z)Lnet/minecraft/core/RegistryAccess$Frozen;"))
//    private static RegistryAccess.Frozen wrapIsServerCall(@Coerce Object registryLoadable, List<HolderLookup.RegistryLookup<?>> baseRegistries, List<RegistryDataLoader.RegistryData<?>> entries, boolean b, Operation<RegistryAccess.Frozen> original) {
//        try {
//            reverie_dreams$IS_SERVER.set(true);
//            return original.call(registryLoadable, baseRegistries, entries, b);
//        } finally {
//            reverie_dreams$IS_SERVER.set(false);
//        }
//    }
//
//    @SuppressWarnings("InvalidInjectorMethodSignature")
//    @Inject(
//            method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Ljava/util/List;Ljava/util/List;Z)Lnet/minecraft/core/RegistryAccess$Frozen;",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
//                    ordinal = 0
//            )
//    )
//    private static void beforeLoad(@Coerce Object registryLoadable,
//                                   List<HolderLookup.RegistryLookup<?>> baseRegistries,
//                                   List<RegistryDataLoader.RegistryData<?>> entries,
//                                   boolean fromResources,
//                                   CallbackInfoReturnable<RegistryAccess.Frozen> cir,
//                                   @Local(ordinal = 0, argsOnly = true) List<RegistryDataLoader.Loader<?>> registriesList
//    ) {
//        if (!reverie_dreams$IS_SERVER.get()) return;
//
//        Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries = new IdentityHashMap<>(registriesList.size());
//
//        for (RegistryDataLoader.Loader<?> entry : registriesList) {
//            registries.put(entry.registry().key(), entry.registry());
//        }
//
//        DynamicRegistrySetupCallback.EVENT.invoker().onRegistrySetup(new DynamicRegistryViewImpl(registries));
//    }
}
