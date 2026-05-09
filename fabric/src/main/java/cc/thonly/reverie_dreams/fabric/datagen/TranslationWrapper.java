package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.creative_tab.content.ItemGroupContentHelper;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistration;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@SuppressWarnings("UnusedReturnValue")
@Getter
@Slf4j
public class TranslationWrapper implements ITranslationWrapper {
    public static final Map<BalmEntityTypeRegistration<?>, DeferredItem> MAPPER = RDEntityTypes.SPAWN_EGG_BIND;
    private final HolderLookup.Provider wrapperLookup;
    private final FabricLanguageProvider.TranslationBuilder translationBuilder;

    public TranslationWrapper(HolderLookup.Provider wrapperLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        this.wrapperLookup = wrapperLookup;
        this.translationBuilder = translationBuilder;
    }

    public TranslationWrapper add(String translationKey, String value) {
        this.translationBuilder.add(translationKey, value);
        return this;
    }

    public TranslationWrapper add(Item item, String value) {
        this.translationBuilder.add(item, value);
        return this;
    }

    public TranslationWrapper add(Block block, String value) {
        this.translationBuilder.add(block, value);
        return this;
    }

    public TranslationWrapper add(ResourceKey<CreativeModeTab> registryKey, String value) {
        Function<CreativeModeTab.Builder, CreativeModeTab.Builder> builderFunction = ItemGroupContentHelper.REGISTRIES.get(registryKey);
        CreativeModeTab.Builder builder = builderFunction.apply(FabricCreativeModeTab.builder());
        if (builder != null) {
            this.add(builder.build(), value);
            return this;
        }
        this.translationBuilder.add(registryKey, value);
        return this;
    }

    public TranslationWrapper add(CreativeModeTab itemGroup, String value) {
        Component text = itemGroup.getDisplayName();
        ComponentContents content = text.getContents();
        if (content instanceof TranslatableContents translatableTextContent) {
            this.translationBuilder.add(translatableTextContent.getKey(), value);
        } else {
            log.error("Can't get translatable text content in item group {}", itemGroup);
        }
        return this;
    }

    public TranslationWrapper add(EntityType<?> entityType, String value) {
        this.translationBuilder.add(entityType, value);
        return this;
    }

    public TranslationWrapper addEnchantment(ResourceKey<Enchantment> enchantment, String value) {
        this.translationBuilder.addEnchantment(enchantment, value);
        return this;
    }

    public TranslationWrapper add(Holder<Attribute> entityAttribute, String value) {
        this.translationBuilder.add(entityAttribute, value);
        return this;
    }

    public TranslationWrapper add(StatType<?> statType, String value) {
        this.translationBuilder.add(statType, value);
        return this;
    }

    public TranslationWrapper add(MobEffect statusEffect, String value) {
        this.translationBuilder.add(statusEffect, value);
        return this;
    }

    public TranslationWrapper add(Identifier identifier, String value) {
        this.translationBuilder.add(identifier, value);
        return this;
    }

    public TranslationWrapper add(TagKey<?> tagKey, String value) {
        this.translationBuilder.add(tagKey, value);
        return this;
    }

    public TranslationWrapper add(Path existingLanguageFile) throws IOException {
        this.translationBuilder.add(existingLanguageFile);
        return this;
    }

    public TranslationWrapper add(EntityType<?> entityType, String name, String spawnEggName) {
        this.add(entityType, name);
        AtomicReference<Item> item = new AtomicReference<>();
        AtomicBoolean lock = new AtomicBoolean(false);
        MAPPER.forEach((entityTypeRegistration, deferredItem) -> {
            if (lock.get()) {
                return;

            }
            if (Objects.equals(entityTypeRegistration.asHolder().value(), entityType)) {
                item.set(deferredItem.asItem());
            }
        });
        if (item.get() == null) {
            return this;
        }
        this.add(item.get(), spawnEggName);
        return this;
    }

