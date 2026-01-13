package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
public class NPCWorkMode implements CodecStep<NPCWorkMode>, OwnerBinding<NPCWorkMode>, BuiltinObject, Translatable {
    public static final Codec<NPCWorkMode> CODEC = Codec.unit(NPCWorkMode::new);
    public static final ResourceLocation DEFAULT_ID = ReverieDreams.id("combat");
    private final String type;
    private final Item itemDisplay;
    private RegistryHandler<NPCWorkMode> owner;

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

    public MutableComponent translationKey() {
        return Component.translatable(this.translateKey());
    }

    @Deprecated
    public NPCWorkMode getNext() {
        int rawId = RegistryHandlers.NPC_WORK_MODE.getId(this);
        NPCWorkMode npcWorkMode = NPCWorkModes.fromInt(rawId + 1);
        return npcWorkMode == null ? NPCWorkModes.fromInt(0) : npcWorkMode;
    }

    @Deprecated
    public NPCWorkMode getPrevious() {
        int rawId = RegistryHandlers.NPC_WORK_MODE.getId(this);

        if (rawId <= 0) {
            int maxId = RegistryHandlers.NPC_WORK_MODE.size() - 1;
            return NPCWorkModes.fromInt(maxId);
        }

        return NPCWorkModes.fromInt(rawId - 1);
    }

    @Override
    public Codec<NPCWorkMode> getCodec() {
        return CODEC;
    }
}
