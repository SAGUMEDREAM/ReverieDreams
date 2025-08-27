package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.registry.RegistrableObject;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
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
    public StandaloneRegistry<NPCRoleInteractionEvent> getRegistryRef() {
        return RegistryManager.ROLE_INTERACTION_EVENT;
    }

    @Override
    public Codec<NPCRoleInteractionEvent> getCodec() {
        return CODEC;
    }
}