    public TranslationWrapper add(Component mutableText, String value) {
        ComponentContents content = mutableText.getContents();
        if (content instanceof TranslatableContents translatableText) {
            String key = translatableText.getKey();
            this.add(key, value);
        } else {
            log.error("Can't parse Translatable Text Content {}", mutableText);
        }
        return this;
    }

    public TranslationWrapper addAdvancement(ResourceKey<Advancement> key, String name, String description) {
        String titleKey = key.identifier().toLanguageKey("title");
        String descriptionKey = key.identifier().toLanguageKey("description");
        this.add(titleKey, name);
        this.add(descriptionKey, description);
        return this;
    }

    public TranslationWrapper generateDanmakuType(DanmakuTrajectory trajectory, String value) {
        Identifier key = RegistryImpls.DANMAKU_TRAJECTORY.getKey(trajectory);
        if (key == null) {
            log.error("Can't find key of {}", trajectory);
            return this;
        }
        this.translationBuilder.add(key.toLanguageKey(), value);
        return this;
    }

    public TranslationWrapper generateJukeBox(ResourceKey<JukeboxSong> key, String value) {
        this.translationBuilder.add(this.getSoundEventSubtitle(key), value);
        this.translationBuilder.add(this.getJukeBoxSongDisc(key), value);
        return this;
    }

    public TranslationWrapper generateStatusEffect(Holder<MobEffect> registryEntry, String value) {
        this.translationBuilder.add(getStatusEffect(registryEntry), value);
        return this;
    }

    public TranslationWrapper generatePotion(
            Potion registryEntry,
            String potion,
            String splash,
            String lingering
    ) {
        this.translationBuilder.add(getPotion(registryEntry), potion);
        this.translationBuilder.add(getSplashPotion(registryEntry), splash);
        this.translationBuilder.add(getLingeringPotion(registryEntry), lingering);
        this.translationBuilder.add(getPotionArrow(registryEntry), potion);
        return this;
    }

    public TranslationWrapper generateSoundEventSubtitle(SoundEvent soundEvent, String value) {
        this.translationBuilder.add(getSoundEventSubtitle(soundEvent), value);
        return this;
    }

    public TranslationWrapper addRoleEntity(NPCRole role, String value, String spawnEggValue) {
        EntityType<?> entityType = role.getEntityType().value();
        Item egg = role.getEgg().asItem();
        String item_value = value + spawnEggValue;
        this.add(entityType, value);
        this.add(egg, item_value);
        return this;
    }

    public String getStatusEffect(Holder<MobEffect> registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.getRegisteredName();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb = sb.append("effect.");
        sb = sb.append(idAsString);
        return sb.toString();
    }

    public String getPotion(Potion registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.name();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb = sb.append("item.minecraft.potion.effect.");
        sb = sb.append(idAsString);
        return sb.toString();
    }

    public String getSplashPotion(Potion registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.name();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb.append("item.minecraft.splash_potion.effect.");
        sb.append(idAsString);
        return sb.toString();
    }

    public String getLingeringPotion(Potion registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.name();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb.append("item.minecraft.lingering_potion.effect.");
        sb.append(idAsString);
        return sb.toString();
    }

    public String getPotionArrow(Potion registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.name();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb.append("item.minecraft.tipped_arrow.effect.");
        sb.append(idAsString);
        return sb.toString();
    }

    public String getSoundEventSubtitle(SoundEvent soundEvent) {
        Identifier id = soundEvent.location();
        return id.toLanguageKey("sound");
    }

    public String getSoundEventSubtitle(ResourceKey<JukeboxSong> registryKey) {
        Identifier id = registryKey.identifier();
        return id.toLanguageKey("sound");
    }

    public String getJukeBoxSongDisc(ResourceKey<JukeboxSong> registryKey) {
        Identifier key = registryKey.identifier();
        String namespace = key.getNamespace();
        String path = key.getPath().replaceAll("/", ".");
        return key.toLanguageKey("jukebox_song").replaceAll("/",".");
    }

}
