package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.datafixer.DataFixerContentManager;
import cc.thonly.reverie_dreams.inf.SimpleRegistrySetter;
import net.fabricmc.fabric.api.event.registry.FabricRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(MappedRegistry.class)
public abstract class SimpleRegistryMixin<T> implements SimpleRegistrySetter, WritableRegistry<T> {

    @Shadow
    private boolean frozen;

    @Shadow
    private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow @Final public Map<ResourceLocation, Holder.Reference<T>> byLocation;

    @Shadow @Final public Map<T, Holder.Reference<T>> byValue;

    @Override
    public void setFrozen(boolean value) {
        this.frozen = value;
    }

    @Inject(method = "freeze", at = @At("HEAD"))
    public void onFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        for (Map.Entry<Registry<?>, Map<ResourceLocation, ResourceLocation>> registryMapEntry : DataFixerContentManager.ENTRIES.entrySet()) {
            Registry<?> key = registryMapEntry.getKey();
            FabricRegistry fabricRegistry = this;
            if (key.equals(fabricRegistry)) {
                Map<ResourceLocation, ResourceLocation> old2new = registryMapEntry.getValue();
                for (Map.Entry<ResourceLocation, ResourceLocation> old2newEntry : old2new.entrySet()) {
                    ResourceLocation oldId = old2newEntry.getKey();
                    ResourceLocation newId = old2newEntry.getValue();
                    this.addAlias(oldId, newId);
                }
            }
        }
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
