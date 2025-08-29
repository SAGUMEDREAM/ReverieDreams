package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.*;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
public class NPCWorkMode implements CodecStep<NPCWorkMode>, OwnerBinding<NPCWorkMode>, BuiltinObject, Translatable {
    public static final Codec<NPCWorkMode> CODEC = Codec.unit(NPCWorkMode::new);
    public static final Identifier DEFAULT_ID = Touhou.id("combat");
    private final String type;
    private final Item itemDisplay;
    private IntrinsicalRegister<NPCWorkMode> owner;

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
        int rawId = RegistryManager.NPC_WORK_MODE.getRawId(this);
        NPCWorkMode npcWorkMode = NPCWorkModes.fromInt(rawId + 1);
        return npcWorkMode == null ? NPCWorkModes.fromInt(0) : npcWorkMode;
    }

    public NPCWorkMode getPrevious() {
        int rawId = RegistryManager.NPC_WORK_MODE.getRawId(this);
        NPCWorkMode npcWorkMode = NPCWorkModes.fromInt(rawId - 1);
        Map<Integer, RegistryEntry.Reference<NPCState>> baseRawToEntry = RegistryManager.NPC_STATE.getIdToEntryMap();
        int maxKey = Collections.max(baseRawToEntry.keySet());
        return npcWorkMode == null ? NPCWorkModes.fromInt(maxKey) : npcWorkMode;
    }

    @Override
    public Codec<NPCWorkMode> getCodec() {
        return CODEC;
    }
}
