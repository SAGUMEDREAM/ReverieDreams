package cc.thonly.reverie_dreams.effect;

import cc.thonly.reverie_dreams.Touhou;
import eu.pb4.polymer.core.api.other.SimplePolymerPotion;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModPotions {
    public static final RegistryEntry.Reference<Potion> ELIXIR_OF_LIFE_POTION = registerPotion("elixir_of_life", new SimplePolymerPotion("elixir_of_life", new StatusEffectInstance(ModStatusEffects.ELIXIR_OF_LIFE, 3600, 0)));
    public static final RegistryEntry.Reference<Potion> ELIXIR_OF_LIFE_POTION_INF = registerPotion("elixir_of_life_inf", new SimplePolymerPotion("elixir_of_life", new StatusEffectInstance(ModStatusEffects.ELIXIR_OF_LIFE, Integer.MAX_VALUE, 0)));
    public static final RegistryEntry.Reference<Potion> MENTAL_DISORDER_POTION = registerPotion("mental_disorder", new SimplePolymerPotion("mental_disorder", new StatusEffectInstance(ModStatusEffects.MENTAL_DISORDER, 3600, 0)));
    public static final RegistryEntry.Reference<Potion> BACK_OF_LIFE_POTION = registerPotion("back_of_life", new SimplePolymerPotion("back_of_life", new StatusEffectInstance(ModStatusEffects.BACK_OF_LIFE, 3600, 0)));
    public static final RegistryEntry.Reference<Potion> KANJU_KUSURI_POTION = registerPotion("kanju_kusuri", new SimplePolymerPotion("kanju_kusuri", new StatusEffectInstance(ModStatusEffects.KANJU_KUSURI, 3600, 0)));

    public static void init() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {

        });
    }

    public static ItemStack createStack(RegistryEntry<Potion> potion) {
        return PotionContentsComponent.createStack(Items.POTION, potion);
    }

    public static RegistryEntry.Reference<Potion> registerPotion(String id, Potion potion) {
        var reference = Registry.registerReference(Registries.POTION, Touhou.id(id), potion);
        RegistrySyncUtils.setServerEntry(Registries.POTION, potion);
        return reference;
    }
}
