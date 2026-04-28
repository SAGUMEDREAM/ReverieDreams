package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.interfaces.SimpleVariant;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;

@Setter
@Getter
public class RabbitUnitVariant implements CodecStep<RabbitUnitVariant>, OwnerBinding<RabbitUnitVariant>, BuiltinObject, SimpleVariant, Translatable {
    public static Codec<RabbitUnitVariant> CODEC = UnitCodec.unit(RabbitUnitVariant::new);
    private static int NEXT = 0;
    private Identifier id;
    private int number;
    private SkinType skinType;
    private RegistryHandler<RabbitUnitVariant> owner;

    private RabbitUnitVariant() {
    }

    public RabbitUnitVariant(Identifier id, SkinType skinType) {
        this.id = id;
        this.number = NEXT++;
        this.skinType = skinType;
    }


    @Override
    public Codec<RabbitUnitVariant> getCodec() {
        return CODEC;
    }
}
