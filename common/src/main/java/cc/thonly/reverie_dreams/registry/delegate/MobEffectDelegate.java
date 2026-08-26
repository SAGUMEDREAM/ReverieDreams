package cc.thonly.reverie_dreams.registry.delegate;

import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.experimental.Delegate;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

@SuppressWarnings("unchecked")
public class MobEffectDelegate implements Holder<MobEffect>, DeferredSupplier<MobEffect> {
    @Delegate
    final RegistrySupplier<MobEffect> effectHolder;

    public MobEffectDelegate(RegistrySupplier<MobEffect> effectHolder) {
        this.effectHolder = effectHolder;
    }

    public static MobEffectDelegate of(RegistrySupplier<MobEffect> effectHolder) {
        return new MobEffectDelegate(effectHolder);
    }

    public Holder<MobEffect> builtInHolder() {
        return this.getRegistrar().getHolder(this.effectHolder.getId());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof Holder<?> holder && this.is((Holder<MobEffect>) holder));
    }
}
