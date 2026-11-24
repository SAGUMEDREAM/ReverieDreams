package cc.thonly.reverie_dreams.engine;

import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.api.registry.SerializableProvider;
import cc.thonly.reverie_dreams.api.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@Setter
@Getter
@Slf4j
@ApiStatus.Experimental
public class JavaScriptElement implements SerializableProvider<JavaScriptElement>, RegistryEntryOwnerBindable<JavaScriptElement>, BuiltinObject {
    public static final Codec<JavaScriptElement> CODEC = UnitCodec.unit(JavaScriptElement::new);
    private Identifier id;
    private final String src;
    private RegistryProviderLookup<JavaScriptElement> owner;

    private JavaScriptElement() {
        this("");
    }

    public JavaScriptElement(String src) {
        this.src = src;
    }

    @Override
    public RegistryProviderLookup<JavaScriptElement> getOwner() {
        return BuiltInRegistryProviders.JAVASCRIPT_ELEMENT;
    }

    @Override
    public Codec<JavaScriptElement> getCodec() {
        return CODEC;
    }
}
