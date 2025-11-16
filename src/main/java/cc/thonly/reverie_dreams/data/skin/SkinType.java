package cc.thonly.reverie_dreams.data.skin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import cc.thonly.reverie_dreams.util.skin.SkinFetcher;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import java.util.Map;
import java.util.Optional;


@Slf4j
public class SkinType implements CodecStep<SkinType>, OwnerBinding<SkinType>, BuiltinObject, Translatable {
    /**
     * 预升级 1.21.9 所用代码
     **/
    private static final Map<ResourceLocation, MannequinInfo> INFO = new Object2ObjectLinkedOpenHashMap<>();

    private record MannequinInfo(ResourceLocation texture, Optional<ResourceLocation> capeTexture,
                                 Optional<ResourceLocation> elytraTexture, PlayerSkinType model) {

    }

    enum PlayerSkinType {
        SLIM("slim", "slim"),
        WIDE("wide", "default");
        private final String name;
        private final String modelMetadata;

        private PlayerSkinType(final String name, final String modelMetadata) {
            this.name = name;
            this.modelMetadata = modelMetadata;
        }
    }

    public static Codec<SkinType> UNIT_CODEC = Codec.unit(SkinType::new);
    public static Codec<SkinType> CODEC;

    @Setter
    @Getter
    private ResourceLocation id;
    private String value;
    private String signature;
    @Setter
    private Property property;
    @Setter
    @Getter
    private SkinConfig config;
    @Getter
    @Setter
    private RegistryHandler<SkinType> owner;

    private SkinType() {
    }

    public SkinType(ResourceLocation id) {
        this.id = id;
        this.value = "null";
        this.signature = "null";
    }

    public SkinType(ResourceLocation id, String value, String signature) {
        this.id = id;
        this.value = value;
        this.signature = signature;
        this.valid();
    }

    @Override
    public String translateKey() {
        Property property = this.get();
        return property.name() + "|" + this.id;
    }

    public Property get() {
//        Thread.dumpStack();
        if (this.config == null) {
            log.warn("Unable to get skin properties until data pack is loaded");
            return MobSkinTypes.DEFAULT.get();
        }
        if (this.property == null) {
            Optional<Property> skinFromNPCSkin = SkinFetcher.getSkinFromNPCSkin(this.config);
            if (skinFromNPCSkin.isPresent()) {
                log.debug("Fetching {} skin from networking", this.id);
                this.setProperty(skinFromNPCSkin.get());
//                System.out.println(skinFromNPCSkin.get());
//                System.out.println("by network");
            } else {
                this.setProperty(texture(this.value, this.signature));
//                System.out.println("by jar");
            }
        }
        return this.property;
    }

    private void valid() {
        try {
            ResourceLocation fileId = ReverieDreams.id("entity/player/%s".formatted(this.id.getPath()));

        } catch (Exception err) {
            log.error("Can't parse role code", err);
        }
    }

    private static Property texture(String value, String signature) {
        return new Property("textures", value, signature);
    }

    @Override
    public Codec<SkinType> getCodec() {
        if (CODEC == null) {
            CODEC = RecordCodecBuilder.create(x->x.group(
                    ResourceLocation.CODEC.fieldOf("SkinType").forGetter(SkinType::getId)
            ).apply(x, RegistryHandlers.SKIN_TYPE::getValue));
        }
        return CODEC;
    }

    public static void onReload(ResourceManager manager) {
        for (SkinType skinType : RegistryHandlers.SKIN_TYPE.values()) {
            skinType.setProperty(null);
        }
    }
}
