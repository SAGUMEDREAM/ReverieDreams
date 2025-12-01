package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

@Setter
@Getter
public class NPCState implements CodecStep<NPCState>, OwnerBinding<NPCState>, BuiltinObject, Translatable {
    public static final ResourceLocation DEFAULT_ID = ReverieDreams.id("normal");
    public static final Codec<NPCState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("value", DEFAULT_ID.toString()).forGetter(NPCState::getType)
            ).apply(instance, NPCState::new)
    );

    private final String type;
    private RegistryHandler<NPCState> owner;

    private NPCState() {
        this.type = "null";
    }

    public NPCState(String type) {
        this.type = type;
    }

    @Override
    public String translateKey() {
        return "gui.npc.mode." + this.type;
    }

    public MutableComponent getTranslateText() {
        return Component.translatable(translateKey());
    }

    @Override
    public RegistryHandler<NPCState> getOwner() {
        return RegistryHandlers.NPC_STATE;
    }

    @Override
    public Codec<NPCState> getCodec() {
        return CODEC;
    }
}