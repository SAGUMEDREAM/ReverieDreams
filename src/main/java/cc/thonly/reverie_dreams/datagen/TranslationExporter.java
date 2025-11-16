package cc.thonly.reverie_dreams.datagen;

import autovalue.shaded.com.google.errorprone.annotations.CanIgnoreReturnValue;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

@CanIgnoreReturnValue
@Getter
@Slf4j
public class TranslationExporter implements ITranslationExporterBuilder {
    public static final Map<EntityType<?>, Item> MAPPER = RDEntityTypes.SPAWN_EGG_BIND;
    private final HolderLookup.Provider wrapperLookup;
    private final FabricLanguageProvider.TranslationBuilder translationBuilder;

    public TranslationExporter(HolderLookup.Provider wrapperLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
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

    public TranslationExporter add(ResourceKey<CreativeModeTab> registryKey, String value) {
        this.translationBuilder.add(registryKey, value);
        return this;
    }

    public TranslationExporter add(CreativeModeTab itemGroup, String value) {
        Component text = itemGroup.getDisplayName();
        ComponentContents content = text.getContents();
        if (content instanceof TranslatableContents translatableTextContent) {
            this.translationBuilder.add(translatableTextContent.getKey(), value);
        } else {
            ComponentContents.Type<?> type = content.type();
            String string = type.getSerializedName();
            log.error("Can't get translatable text content in item group {}", string);
        }
        return this;
    }

    public TranslationExporter add(EntityType<?> entityType, String value) {
        this.translationBuilder.add(entityType, value);
        return this;
    }

    public TranslationExporter addEnchantment(ResourceKey<Enchantment> enchantment, String value) {
        this.translationBuilder.addEnchantment(enchantment, value);
        return this;
    }

    public TranslationExporter add(Holder<Attribute> entityAttribute, String value) {
        this.translationBuilder.add(entityAttribute, value);
        return this;
    }

    public TranslationExporter add(StatType<?> statType, String value) {
        this.translationBuilder.add(statType, value);
        return this;
    }

    public TranslationExporter add(MobEffect statusEffect, String value) {
        this.translationBuilder.add(statusEffect, value);
        return this;
    }

    public TranslationExporter add(ResourceLocation identifier, String value) {
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

    public TranslationExporter add(Component mutableText, String value) {
        ComponentContents content = mutableText.getContents();
        if (content instanceof TranslatableContents translatableText) {
            String key = translatableText.getKey();
            this.add(key, value);
        } else {
            log.error("Can't parse Translatable Text Content {}", mutableText);
        }
        return this;
    }

    public TranslationExporter generateDanmakuType(DanmakuTrajectory trajectory, String value) {
        this.translationBuilder.add(RegistryHandlers.DANMAKU_TRAJECTORY.getKey(trajectory).toLanguageKey(), value);
        return this;
    }

    public TranslationExporter generateJukeBox(ResourceKey<JukeboxSong> key, String value) {
        this.translationBuilder.add(this.getSoundEventSubtitle(key), value);
        this.translationBuilder.add(this.getJukeBoxSongDisc(key), value);
        return this;
    }

    public TranslationExporter generateStatusEffect(Holder<MobEffect> registryEntry, String value) {
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
        this.translationBuilder.add(getPotionArrow(registryEntry), potion);
        return this;
    }

    public TranslationExporter generateSoundEventSubtitle(SoundEvent soundEvent, String value) {
        this.translationBuilder.add(getSoundEventSubtitle(soundEvent), value);
        return this;
    }

    public TranslationExporter addRoleEntity(NPCRole role, String value, String spawnEggValue) {
        EntityType<?> entityType = role.getEntityType();
        Item egg = role.getEgg();
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
        ResourceLocation id = soundEvent.location();
        return id.toLanguageKey("sound");
    }

    public String getSoundEventSubtitle(ResourceKey<JukeboxSong> registryKey) {
        ResourceLocation id = registryKey.location();
        return id.toLanguageKey("sound");
    }

    public String getJukeBoxSongDisc(ResourceKey<JukeboxSong> registryKey) {
        ResourceLocation key = registryKey.location();
        String namespace = key.getNamespace();
        String path = key.getPath().replaceAll("/", ".");
        return key.toLanguageKey("jukebox_song");
    }

}
