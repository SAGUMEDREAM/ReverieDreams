package cc.thonly.reverie_dreams.fabric.datagen.lang;

import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;

public interface EffectLang {

    static void add(TranslationBuilder builder) {
        builder.add(RDStatusEffects.EMPTY.value(), "绝密");
        builder.add(RDStatusEffects.ELIXIR_OF_LIFE.value(), "不死");
        builder.add(RDStatusEffects.MENTAL_DISORDER.value(), "精神错乱");
        builder.add(RDStatusEffects.BACK_OF_LIFE.value(), "返生");
        builder.add(RDStatusEffects.KANJU_KUSURI.value(), "绀珠");
        builder.add(RDStatusEffects.ANTI_ALCOHOL.value(), "抗酒精");
        addPotion(builder, RDPotions.ELIXIR_OF_LIFE_POTION.value(), "蓬莱之药", "喷溅型蓬莱之药", "滞留型蓬莱之药");
        addPotion(builder, RDPotions.MENTAL_DISORDER_POTION.value(), "精神错乱药水", "喷溅型精神错乱药水", "滞留型精神错乱药水");
        addPotion(builder, RDPotions.BACK_OF_LIFE_POTION.value(), "还生药", "喷溅型还生药", "滞留型还生药");
        addPotion(builder, RDPotions.KANJU_KUSURI_POTION.value(), "绀珠药", "喷溅型绀珠药", "滞留型绀珠药");
        builder.addEnchantment(RDEnchantments.EXTERMINATION, "退治");
        builder.addEnchantment(RDEnchantments.MOON_DAMAGE, "月伤");
        builder.addEnchantment(RDEnchantments.DANMAKU_PROTECTION, "弹幕保护");
        builder.addEnchantment(RDEnchantments.POWERFUL, "威力");
        builder.addEnchantment(RDEnchantments.FROZEN, "冰封");
        builder.addEnchantment(RDEnchantments.CHARGE, "冲锋");
    }

    private static void addPotion(TranslationBuilder builder, Potion potion, String normal, String splash, String lingering) {
        builder.add(Items.POTION.getDescriptionId() + ".effect." + potion.name(), normal);
        builder.add(Items.SPLASH_POTION.getDescriptionId() + ".effect." + potion.name(), splash);
        builder.add(Items.LINGERING_POTION.getDescriptionId() + ".effect." + potion.name(), lingering);
        builder.add(Items.TIPPED_ARROW.getDescriptionId() + ".effect." + potion.name(), normal);
    }
}
