package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.entity.PolymerVariant;
import cc.thonly.reverie_dreams.registry.RegistrableObject;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;

@Setter
@Getter
public class YouseiVariant implements RegistrableObject<YouseiVariant>, PolymerVariant {
    public static Codec<YouseiVariant> CODEC = Codec.unit(YouseiVariant::new);
    private static int NEXT = 0;
    private Identifier id;
    private int number;
    private Property property;

    private YouseiVariant() {
    }

    public YouseiVariant(Identifier id, Property property) {
        this.id = id;
        this.number = NEXT++;
        this.property = property;
    }

    @Override
    public StandaloneRegistry<YouseiVariant> getRegistryRef() {
        return RegistryManager.YOUSEI_VARIANT;
    }

    @Override
    public Codec<YouseiVariant> getCodec() {
        return CODEC;
    }

}
