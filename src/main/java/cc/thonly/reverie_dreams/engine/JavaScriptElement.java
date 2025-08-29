package cc.thonly.reverie_dreams.engine;

import cc.thonly.reverie_dreams.entity.npc.NPCRoleInteractionEvent;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Identifier;

@Setter
@Getter
@Slf4j
public class JavaScriptElement implements CodecStep<JavaScriptElement>, OwnerBinding<JavaScriptElement>, BuiltinObject {
    public static final Codec<JavaScriptElement> CODEC = Codec.unit(JavaScriptElement::new);
    private Identifier id;
    private final String src;
    private IntrinsicalRegister<JavaScriptElement> owner;

    private JavaScriptElement() {
        this("");
    }

    public JavaScriptElement(String src) {
        this.src = src;
    }

    @Override
    public IntrinsicalRegister<JavaScriptElement> getOwner() {
        return RegistryManager.JAVASCRIPT_ELEMENT;
    }

    @Override
    public Codec<JavaScriptElement> getCodec() {
        return CODEC;
    }
}
