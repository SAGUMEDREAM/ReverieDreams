package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.entity.SimpleVariant;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

@Setter
@Getter
public class YouseiVariant implements CodecStep<YouseiVariant>, OwnerBinding<YouseiVariant>, BuiltinObject, SimpleVariant, Translatable {
    public static Codec<YouseiVariant> CODEC = Codec.unit(YouseiVariant::new);
    private static int NEXT = 0;
    private Identifier id;
    private int number;
    private Supplier<Property> propertySupplier;
    private IntrinsicalRegister<YouseiVariant> owner;

    private YouseiVariant() {
    }

    public YouseiVariant(Identifier id, Supplier<Property> propertySupplier) {
        this.id = id;
        this.number = NEXT++;
        this.propertySupplier = propertySupplier;
    }


    @Override
    public Codec<YouseiVariant> getCodec() {
        return CODEC;
    }

    @Override
    public void setPropertySupplier(Supplier<Property> propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    @Override
    public void setProperty(Property property) {
        this.propertySupplier = () -> property;
    }
}
