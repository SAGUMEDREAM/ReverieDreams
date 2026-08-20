package cc.thonly.reverie_dreams.registry.content.effect;

import cc.thonly.reverie_dreams.effect.*;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

public class RDStatusEffects {
    public static final List<Holder<MobEffect>> EFFECTS = new ArrayList<>();
    public static final Holder<MobEffect> EMPTY = MCBuiltInRegistries.MOB_EFFECT.register("empty_effect", EmptyEffect::new);
    public static final Holder<MobEffect> ELIXIR_OF_LIFE = MCBuiltInRegistries.MOB_EFFECT.register("elixir_of_life", ElixirOfLifeEffect::new);
    public static final Holder<MobEffect> MENTAL_DISORDER = MCBuiltInRegistries.MOB_EFFECT.register("mental_disorder", MentalDisorder::new);
    public static final Holder<MobEffect> BACK_OF_LIFE = MCBuiltInRegistries.MOB_EFFECT.register("back_of_life", BackOfLifeEffect::new);
    public static final Holder<MobEffect> KANJU_KUSURI = MCBuiltInRegistries.MOB_EFFECT.register("kanju_kansuri", KanjuKusuriEffect::new);

    public static void initialize() {
        EFFECTS.addAll(List.of(EMPTY, ELIXIR_OF_LIFE, MENTAL_DISORDER, BACK_OF_LIFE, KANJU_KUSURI));
    }

}
