package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCInteractResult;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Setter
@Getter
public class NPCRoleInteractionEvent implements CodecStep<NPCRoleInteractionEvent>, OwnerBinding<NPCRoleInteractionEvent>, BuiltinObject {
    public static final Codec<NPCRoleInteractionEvent> CODEC = UnitCodec.unit(NPCRoleInteractionEvent::new);
    private Identifier id;
    private RegistryImpl<NPCRoleInteractionEvent> owner;
    private final InteractionCallback callback;

    protected NPCRoleInteractionEvent() {
        this((world, player, itemStack, hand, entity) -> NPCInteractResult.PASS);
    }

    public NPCRoleInteractionEvent(InteractionCallback callback) {
        this.callback = callback;
    }

    public NPCInteractResult interact(ServerLevel world, ServerPlayer player, ItemStack itemStack, InteractionHand hand, BaseNPCLikeEntity entity) {
        return this.callback.onInteract(world, player, itemStack, hand, entity);
    }

    @Override
    public Codec<NPCRoleInteractionEvent> getCodec() {
        return CODEC;
    }

    public interface InteractionCallback {
        NPCInteractResult onInteract(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity);
    }
}
