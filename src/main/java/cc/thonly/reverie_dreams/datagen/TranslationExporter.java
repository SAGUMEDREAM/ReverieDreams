package cc.thonly.reverie_dreams.datagen;

import autovalue.shaded.com.google.errorprone.annotations.CanIgnoreReturnValue;
import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.npc.AbstractNPCEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.block.Block;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.potion.Potion;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@CanIgnoreReturnValue
@Getter
@Slf4j
public class TranslationExporter implements TranslationExporterBuilderImpl {
    public static final Map<EntityType<?>, Item> MAPPER = ModEntities.SPAWN_EGG_BIND;
    private final RegistryWrapper.WrapperLookup wrapperLookup;
    private final FabricLanguageProvider.TranslationBuilder translationBuilder;

    public TranslationExporter(RegistryWrapper.WrapperLookup wrapperLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        this.wrapperLookup = wrapperLookup;
        this.translationBuilder = translationBuilder;
    }

    public TranslationExporter add(String translationKey, String value) {
        this.translationBuilder.add(translationKey, value);
        return this;
    }

    public TranslationExporter add(Item item, String value) {
        this.translationBuilder.add(item, value);
        return this;
    }

    public TranslationExporter add(Block block, String value) {
        this.translationBuilder.add(block, value);
        return this;
    }

    public TranslationExporter add(RegistryKey<ItemGroup> registryKey, String value) {
        this.translationBuilder.add(registryKey, value);
        return this;
    }

    public TranslationExporter add(ItemGroup itemGroup, String value) {
        Text text = itemGroup.getDisplayName();
        TextContent content = text.getContent();
        if (content instanceof TranslatableTextContent translatableTextContent) {
            this.translationBuilder.add(translatableTextContent.getKey(), value);
        } else {
            TextContent.Type<?> type = content.getType();
            String string = type.asString();
            log.error("Can't get translatable text content in item group {}", string);
        }
        return this;
    }

    public TranslationExporter add(EntityType<?> entityType, String value) {
        this.translationBuilder.add(entityType, value);
        return this;
    }

    public TranslationExporter addEnchantment(RegistryKey<Enchantment> enchantment, String value) {
        this.translationBuilder.addEnchantment(enchantment, value);
        return this;
    }

    public TranslationExporter add(RegistryEntry<EntityAttribute> entityAttribute, String value) {
        this.translationBuilder.add(entityAttribute, value);
        return this;
    }

    public TranslationExporter add(StatType<?> statType, String value) {
        this.translationBuilder.add(statType, value);
        return this;
    }

    public TranslationExporter add(StatusEffect statusEffect, String value) {
        this.translationBuilder.add(statusEffect, value);
        return this;
    }

    public TranslationExporter add(Identifier identifier, String value) {
        this.translationBuilder.add(identifier, value);
        return this;
    }

    public TranslationExporter add(TagKey<?> tagKey, String value) {
        this.translationBuilder.add(tagKey, value);
        return this;
    }

    public TranslationExporter add(Path existingLanguageFile) throws IOException {
        this.translationBuilder.add(existingLanguageFile);
        return this;
    }

    public TranslationExporter add(EntityType<?> entityType, String name, String spawnEggName) {
        this.add(entityType, name);
        Item item = MAPPER.get(entityType);
        if (item != null) {
            this.add(item, spawnEggName);
        }
        return this;
    }

    public TranslationExporter add(Text mutableText, String value) {
        TextContent content = mutableText.getContent();
        if (content instanceof TranslatableTextContent translatableText) {
            String key = translatableText.getKey();
            this.add(key, value);
        } else {
            log.error("Can't parse Translatable Text Content {}", mutableText);
        }
        return this;
    }

    public TranslationExporter generateDanmakuType(DanmakuTrajectory trajectory, String value) {
        this.translationBuilder.add(RegistryManager.DANMAKU_TRAJECTORY.getId(trajectory).toTranslationKey(), value);
        return this;
    }

    public TranslationExporter generateJukeBox(RegistryKey<JukeboxSong> key, String value) {
        this.translationBuilder.add(this.getSoundEventSubtitle(key), value);
        this.translationBuilder.add(this.getJukeBoxSongDisc(key), value);
        return this;
    }

    public TranslationExporter generateStatusEffect(RegistryEntry<StatusEffect> registryEntry, String value) {
        this.translationBuilder.add(getStatusEffect(registryEntry), value);
        return this;
    }

    public TranslationExporter generatePotion(
            Potion registryEntry,
            String potion,
            String splash,
            String lingering
    ) {
        this.translationBuilder.add(getPotion(registryEntry), potion);
        this.translationBuilder.add(getSplashPotion(registryEntry), splash);
        this.translationBuilder.add(getLingeringPotion(registryEntry), lingering);
        return this;
    }

    public TranslationExporter generateSoundEventSubtitle(SoundEvent soundEvent, String value) {
        this.translationBuilder.add(getSoundEventSubtitle(soundEvent), value);
        return this;
    }

    public TranslationExporter addRoleEntity(NPCRole role, String value, String spawnEggValue) {
        EntityType<AbstractNPCEntity> entityType = role.getEntityType();
        Item egg = role.getEgg();
        String item_value = value + spawnEggValue;
        this.add(entityType, value);
        this.add(egg, item_value);
        return this;
    }

    public String getStatusEffect(RegistryEntry<StatusEffect> registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.getIdAsString();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb = sb.append("effect.");
        sb = sb.append(idAsString);
        return sb.toString();
    }

    public String getPotion(Potion registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.getBaseName();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb = sb.append("item.minecraft.potion.effect.");
        sb = sb.append(idAsString);
        return sb.toString();
    }

    public String getSplashPotion(Potion registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.getBaseName();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb = sb.append("item.minecraft.splash_potion.effect.");
        sb = sb.append(idAsString);
        return sb.toString();
    }

    public String getLingeringPotion(Potion registryEntry) {
        StringBuilder sb = new StringBuilder();
        String idAsString = registryEntry.getBaseName();
        idAsString = idAsString.replaceAll(":", ".");
        idAsString = idAsString.replaceAll("/", ".");
        sb = sb.append("item.minecraft.lingering_potion.effect.");
        sb = sb.append(idAsString);
        return sb.toString();
    }

    public String getSoundEventSubtitle(SoundEvent soundEvent) {
        Identifier id = soundEvent.id();
        return id.toTranslationKey("sound");
    }

    public String getSoundEventSubtitle(RegistryKey<JukeboxSong> registryKey) {
        Identifier id = registryKey.getValue();
        return id.toTranslationKey("sound");
    }

    public String getJukeBoxSongDisc(RegistryKey<JukeboxSong> registryKey) {
        Identifier key = registryKey.getValue();
        String namespace = key.getNamespace();
        String path = key.getPath().replaceAll("/", ".");
        return key.toTranslationKey("jukebox_song");
    }

}
