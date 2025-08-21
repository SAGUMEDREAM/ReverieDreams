package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.registry.RegistrableObject;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;

@Setter
@Getter
public class NPCRoleInteractionEvent implements RegistrableObject<NPCRoleInteractionEvent> {
    public static final Codec<NPCRoleInteractionEvent> CODEC = Codec.unit(NPCRoleInteractionEvent::new);
    private Identifier id;

    @Override
    public Codec<NPCRoleInteractionEvent> getCodec() {
        return CODEC;
    }
}
