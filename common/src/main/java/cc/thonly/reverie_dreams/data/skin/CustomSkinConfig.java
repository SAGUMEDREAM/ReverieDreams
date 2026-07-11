package cc.thonly.reverie_dreams.data.skin;

import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
@Slf4j
public class CustomSkinConfig implements CodecStep<CustomSkinConfig> {
    public static final Codec<CustomSkinConfig> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(x -> x.group(
            Identifier.CODEC.fieldOf("id").forGetter(CustomSkinConfig::getId),
            SkinConfig.ModelType.CODEC.optionalFieldOf("model_type", SkinConfig.ModelType.WIDE).forGetter(CustomSkinConfig::getType),
            Identifier.CODEC.optionalFieldOf("cape_texture").forGetter(CustomSkinConfig::getCapeTexture),
            Identifier.CODEC.optionalFieldOf("elytra_texture").forGetter(CustomSkinConfig::getElytraTexture),
            CodecStep.ITEM_CODEC.optionalFieldOf("icon", Items.VILLAGER_SPAWN_EGG).forGetter(CustomSkinConfig::getIcon)
    ).apply(x, CustomSkinConfig::new)));
    @Getter
    private final Identifier id;
    @Getter
    private final SkinConfig.ModelType type;
    @Getter
    private final Optional<Identifier> capeTexture;
    @Getter
    private final Optional<Identifier> elytraTexture;
    @Getter
    private Item icon = Items.VILLAGER_SPAWN_EGG;
    @Getter
    private SkinType value;

    public CustomSkinConfig(Identifier id, SkinConfig.ModelType type, Optional<Identifier> capeTexture, Optional<Identifier> elytraTexture) {
        this.id = id;
        this.type = type;
        this.capeTexture = capeTexture;
        this.elytraTexture = elytraTexture;
    }

    public CustomSkinConfig(Identifier id, SkinConfig.ModelType type, Optional<Identifier> capeTexture, Optional<Identifier> elytraTexture, Item icon) {
        this.id = id;
        this.type = type;
        this.capeTexture = capeTexture;
        this.elytraTexture = elytraTexture;
        this.icon = icon;
    }

    public SkinType value() {
        if (this.value != null) {
            return this.value;
        }
        Optional<Holder.Reference<SkinType>> reference = RegistryImpls.SKIN_TYPE.get(this.id);
        if (reference.isPresent() && (reference.get().value() instanceof CustomSkinConfig.CustomType)) {
            log.error("Duplicate key {}", this.id);
            return reference.get().value();
        }
        SkinType skinType = new CustomType(this.id, this.icon);
        SkinConfig skinConfig = new SkinConfig(this.type, this.capeTexture, this.elytraTexture);
        skinType.bindConfig(skinConfig);
        skinConfig.bindRegistryKey(this.id);
        this.value = skinType;
        return skinType;
    }

    @Override
    public Codec<CustomSkinConfig> getCodec() {
        return CODEC;
    }

    public Identifier getTexture() {
        return Identifier.fromNamespaceAndPath(this.id.getNamespace(), "entity/player/skin/%s".formatted(this.id.getPath()));
    }

    @Getter
    public static class CustomType extends SkinType {
        final Item icon;
        public CustomType(Identifier id, Item icon) {
            super(id);
            this.icon = icon;
        }

        public String getDescriptionId() {
            return Util.makeDescriptionId("entity", this.getId());
        }
    }

}
