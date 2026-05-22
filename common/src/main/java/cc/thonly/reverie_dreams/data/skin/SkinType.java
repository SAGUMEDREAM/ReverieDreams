package cc.thonly.reverie_dreams.data.skin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.UnitCodec;
import cc.thonly.reverie_dreams.util.skin.SkinFetcher;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Optional;


@Slf4j
public class SkinType implements CodecStep<SkinType>, RegistryEntryOwnerBindable<SkinType>, BuiltinObject, RegistryEntryTranslatable {
    public static Codec<SkinType> UNIT_CODEC = UnitCodec.unit(SkinType::new);
    public static Codec<SkinType> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(x -> x.group(
            Identifier.CODEC.fieldOf("SkinType").forGetter(SkinType::getId)
    ).apply(x, RegistryImpls.SKIN_TYPE::getValue)));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinType> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    public static final EntityDataSerializer<SkinType> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);

    @Setter
    @Getter
    private Identifier id;
    private String value;
    private String signature;
    @Setter
    private Property property;
    @Setter
    @Getter
    private SkinConfig config;
    @Getter
    @Setter
    private RegistryImpl<SkinType> owner;
    @Setter
    @Getter
    private boolean slim;

    private SkinType() {
    }

    public SkinType(Identifier id) {
        this.id = id;
        this.value = "null";
        this.signature = "null";
        this.slim = true;
    }

    public SkinType(Identifier id, String value, String signature) {
        this.id = id;
        this.value = value;
        this.signature = signature;
        this.slim = true;
        this.valid();
    }

    @Override
    public String translateKey() {
        Property property = this.getProperty();
        return property.name() + "|" + this.id;
    }

    public Identifier getTexture() {
        return Identifier.fromNamespaceAndPath(this.id.getNamespace(), "entity/player/skin/%s".formatted(this.id.getPath()));
    }

    public Property getProperty() {
        if (this.config == null) {
            log.warn("Unable to get skin properties until data pack is loaded");
            return MobSkinTypes.DEFAULT.getProperty();
        }
        if (this.property == null) {
            Optional<Property> skinFromNPCSkin = SkinFetcher.getSkinFromNPCSkin(this.config);
            if (skinFromNPCSkin.isPresent()) {
                log.debug("Fetching {} skin from networking", this.id);
                this.setProperty(skinFromNPCSkin.get());
            } else {
                this.setProperty(texture(this.value, this.signature));
            }
        }
        return this.property;
    }

    public void bindConfig(SkinConfig config) {
        this.config = config;
    }

    public void unbind() {
        this.config = null;
        this.property = null;
    }

    private void valid() {
        try {
            Identifier fileId = ReverieDreams.id("entity/player/%s".formatted(this.id.getPath()));

        } catch (Exception err) {
            log.error("Can't parse role code", err);
        }
    }

    private static Property texture(String value, String signature) {
        return new Property("textures", value, signature);
    }

    @Override
    public Codec<SkinType> getCodec() {
        return CODEC;
    }

    public static void onReload(ResourceManager manager) {
        for (SkinType skinType : RegistryImpls.SKIN_TYPE.values()) {
            skinType.unbind();
        }
    }
}
