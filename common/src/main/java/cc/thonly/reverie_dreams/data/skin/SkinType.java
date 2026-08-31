package cc.thonly.reverie_dreams.data.skin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.SerializableProvider;
import cc.thonly.reverie_dreams.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.UnitCodec;
import cc.thonly.reverie_dreams.util.skin.SkinFetcher;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Objects;
import java.util.Optional;


@Slf4j
public class SkinType implements SerializableProvider<SkinType>, RegistryEntryOwnerBindable<SkinType>, RegistryEntryTranslatable {
    public static final RecordCodecBuilder<SkinType, Identifier> PART = Identifier.CODEC.fieldOf("SkinType").forGetter(SkinType::getId);
    public static final Codec<SkinType> UNIT_CODEC = UnitCodec.unit(SkinType::new);
    public static final Codec<SkinType> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(x -> x.group(PART).apply(x, BuiltInRegistryProviders.SKIN_TYPE::getValue)));
    public static final Codec<SkinType> MERGED_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(x -> x.group(PART).apply(x, id -> {
        SkinType builtinSkinType = BuiltInRegistryProviders.SKIN_TYPE.getValue(id);
        if (builtinSkinType != null) {
            return builtinSkinType;
        }
        CustomType customType = BuiltInRegistryProviders.CUSTOM_SKIN_TYPE.getValue(id);
        if (customType != null) {
            return customType;
        }
        return MobSkinTypes.DEFAULT;
    })));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkinType> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(MERGED_CODEC);
    public static final EntityDataSerializer<SkinType> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);
    public static final Identifier RECOVERY = ReverieDreams.id("recovery");

    @Setter
    @Getter
    protected Identifier id;
    protected String value;
    protected String signature;
    @Setter
    private Property property;
    @Setter
    @Getter
    private SkinConfig config;
    @Getter
    @Setter
    @ToString.Exclude
    private RegistryProvider<SkinType> owner;
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
            if (ReverieDreams.getServer() != null) {
                log.warn("Unable to get skin properties until data pack is loaded");
            }
            return new Property("ewogICJ0aW1lc3RhbXAiIDogMTcwODU4MzQ0MDY4OSwKICAicHJvZmlsZUlkIiA6ICJiM2E3NjExNGVmMzI0ZjYyYWM4NDRiOWJmNTY1NGFiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNcmd1eW1hbnBlcnNvbiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lODRjY2E2ZGVhNjhlN2UzYzZhNDJiYTQ2YTNhN2ZlODdlZDY3YTk3ZmRjOTRlNTY0NzI1YTc0ZTYxYjBmOTI3IgogICAgfQogIH0KfQ==",
                    "x/b2EYJFrRIkSl5TviHdKoZcwBmmDt0RfAa2y0oTK3n/YZ2xco3rl/D60NN5CSWK24Ui0VaGQ66SdrKV742aXjiNAKzOTxga3IEHSaCTN/to9jrvKHvz0esyAGiLFB98co9o4nZyGClTlzieW0dHexPmyfa6g8MTlS/T3kjACDIT8OQvSkl95U2iMnvvmqfLnZ9l7WlvEkD9+gjNg8dRUFwMGVGVRz2hCYHR6WxjOJEpqbYMMRMJgKBXRzKVCwxy7cDW+warLZL7BwV8pYZVv4FOc8epaJm3JsTbwp7eTMxb+o8rSupv1Aoq52iJwZfk4x+c0rRS/xCfc+d1bY1UfLG6s2NnwvUJpMJ3VdSMVcRZ5QecGX+OU8ZVdi+VWAZncZ4csrcZ1KqI30EeA6ztccTaarA6nmwghGzNUi+bXCUJctnzBXpjL7eErgrTeHso8JgmOpybMeu+UefDWIw1fbRWjRuX5l9/VRZp3zR4wfSu7NMuKANx/cmx0almu4ef0qeN9PZ39fjOoxwEQvaGbeq2pH0+2HCE9hHnWoH6RXFuCFTnnQkc8TsFNOWQVrY6alg1X6wWS2tzfuIzzW/EjM6Wl3qpNDd4VaVHWUNfq6xGp+F+kpymaKrCGvSxAQiLrJ/kK0SK5kAW16kKedkiSFji3dmdBlrYbTitNRFPXRg=");
        }
        if (!PlatformContext.hasPolymer()) {
            return new Property("builtin", "builtin", "builtin");
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

    public boolean is(SkinType skinType) {
        return Objects.equals(this.id, skinType.id);
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
    public String toString() {
        return "SkinType{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", signature='" + signature + '\'' +
                ", slim=" + slim +
                '}';
    }

    @Override
    public Codec<SkinType> getCodec() {
        return CODEC;
    }

    public static void onReload(ResourceManager manager) {
        for (SkinType skinType : BuiltInRegistryProviders.SKIN_TYPE.values()) {
            skinType.unbind();
        }
    }
}
