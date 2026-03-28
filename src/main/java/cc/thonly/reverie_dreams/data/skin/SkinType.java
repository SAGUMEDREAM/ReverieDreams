package cc.thonly.reverie_dreams.data.skin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import cc.thonly.reverie_dreams.util.UnitCodec;
import cc.thonly.reverie_dreams.util.skin.SkinFetcher;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;
import java.util.Optional;


@Slf4j
public class SkinType implements CodecStep<SkinType>, OwnerBinding<SkinType>, BuiltinObject, Translatable {
    public static Codec<SkinType> UNIT_CODEC = UnitCodec.unit(SkinType::new);
    public static Codec<SkinType> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(x -> x.group(
            Identifier.CODEC.fieldOf("SkinType").forGetter(SkinType::getId)
    ).apply(x, RegistryHandlers.SKIN_TYPE::getValue)));

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
    private RegistryHandler<SkinType> owner;

    private SkinType() {
    }

    public SkinType(Identifier id) {
        this.id = id;
        this.value = "null";
        this.signature = "null";
    }

    public SkinType(Identifier id, String value, String signature) {
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
        if (this.config == null) {
            log.warn("Unable to get skin properties until data pack is loaded");
            return MobSkinTypes.DEFAULT.get();
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
        for (SkinType skinType : RegistryHandlers.SKIN_TYPE.values()) {
            skinType.unbind();
        }
    }
}
