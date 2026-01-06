package cc.thonly.reverie_dreams.registry.content.effect;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;

public class RDPotions {
    public static final List<Potion> POTIONS = new ArrayList<>();
    public static final Holder.Reference<Potion> ELIXIR_OF_LIFE_POTION = registerPotion("elixir_of_life", new Potion("elixir_of_life", new MobEffectInstance(RDStatusEffects.ELIXIR_OF_LIFE, 3600, 0)));
    public static final Holder.Reference<Potion> ELIXIR_OF_LIFE_POTION_INF = registerPotion("elixir_of_life_inf", new Potion("elixir_of_life", new MobEffectInstance(RDStatusEffects.ELIXIR_OF_LIFE, Integer.MAX_VALUE, 0)));
    public static final Holder.Reference<Potion> MENTAL_DISORDER_POTION = registerPotion("mental_disorder", new Potion("mental_disorder", new MobEffectInstance(RDStatusEffects.MENTAL_DISORDER, 3600, 0)));
    public static final Holder.Reference<Potion> BACK_OF_LIFE_POTION = registerPotion("back_of_life", new Potion("back_of_life", new MobEffectInstance(RDStatusEffects.BACK_OF_LIFE, 3600, 0)));
    public static final Holder.Reference<Potion> KANJU_KUSURI_POTION = registerPotion("kanju_kusuri", new Potion("kanju_kusuri", new MobEffectInstance(RDStatusEffects.KANJU_KUSURI, 3600, 0)));

    public static void registerPotions() {

    }

    public static ItemStack createStack(Holder<Potion> potion) {
        return PotionContents.createItemStack(Items.POTION, potion);
    }

    public static Holder.Reference<Potion> registerPotion(String id, Potion potion) {
        var reference = Registry.registerForHolder(BuiltInRegistries.POTION, ReverieDreams.id(id), potion);
        POTIONS.add(potion);
        return reference;
    }
}
