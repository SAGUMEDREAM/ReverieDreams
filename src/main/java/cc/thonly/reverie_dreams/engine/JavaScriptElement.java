package cc.thonly.reverie_dreams.engine;

import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;

@Setter
@Getter
@Slf4j
public class JavaScriptElement implements CodecStep<JavaScriptElement>, OwnerBinding<JavaScriptElement>, BuiltinObject {
    public static final Codec<JavaScriptElement> CODEC = Codec.unit(JavaScriptElement::new);
    private ResourceLocation id;
    private final String src;
    private RegistryHandler<JavaScriptElement> owner;

    private JavaScriptElement() {
        this("");
    }

    public JavaScriptElement(String src) {
        this.src = src;
    }

    @Override
    public RegistryHandler<JavaScriptElement> getOwner() {
        return RegistryHandlers.JAVASCRIPT_ELEMENT;
    }

    @Override
    public Codec<JavaScriptElement> getCodec() {
        return CODEC;
    }
}
