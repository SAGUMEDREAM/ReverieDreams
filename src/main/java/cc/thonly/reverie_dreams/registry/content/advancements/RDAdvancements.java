package cc.thonly.reverie_dreams.registry.content.advancements;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RDAdvancements {
    public static final ResourceLocation ADVANCEMENT_BACKGROUND = ResourceLocation.withDefaultNamespace("gui/advancements/backgrounds/stone");
    public static final ResourceKey<Advancement> ROOT = getOrCreateKey("root");
    public static final ResourceKey<Advancement> DANMAKU_TABLE = getOrCreateKey("danmaku_table");
    public static final ResourceKey<Advancement> DANMAKU_WARS = getOrCreateKey("danmaku_wars");
    public static final ResourceKey<Advancement> DANMAKU_UPGRADE = getOrCreateKey("danmaku_upgrade");
    public static final ResourceKey<Advancement> DARK_CUISINE = getOrCreateKey("dark_cuisine");
    public static final ResourceKey<Advancement> ABANDONED_SHRINE = getOrCreateKey("abandoned_shrine");
    public static final ResourceKey<Advancement> ENTER_DREAM = getOrCreateKey("enter_dream");
    public static final ResourceKey<Advancement> FUMOFUMO = getOrCreateKey("fumofumo");
    public static final ResourceKey<Advancement> SHINY_COINS = getOrCreateKey("shiny_coins");
    public static final ResourceKey<Advancement> LAYLA_PRISMRIVER = getOrCreateKey("layla_prismriver");
    public static final ResourceKey<Advancement> WOOD_WITH_SPIRITUAL_POWER = getOrCreateKey("wood_with_spiritual_power");
    public static final ResourceKey<Advancement> GENSOKYO_ALTAR_CRAFTING = getOrCreateKey("gensokyo_altar_crafting");
    public static final ResourceKey<Advancement> PSYCHOLOGIST = getOrCreateKey("psychologist");
    public static final ResourceKey<Advancement> MAKE_FRIEND = getOrCreateKey("make_friend");
    public static final ResourceKey<Advancement> TAKING_PHOTO = getOrCreateKey("taking_photo");
    public static final ResourceKey<Advancement> I_WILL_TAKE_YOUR_SOUL = getOrCreateKey("i_will_take_your_soul");
    public static final ResourceKey<Advancement> BURST = getOrCreateKey("burst");
    public static final ResourceKey<Advancement> LEVEL_UP = getOrCreateKey("level_up");
    public static final ResourceKey<Advancement> REHABILITATION_EXPERT = getOrCreateKey("rehabilitation_expert");
    public static final ResourceKey<Advancement> A_CELESTIAL_BEING_DESCENDED_TO_EARTH = getOrCreateKey("a_celestial_being_descended_to_earth");
    public static final ResourceKey<Advancement> TOUHOU_MYSTIA_IZAKAYA = getOrCreateKey("touhou_mystias_izakaya");
    public static final ResourceKey<Advancement> COOKING_BY_MYSELF = getOrCreateKey("cooking_by_myself");
    public static final ResourceKey<Advancement> COOKING_BY_MYSELF_AMOUNT_5_TAG = getOrCreateKey("cooking_by_myself_amount_5_tag");
    public static final ResourceKey<Advancement> DELICACY = getOrCreateKey("delicacy");
    public static final ResourceKey<Advancement> FINE_WINE = getOrCreateKey("fine_wine");

    public static String getRootKey() {
        return ROOT.location().toLanguageKey("title");
    }

    public static MutableComponent getTitleComponent(ResourceKey<Advancement> key) {
        return Component.empty().append(Component.translatable(getTitleKey(key)));
    }

    public static MutableComponent getDescriptionComponent(ResourceKey<Advancement> key) {
        return Component.empty().append(Component.translatable(getDescriptionKey(key)));
    }

    public static String getTitleKey(ResourceKey<Advancement> key) {
        return key.location().toLanguageKey("title");
    }

    public static String getDescriptionKey(ResourceKey<Advancement> key) {
        return key.location().toLanguageKey("description");
    }

    public static ResourceKey<Advancement> getOrCreateKey(String name) {
        return ResourceKey.create(Registries.ADVANCEMENT, ReverieDreams.id(name));
    }
}
