package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import cc.thonly.reverie_dreams.registry.Translatable;
import cc.thonly.reverie_dreams.util.UnitCodec;
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

@SuppressWarnings("deprecation")
@Getter
@Setter
@ToString
public class NPCWorkMode implements CodecStep<NPCWorkMode>, OwnerBinding<NPCWorkMode>, BuiltinObject, Translatable {
    public static final Codec<NPCWorkMode> CODEC = UnitCodec.unit(NPCWorkMode::new);
    public static final Identifier DEFAULT_ID = ReverieDreams.id("combat");
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
}
