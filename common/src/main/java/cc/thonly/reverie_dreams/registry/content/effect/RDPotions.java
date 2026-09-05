package cc.thonly.reverie_dreams.registry.content.effect;

import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RDPotions {
    public static List<RegistryDelegate<Potion>> POTIONS = new ArrayList<>();
    public static RegistryDelegate<Potion> ELIXIR_OF_LIFE_POTION = registerPotion("elixir_of_life", () -> new Potion("elixir_of_life", new MobEffectInstance(RDStatusEffects.ELIXIR_OF_LIFE.builtInHolder(), 3600, 0)));
    public static RegistryDelegate<Potion> ELIXIR_OF_LIFE_POTION_INF = registerPotion("elixir_of_life_inf", () -> new Potion("elixir_of_life", new MobEffectInstance(RDStatusEffects.ELIXIR_OF_LIFE.builtInHolder(), -1, 0)));
    public static RegistryDelegate<Potion> MENTAL_DISORDER_POTION = registerPotion("mental_disorder", () -> new Potion("mental_disorder", new MobEffectInstance(RDStatusEffects.MENTAL_DISORDER.builtInHolder(), 3600, 0)));
    public static RegistryDelegate<Potion> BACK_OF_LIFE_POTION = registerPotion("back_of_life", () -> new Potion("back_of_life", new MobEffectInstance(RDStatusEffects.BACK_OF_LIFE.builtInHolder(), 3600, 0)));
    public static RegistryDelegate<Potion> KANJU_KUSURI_POTION = registerPotion("kanju_kusuri", () -> new Potion("kanju_kusuri", new MobEffectInstance(RDStatusEffects.KANJU_KUSURI.builtInHolder(), 3600, 0)));

    public static void initialize() {
    }

    public static ItemStackTemplate createStackTemplate(RegistryDelegate<Potion> potion) {
        ItemStackTemplate template = ItemStackTemplateHelper.create(Items.POTION);
        ItemStackTemplateHelper.modify(template, (old, modifier) -> {
            modifier.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        });
        return template;
    }

    public static RegistryDelegate<Potion> registerPotion(String id, Supplier<Potion> potion) {
        var holder = MCBuiltInRegistries.POTION.register(id, potion);
        POTIONS.add(holder);
        return holder;
    }
}
