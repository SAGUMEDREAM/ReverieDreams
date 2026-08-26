package cc.thonly.reverie_dreams.registry.content.effect;

import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RDPotions {
    public static List<RegistrySupplier<Potion>> POTIONS = new ArrayList<>();
    public static RegistrySupplier<Potion> ELIXIR_OF_LIFE_POTION = registerPotion("elixir_of_life", () -> new Potion("elixir_of_life", new MobEffectInstance(RDStatusEffects.ELIXIR_OF_LIFE.builtInHolder(), 3600, 0)));
    public static RegistrySupplier<Potion> ELIXIR_OF_LIFE_POTION_INF = registerPotion("elixir_of_life_inf", () -> new Potion("elixir_of_life", new MobEffectInstance(RDStatusEffects.ELIXIR_OF_LIFE.builtInHolder(), -1, 0)));
    public static RegistrySupplier<Potion> MENTAL_DISORDER_POTION = registerPotion("mental_disorder", () -> new Potion("mental_disorder", new MobEffectInstance(RDStatusEffects.MENTAL_DISORDER.builtInHolder(), 3600, 0)));
    public static RegistrySupplier<Potion> BACK_OF_LIFE_POTION = registerPotion("back_of_life", () -> new Potion("back_of_life", new MobEffectInstance(RDStatusEffects.BACK_OF_LIFE.builtInHolder(), 3600, 0)));
    public static RegistrySupplier<Potion> KANJU_KUSURI_POTION = registerPotion("kanju_kusuri", () -> new Potion("kanju_kusuri", new MobEffectInstance(RDStatusEffects.KANJU_KUSURI.builtInHolder(), 3600, 0)));

    public static void initialize() {
    }

    public static ItemStackTemplate createStackTemplate(RegistrySupplier<Potion> potion) {
        ItemStackTemplate template = ItemStackTemplateHelper.create(Items.POTION);
        ItemStackTemplateHelper.modify(template, (old, modifier) -> {
            modifier.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        });
        return template;
    }

    public static RegistrySupplier<Potion> registerPotion(String id, Supplier<Potion> potion) {
        var holder = MCBuiltInRegistries.POTION.register(id, potion);
        POTIONS.add(holder);
        return holder;
    }
}
