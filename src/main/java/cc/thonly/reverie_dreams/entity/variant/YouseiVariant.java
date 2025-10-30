package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.entity.SimpleVariant;
import cc.thonly.reverie_dreams.entity.skin.SkinType;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

@Setter
@Getter
public class YouseiVariant implements CodecStep<YouseiVariant>, OwnerBinding<YouseiVariant>, BuiltinObject, SimpleVariant, Translatable {
    public static Codec<YouseiVariant> CODEC = Codec.unit(YouseiVariant::new);
    private static int NEXT = 0;
    private ResourceLocation id;
    private int number;
    private SkinType skinType;
    private IntrinsicalRegister<YouseiVariant> owner;

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
