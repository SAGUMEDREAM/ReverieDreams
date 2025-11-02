package cc.thonly.reverie_dreams.effect;

import cc.thonly.reverie_dreams.ReverieDreams;
import eu.pb4.polymer.core.api.other.SimplePolymerPotion;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public class ModPotions {
    public static final Holder.Reference<Potion> ELIXIR_OF_LIFE_POTION = registerPotion("elixir_of_life", new SimplePolymerPotion("elixir_of_life", new MobEffectInstance(ModStatusEffects.ELIXIR_OF_LIFE, 3600, 0)));
    public static final Holder.Reference<Potion> ELIXIR_OF_LIFE_POTION_INF = registerPotion("elixir_of_life_inf", new SimplePolymerPotion("elixir_of_life", new MobEffectInstance(ModStatusEffects.ELIXIR_OF_LIFE, Integer.MAX_VALUE, 0)));
    public static final Holder.Reference<Potion> MENTAL_DISORDER_POTION = registerPotion("mental_disorder", new SimplePolymerPotion("mental_disorder", new MobEffectInstance(ModStatusEffects.MENTAL_DISORDER, 3600, 0)));
    public static final Holder.Reference<Potion> BACK_OF_LIFE_POTION = registerPotion("back_of_life", new SimplePolymerPotion("back_of_life", new MobEffectInstance(ModStatusEffects.BACK_OF_LIFE, 3600, 0)));
    public static final Holder.Reference<Potion> KANJU_KUSURI_POTION = registerPotion("kanju_kusuri", new SimplePolymerPotion("kanju_kusuri", new MobEffectInstance(ModStatusEffects.KANJU_KUSURI, 3600, 0)));

    public static void init() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {

        });
    }

    public static ItemStack createStack(Holder<Potion> potion) {
        return PotionContents.createItemStack(Items.POTION, potion);
    }

    public static Holder.Reference<Potion> registerPotion(String id, Potion potion) {
        var reference = Registry.registerForHolder(BuiltInRegistries.POTION, ReverieDreams.id(id), potion);
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.POTION, potion);
        return reference;
    }
}
