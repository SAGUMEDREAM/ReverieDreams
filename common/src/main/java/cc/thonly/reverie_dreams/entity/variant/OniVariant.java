package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.interfaces.SimpleVariant;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;

@Setter
@Getter
public class OniVariant implements CodecStep<OniVariant>, RegistryEntryOwnerBindable<OniVariant>, BuiltinObject, SimpleVariant, RegistryEntryTranslatable {
    public static Codec<OniVariant> CODEC = UnitCodec.unit(OniVariant::new);
    private static int NEXT = 0;
    private Identifier id;
    private int number;
    private SkinType skinType;
    private RegistryImpl<OniVariant> owner;

    private OniVariant() {
    }

    public OniVariant(Identifier id, SkinType skinType) {
        this.id = id;
        this.number = NEXT++;
        this.skinType = skinType;
    }

    @Override
    public Codec<OniVariant> getCodec() {
        return CODEC;
    }

}
