package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.datafixer.DataFixerContentManager;
import cc.thonly.reverie_dreams.interfaces.SimpleRegistrySetter;
import net.fabricmc.fabric.api.event.registry.FabricRegistry;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements SimpleRegistrySetter, MutableRegistry<T> {

    @Shadow
    private boolean frozen;

    @Shadow
    private @Nullable Map<T, RegistryEntry.Reference<T>> intrusiveValueToEntry;

    @Shadow @Final public Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Shadow @Final public Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Override
    public void setFrozen(boolean value) {
        this.frozen = value;
    }

    @Inject(method = "freeze", at = @At("HEAD"))
    public void onFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        for (Map.Entry<Registry<?>, Map<Identifier, Identifier>> registryMapEntry : DataFixerContentManager.ENTRIES.entrySet()) {
            Registry<?> key = registryMapEntry.getKey();
            FabricRegistry fabricRegistry = this;
            if (key.equals(fabricRegistry)) {
                Map<Identifier, Identifier> old2new = registryMapEntry.getValue();
                for (Map.Entry<Identifier, Identifier> old2newEntry : old2new.entrySet()) {
                    Identifier oldId = old2newEntry.getKey();
                    Identifier newId = old2newEntry.getValue();
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
