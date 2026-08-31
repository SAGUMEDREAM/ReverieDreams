package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.BiConsumer;

@Setter
@Getter
public class NPCState implements SerializableProvider<NPCState>, RegistryEntryOwnerBindable<NPCState>, BuiltinObject, RegistryEntryTranslatable {
    public static final Identifier DEFAULT_ID = ReverieDreams.id("normal");
    public static final Codec<NPCState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("value", DEFAULT_ID.toString()).forGetter(NPCState::getType)
            ).apply(instance, NPCState::new)
    );

    private final String type;
    private RegistryProvider<NPCState> owner;
    private BiConsumer<BaseNPCLikeEntity, NPCState> onStarted = (npc, state) -> {

    };
    private BiConsumer<BaseNPCLikeEntity, NPCState> onStop = (npc, state) -> {

    };

    private NPCState() {
        this.type = "null";
    }

    public NPCState(String type) {
        this.type = type;
    }

    public NPCState withStarted(BiConsumer<BaseNPCLikeEntity, NPCState>function) {
        this.onStarted = function;
        return this;
    }

    public NPCState withStop(BiConsumer<BaseNPCLikeEntity, NPCState> function) {
        this.onStop = function;
        return this;
    }

    public void onStarted(BaseNPCLikeEntity npc) {
        this.onStarted.accept(npc, this);
    }

    public void onStop(BaseNPCLikeEntity npc) {
        this.onStop.accept(npc, this);
    }

    @Override
    public String translateKey() {
        return "gui.npc.mode." + this.type;
    }

    public MutableComponent getTranslateText() {
        return Component.translatable(translateKey());
    }

    @Override
    public RegistryProvider<NPCState> getOwner() {
        return BuiltInRegistryProviders.NPC_STATE;
    }

    @Override
    public Codec<NPCState> getCodec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof NPCState state && Objects.equals(this.type, state.type));
    }
}