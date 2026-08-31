package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCInteractResult;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.SerializableProvider;
import cc.thonly.reverie_dreams.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
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
public class NPCLikeInteractionEvent implements SerializableProvider<NPCLikeInteractionEvent>, RegistryEntryOwnerBindable<NPCLikeInteractionEvent>, BuiltinObject {
    public static final Codec<NPCLikeInteractionEvent> CODEC = UnitCodec.unit(NPCLikeInteractionEvent::new);
    private Identifier id;
    private RegistryProvider<NPCLikeInteractionEvent> owner;
    private final InteractionCallback callback;

    protected NPCLikeInteractionEvent() {
        this((world, player, itemStack, hand, entity) -> NPCInteractResult.PASS);
    }

    public NPCLikeInteractionEvent(InteractionCallback callback) {
        this.callback = callback;
    }

    public NPCInteractResult interact(ServerLevel world, ServerPlayer player, ItemStack itemStack, InteractionHand hand, BaseNPCLikeEntity entity) {
        return this.callback.onInteract(world, player, itemStack, hand, entity);
    }

    @Override
    public Codec<NPCLikeInteractionEvent> getCodec() {
        return CODEC;
    }

    public interface InteractionCallback {
        NPCInteractResult onInteract(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity);
    }
}
