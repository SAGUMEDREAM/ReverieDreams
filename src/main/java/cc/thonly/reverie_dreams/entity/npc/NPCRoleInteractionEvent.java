package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;

@Setter
@Getter
public class NPCRoleInteractionEvent implements CodecStep<NPCRoleInteractionEvent>, OwnerBinding<NPCRoleInteractionEvent>, BuiltinObject {
    public static final Codec<NPCRoleInteractionEvent> CODEC = Codec.unit(NPCRoleInteractionEvent::new);
    private Identifier id;
    private IntrinsicalRegister<NPCRoleInteractionEvent> owner;

    @Override
    public Codec<NPCRoleInteractionEvent> getCodec() {
        return CODEC;
    }

}
