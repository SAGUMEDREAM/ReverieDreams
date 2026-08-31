package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.api.entity.type.SimpleVariant;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.SerializableProvider;
import cc.thonly.reverie_dreams.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;

@Setter
@Getter
public class YouseiVariant implements SerializableProvider<YouseiVariant>, RegistryEntryOwnerBindable<YouseiVariant>, BuiltinObject, SimpleVariant, RegistryEntryTranslatable {
    public static Codec<YouseiVariant> CODEC = UnitCodec.unit(YouseiVariant::new);
    private static int NEXT = 0;
    private Identifier id;
    private int number;
    private SkinType skinType;
    private RegistryProvider<YouseiVariant> owner;

    private YouseiVariant() {
    }

    public YouseiVariant(Identifier id, SkinType skinType) {
        this.id = id;
        this.number = NEXT++;
        this.skinType = skinType;
    }


    @Override
    public Codec<YouseiVariant> getCodec() {
        return CODEC;
    }


}
