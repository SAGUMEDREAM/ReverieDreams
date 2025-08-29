package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.registry.*;
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
