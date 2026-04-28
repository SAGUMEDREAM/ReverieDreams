package cc.thonly.reverie_dreams.registry.content.effect;

import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;

public class RDPotions {
    public static List<Holder<Potion>> POTIONS = new ArrayList<>();
    public static Holder<Potion> ELIXIR_OF_LIFE_POTION;
    public static Holder<Potion> ELIXIR_OF_LIFE_POTION_INF;
    public static Holder<Potion> MENTAL_DISORDER_POTION;
    public static Holder<Potion> BACK_OF_LIFE_POTION;
    public static Holder<Potion> KANJU_KUSURI_POTION;

    public static void initialize(BalmRegistrar.Scoped<Potion> scoped) {
        ELIXIR_OF_LIFE_POTION = registerPotion(scoped, "elixir_of_life", new Potion("elixir_of_life", new MobEffectInstance(RDStatusEffects.ELIXIR_OF_LIFE, 3600, 0)));
        ELIXIR_OF_LIFE_POTION_INF = registerPotion(scoped, "elixir_of_life_inf", new Potion("elixir_of_life", new MobEffectInstance(RDStatusEffects.ELIXIR_OF_LIFE, Integer.MAX_VALUE, 0)));
        MENTAL_DISORDER_POTION = registerPotion(scoped, "mental_disorder", new Potion("mental_disorder", new MobEffectInstance(RDStatusEffects.MENTAL_DISORDER, 3600, 0)));
        BACK_OF_LIFE_POTION = registerPotion(scoped, "back_of_life", new Potion("back_of_life", new MobEffectInstance(RDStatusEffects.BACK_OF_LIFE, 3600, 0)));
        KANJU_KUSURI_POTION = registerPotion(scoped, "kanju_kusuri", new Potion("kanju_kusuri", new MobEffectInstance(RDStatusEffects.KANJU_KUSURI, 3600, 0)));
    }

    public static ItemStack createStack(Holder<Potion> potion) {
        return PotionContents.createItemStack(Items.POTION, potion);
    }

    public static Holder<Potion> registerPotion(BalmRegistrar.Scoped<Potion> scoped, String id, Potion potion) {
        var holder = scoped.register(id, val -> potion);
        POTIONS.add(holder);
        return holder;
    }
}
