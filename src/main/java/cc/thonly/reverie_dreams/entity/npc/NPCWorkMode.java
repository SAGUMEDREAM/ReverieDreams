package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.RegistrableObject;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
public class NPCWorkMode implements RegistrableObject<NPCWorkMode> {
    public static final Codec<NPCWorkMode> CODEC = Codec.unit(NPCWorkMode::new);
    public static final String DEFAULT_ID = Touhou.id("combat").toString();
    private final String type;
    private final Item itemDisplay;
    private Identifier id;

    private NPCWorkMode() {
        this.type = null;
        this.itemDisplay = Items.WHITE_DYE;
    }

    public NPCWorkMode(String id, Item itemDisplay) {
        this.type = id;
        this.itemDisplay = itemDisplay;
    }

    public String translateKey() {
        return "gui.npc.work.mode." + this.type;
    }

    public MutableText translationKey() {
        return Text.translatable(this.translateKey());
    }

    public NPCWorkMode getNext() {
        Integer rawId = RegistryManager.NPC_WORK_MODE.getRawId(this);
        NPCWorkMode npcWorkMode = NPCWorkModes.fromInt(rawId + 1);
        return npcWorkMode == null ? NPCWorkModes.fromInt(0) : npcWorkMode;
    }

    public NPCWorkMode getPrevious() {
        Integer rawId = RegistryManager.NPC_WORK_MODE.getRawId(this);
        NPCWorkMode npcWorkMode = NPCWorkModes.fromInt(rawId - 1);
        Map<Integer, NPCState> baseRawToEntry = RegistryManager.NPC_STATE.getBaseRawToEntry();
        int maxKey = Collections.max(baseRawToEntry.keySet());
        return npcWorkMode == null ? NPCWorkModes.fromInt(maxKey) : npcWorkMode;
    }

    @Override
    public StandaloneRegistry<NPCWorkMode> getRegistryRef() {
        return RegistryManager.NPC_WORK_MODE;
    }

    @Override
    public Codec<NPCWorkMode> getCodec() {
        return CODEC;
    }
}
