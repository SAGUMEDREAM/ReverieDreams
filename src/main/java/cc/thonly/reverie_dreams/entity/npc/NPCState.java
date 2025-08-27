package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.RegistrableObject;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Getter
public class NPCState implements RegistrableObject<NPCState> {
    public static final String DEFAULT_ID = Touhou.id("normal").toString();
    public static final Codec<NPCState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("value", DEFAULT_ID).forGetter(NPCState::getType)
            ).apply(instance, NPCState::new)
    );

    private Identifier id;
    private final String type;

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
    public StandaloneRegistry<NPCState> getRegistryRef() {
        return RegistryManager.NPC_STATE;
    }

    @Override
    public void setId(Identifier id) {
        this.id = id;
    }

    @Override
    public Codec<NPCState> getCodec() {
        return CODEC;
    }
}