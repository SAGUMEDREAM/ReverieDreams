package cc.thonly.reverie_dreams.registry.delegate;

import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.experimental.Delegate;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public class SoundDelegate implements Holder<SoundEvent>, DeferredSupplier<SoundEvent> {
    @Delegate
    final RegistrySupplier<SoundEvent> supplier;

    public SoundDelegate(RegistrySupplier<SoundEvent> supplier) {
        this.supplier = supplier;
    }

    public static SoundDelegate of(RegistrySupplier<SoundEvent> supplier) {
        return new SoundDelegate(supplier);
    }


    public SoundEvent asSoundEvent() {
        return this.supplier.get();
    }
}
