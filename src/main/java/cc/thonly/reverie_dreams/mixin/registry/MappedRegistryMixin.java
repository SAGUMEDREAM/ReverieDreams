package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.inf.SimpleRegistrySetter;
import cc.thonly.reverie_dreams.server.ServerContentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements SimpleRegistrySetter, WritableRegistry<T> {

    @Shadow
    private boolean frozen;

    @Shadow
    private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow @Final public Map<Identifier, Holder.Reference<T>> byLocation;

    @Shadow @Final public Map<T, Holder.Reference<T>> byValue;

    @Unique
    private boolean reverie_dreams$injected = false;

    @Override
    public void reverie_dreams$setFrozen(boolean value) {
        this.frozen = value;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "freeze", at = @At("HEAD"), order = 100)
    public void reverie_dreams$onFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        if (this.reverie_dreams$injected) {
            return;
        }
        this.reverie_dreams$injected = true;
        MappedRegistry<T> registry = (MappedRegistry<T>) (Object) this;
        ServerContentRegistry.IMPL.write(registry);
    }

//    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
//    public void add(RegistryKey<T> key, T value, RegistryEntryInfo info, CallbackInfoReturnable<RegistryEntry.Reference<T>> cir) {
//        Objects.requireNonNull(key);
//        Objects.requireNonNull(value);
//        if (this.idToEntry.containsKey(key.getValue())) {
//            cir.setReturnValue(this.idToEntry.get(key.getValue()));
//            cir.cancel();
//        }
//        if (this.valueToEntry.containsKey(value)) {
//            cir.setReturnValue(this.idToEntry.get(key.getValue()));
//            cir.cancel();
//        }
//    }

//    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
//    public void add(RegistryKey<T> key, T value, RegistryEntryInfo info, CallbackInfoReturnable<RegistryEntry.Reference<T>> cir) {
//        if(intrusiveValueToEntry == null) {
//            this.intrusiveValueToEntry = new IdentityHashMap<>();
//        }
//    }
//
//    @Inject(method = "createEntry", at = @At("HEAD"), cancellable = true)
//    public void createEntry(T value, CallbackInfoReturnable<RegistryEntry.Reference<T>> cir) {
//        if(intrusiveValueToEntry == null) {
//            this.intrusiveValueToEntry = new IdentityHashMap<>();
//        }
//        cir.setReturnValue(this.intrusiveValueToEntry.computeIfAbsent(value, valuex -> RegistryEntry.Reference.intrusive((net.minecraft.registry.block.RegistryEntryOwner<T>) this, valuex)));
//        cir.cancel();
//    }
}
