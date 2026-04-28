package cc.thonly.reverie_dreams.registry.content.effect;

import cc.thonly.reverie_dreams.effect.*;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

public class RDStatusEffects {
    public static final List<Holder<MobEffect>> EFFECTS = new ArrayList<>();
    public static Holder<MobEffect> EMPTY;
    public static Holder<MobEffect> ELIXIR_OF_LIFE;
    public static Holder<MobEffect> MENTAL_DISORDER;
    public static Holder<MobEffect> BACK_OF_LIFE;
    public static Holder<MobEffect> KANJU_KUSURI;

    public static void initialize(BalmRegistrar.Scoped<MobEffect> scoped) {
        EMPTY = scoped.register("empty_effect", id -> new EmptyEffect());
        ELIXIR_OF_LIFE = scoped.register("elixir_of_life", id -> new ElixirOfLifeEffect());
        MENTAL_DISORDER = scoped.register("mental_disorder", id -> new MentalDisorder());
        BACK_OF_LIFE = scoped.register("back_of_life", id -> new BackOfLifeEffect());
        KANJU_KUSURI = scoped.register("kanju_kansuri", id -> new KanjuKusuriEffect());
        EFFECTS.addAll(List.of(EMPTY, ELIXIR_OF_LIFE, MENTAL_DISORDER, BACK_OF_LIFE, KANJU_KUSURI));
    }

}
