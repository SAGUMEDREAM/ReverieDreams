package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

@Setter
@Getter
public class NPCRoleInteractionEvent implements CodecStep<NPCRoleInteractionEvent>, OwnerBinding<NPCRoleInteractionEvent>, BuiltinObject {
    public static final Codec<NPCRoleInteractionEvent> CODEC = Codec.unit(NPCRoleInteractionEvent::new);
    private Identifier id;
    private IntrinsicalRegister<NPCRoleInteractionEvent> owner;
    private final InteractionCallback callback;

    protected NPCRoleInteractionEvent() {
        this((world, player, itemStack, hand, entity) -> NPCInteractResult.PASS);
    }

    public NPCRoleInteractionEvent(InteractionCallback callback) {
        this.callback = callback;
    }

    public NPCInteractResult interact(ServerWorld world, ServerPlayerEntity player, ItemStack itemStack, Hand hand, NPCEntityImpl entity) {
        return this.callback.onInteract(world, player, itemStack, hand, entity);
    }

    @Override
    public Codec<NPCRoleInteractionEvent> getCodec() {
        return CODEC;
    }

    public interface InteractionCallback {
        NPCInteractResult onInteract(ServerWorld world, ServerPlayerEntity player, ItemStack stack, Hand hand, NPCEntityImpl entity);
    }
}
