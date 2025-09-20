package cc.thonly.reverie_dreams.entity.skin;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.*;
import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;


@Slf4j
public class NPCSkin implements CodecStep<NPCSkin>, OwnerBinding<NPCSkin>, BuiltinObject, Translatable {
    /**
     * 预升级 1.21.9 所用代码
     **/
    private static final Map<Identifier, MannequinInfo> INFO = new Object2ObjectLinkedOpenHashMap<>();

    private record MannequinInfo(Identifier texture, Optional<Identifier> capeTexture,
                                 Optional<Identifier> elytraTexture, PlayerSkinType model) {

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
    private static final Gson GSON = new Gson();

    public static final Codec<NPCSkin> UNIT_CODEC = Codec.unit(NPCSkin::new);
    public static final Codec<NPCSkin> CODEC = null;
    @Setter
    @Getter
    private Identifier id;
    private String value;
    private String signature;
    private Property instance;
    @Setter
    @Getter
    private NPCSkinConfig config;
    @Getter
    @Setter
    private IntrinsicalRegister<NPCSkin> owner;

    private NPCSkin() {
    }

    public NPCSkin(Identifier id, String value, String signature) {
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
        if (this.instance == null) {
            this.instance = texture(this.value, this.signature);
        }
        return this.instance;
    }

    private void valid() {
        Property property = this.get();
        try {
            Identifier fileId = Touhou.id("entity/player/%s".formatted(this.id.getPath()));

        } catch (Exception err) {
            log.error("Can't parse role code", err);
        }
    }

    private static Property texture(String value, String signature) {
        return new Property("textures", value, signature);
    }

    @Override
    public Codec<NPCSkin> getCodec() {
        return UNIT_CODEC;
    }
}
