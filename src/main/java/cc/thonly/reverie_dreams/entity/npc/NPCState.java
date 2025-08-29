package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Setter
@Getter
public class NPCState implements CodecStep<NPCState>, OwnerBinding<NPCState>, BuiltinObject, Translatable {
    public static final Identifier DEFAULT_ID = Touhou.id("normal");
    public static final Codec<NPCState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("value", DEFAULT_ID.toString()).forGetter(NPCState::getType)
            ).apply(instance, NPCState::new)
    );

    private final String type;
    private IntrinsicalRegister<NPCState> owner;

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

    public MutableText getTranslateText() {
        return Text.translatable(translateKey());
    }

    @Override
    public IntrinsicalRegister<NPCState> getOwner() {
        return RegistryManager.NPC_STATE;
    }

    @Override
    public Codec<NPCState> getCodec() {
        return CODEC;
    }
}