package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Objects;

@SuppressWarnings("deprecation")
@Getter
@Setter
@ToString
public class NPCWorkMode implements CodecStep<NPCWorkMode>, RegistryEntryOwnerBindable<NPCWorkMode>, BuiltinObject, RegistryEntryTranslatable {
    public static final Identifier DEFAULT_ID = ReverieDreams.id("combat");
    public static final Codec<NPCWorkMode> CODEC = Codec.lazyInitialized(() -> Codec.STRING.xmap(id -> {
        for (NPCWorkMode npcWorkMode : RegistryImpls.NPC_WORK_MODE) {
            if (Objects.equals(npcWorkMode.type, id)) {
                return npcWorkMode;
            }
        }
        return new NPCWorkMode();
    }, mode -> mode.type == null ? DEFAULT_ID.toString() : mode.type));
    private final String type;
    private final Holder<Item> itemDisplay;
    private RegistryImpl<NPCWorkMode> owner;

    private NPCWorkMode() {
        this.type = null;
        this.itemDisplay = Items.WHITE_DYE.builtInRegistryHolder();
    }

    public NPCWorkMode(String id, Item itemDisplay) {
        this.type = id;
        this.itemDisplay = itemDisplay.builtInRegistryHolder();
    }

    public NPCWorkMode(String id, Holder<Item> itemDisplay) {
        this.type = id;
        this.itemDisplay = itemDisplay;
    }

    public String translateKey() {
        return "gui.npc.work.mode." + this.type;
    }

    public MutableComponent translationKey() {
        return Component.translatable(this.translateKey());
    }

    @Deprecated
    public NPCWorkMode getNext() {
        int rawId = RegistryImpls.NPC_WORK_MODE.getId(this);
        NPCWorkMode npcWorkMode = NPCWorkModes.fromInt(rawId + 1);
        return npcWorkMode == null ? NPCWorkModes.fromInt(0) : npcWorkMode;
    }

    @Deprecated
    public NPCWorkMode getPrevious() {
        int rawId = RegistryImpls.NPC_WORK_MODE.getId(this);

        if (rawId <= 0) {
            int maxId = RegistryImpls.NPC_WORK_MODE.size() - 1;
            return NPCWorkModes.fromInt(maxId);
        }

        return NPCWorkModes.fromInt(rawId - 1);
    }

    @Override
    public Codec<NPCWorkMode> getCodec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (!(obj instanceof NPCWorkMode other)) {
            return false;
        }
        return Objects.equals(this.type, other.type);
    }
}
