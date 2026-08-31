package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
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
import java.util.function.BiConsumer;

@SuppressWarnings("deprecation")
@Getter
@Setter
@ToString
public class NPCWorkMode implements SerializableProvider<NPCWorkMode>, RegistryEntryOwnerBindable<NPCWorkMode>, BuiltinObject, RegistryEntryTranslatable {
    public static final Identifier DEFAULT_ID = ReverieDreams.id("combat");
    public static final Codec<NPCWorkMode> CODEC = Codec.lazyInitialized(() -> Codec.STRING.xmap(id -> {
        for (NPCWorkMode npcWorkMode : BuiltInRegistryProviders.NPC_WORK_MODE) {
            if (Objects.equals(npcWorkMode.typeName, id)) {
                return npcWorkMode;
            }
        }
        return new NPCWorkMode();
    }, mode -> mode.typeName == null ? DEFAULT_ID.toString() : mode.typeName));
    private static final BiConsumer<BaseNPCLikeEntity, NPCWorkMode> DEFAULT_ACTION = (npc, mode) -> {};
    private final String typeName;
    private final Holder<Item> itemDisplay;
    private RegistryProvider<NPCWorkMode> owner;
    private BiConsumer<BaseNPCLikeEntity, NPCWorkMode> onStarted = DEFAULT_ACTION;
    private BiConsumer<BaseNPCLikeEntity, NPCWorkMode> onStop = DEFAULT_ACTION;

    private NPCWorkMode() {
        this.typeName = null;
        this.itemDisplay = Items.WHITE_DYE.builtInRegistryHolder();
    }

    public NPCWorkMode(String typeName, Item itemDisplay) {
        this.typeName = typeName;
        this.itemDisplay = itemDisplay.builtInRegistryHolder();
    }

    public NPCWorkMode(String typeName, Holder<Item> itemDisplay) {
        this.typeName = typeName;
        this.itemDisplay = itemDisplay;
    }

    public NPCWorkMode withStarted(BiConsumer<BaseNPCLikeEntity, NPCWorkMode>function) {
        this.onStarted = function;
        return this;
    }

    public NPCWorkMode withStop(BiConsumer<BaseNPCLikeEntity, NPCWorkMode> function) {
        this.onStop = function;
        return this;
    }

    public void onStarted(BaseNPCLikeEntity npc) {
        this.onStarted.accept(npc, this);
    }

    public void onStop(BaseNPCLikeEntity npc) {
        this.onStop.accept(npc, this);
    }

    public String translateKey() {
        return "gui.npc.work.mode." + this.typeName;
    }

    public MutableComponent translationKey() {
        return Component.translatable(this.translateKey());
    }

    @Deprecated
    public NPCWorkMode getNext() {
        int rawId = BuiltInRegistryProviders.NPC_WORK_MODE.getId(this);
        NPCWorkMode npcWorkMode = NPCWorkModes.fromInt(rawId + 1);
        return npcWorkMode == null ? NPCWorkModes.fromInt(0) : npcWorkMode;
    }

    @Deprecated
    public NPCWorkMode getPrevious() {
        int rawId = BuiltInRegistryProviders.NPC_WORK_MODE.getId(this);

        if (rawId <= 0) {
            int maxId = BuiltInRegistryProviders.NPC_WORK_MODE.size() - 1;
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
        return Objects.equals(this.typeName, other.typeName);
    }
}
