package cc.thonly.reverie_dreams.registry.content.effect;

import cc.thonly.reverie_dreams.ReverieDreams;

import java.util.ArrayList;
import java.util.List;

import cc.thonly.reverie_dreams.effect.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class RDStatusEffects {
    public static final List<Holder<MobEffect>> REVERIE_DREAMS_EFFECTS = new ArrayList<>();
    public static final Holder<MobEffect> EMPTY = registerEffect("empty_effect", new EmptyEffect());
    public static final Holder<MobEffect> ELIXIR_OF_LIFE = registerEffect("elixir_of_life", new ElixirOfLifeEffect());
    public static final Holder<MobEffect> MENTAL_DISORDER = registerEffect("mental_disorder", new MentalDisorder());
    public static final Holder<MobEffect> BACK_OF_LIFE = registerEffect("back_of_life", new BackOfLifeEffect());
    public static final Holder<MobEffect> KANJU_KUSURI = registerEffect("kanju_kansuri", new KanjuKusuriEffect());

    public static void registerEffects() {
    }

    private static Holder<MobEffect> registerEffect(String id, MobEffect statusEffect) {
        Holder.Reference<MobEffect> reference = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, ReverieDreams.id(id), statusEffect);
        REVERIE_DREAMS_EFFECTS.add(reference);
        return reference;
    }
}
