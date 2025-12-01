package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.interfaces.SimpleVariant;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

@Setter
@Getter
public class YouseiVariant implements CodecStep<YouseiVariant>, OwnerBinding<YouseiVariant>, BuiltinObject, SimpleVariant, Translatable {
    public static Codec<YouseiVariant> CODEC = Codec.unit(YouseiVariant::new);
    private static int NEXT = 0;
    private ResourceLocation id;
    private int number;
    private SkinType skinType;
    private RegistryHandler<YouseiVariant> owner;

    private YouseiVariant() {
    }

    public YouseiVariant(ResourceLocation id, SkinType skinType) {
        this.id = id;
        this.number = NEXT++;
        this.skinType = skinType;
    }


    @Override
    public Codec<YouseiVariant> getCodec() {
        return CODEC;
    }


}
