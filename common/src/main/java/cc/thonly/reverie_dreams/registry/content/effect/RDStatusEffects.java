package cc.thonly.reverie_dreams.registry.content.effect;

import cc.thonly.reverie_dreams.effect.*;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.delegate.MobEffectDelegate;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RDStatusEffects {
    public static final List<Holder<MobEffect>> EFFECTS = new ArrayList<>();
    public static final MobEffectDelegate EMPTY = register("empty_effect", EmptyEffect::new);
    public static final MobEffectDelegate ELIXIR_OF_LIFE = register("elixir_of_life", ElixirOfLifeEffect::new);
    public static final MobEffectDelegate MENTAL_DISORDER = register("mental_disorder", MentalDisorder::new);
    public static final MobEffectDelegate BACK_OF_LIFE = register("back_of_life", BackOfLifeEffect::new);
    public static final MobEffectDelegate KANJU_KUSURI = register("kanju_kansuri", KanjuKusuriEffect::new);
    public static final MobEffectDelegate ANTI_ALCOHOL = register("anti_alcohol", AntiAlcohol::new);

    public static void initialize() {
        EFFECTS.addAll(List.of(EMPTY, ELIXIR_OF_LIFE, MENTAL_DISORDER, BACK_OF_LIFE, KANJU_KUSURI, ANTI_ALCOHOL));
    }

    public static MobEffectDelegate register(String name, Supplier<MobEffect> supplier) {
        RegistryDelegate<MobEffect> registrySupplier = MCBuiltInRegistries.MOB_EFFECT.register(name, supplier);
        return MobEffectDelegate.of(registrySupplier);
    }

}
