package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import com.mojang.serialization.Codec;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("SpellCheckingInspection")
public class NPCMenuType {
    public static final Codec<NPCMenuType> BY_REGISTRY_CODEC = Codec.lazyInitialized(() -> Identifier.CODEC.xmap(key -> {
        NPCMenuType menuType = BuiltInRegistryProviders.NPC_MENU_TYPE.getValue(key);
        if (menuType == null) {
            throw new NullPointerException("No NPCMenuType for %s found".formatted(key));
        }
        return menuType;
    }, menuType -> {
        Identifier key = BuiltInRegistryProviders.NPC_MENU_TYPE.getKey(menuType);
        if (key == null) {
            return Identifier.withDefaultNamespace("undefined");
        }
        return key;
    }));
    private ElementFactory factory = ElementFactory.DEFAULT;
    private NPCPredicate predicate = NPCPredicate.DEFAULT;

    public NPCMenuType() {

    }

    public NPCMenuType(ElementFactory factory) {
        this.factory = factory;
    }

    public NPCMenuType(ElementFactory factory, NPCPredicate predicate) {
        this.factory = factory;
        this.predicate = predicate;
    }

    public GuiElementBuilder create(ServerPlayer player, BaseNPCLikeEntity npc, SimpleGui currentGui) {
        if (this.factory == ElementFactory.DEFAULT) {
            return new GuiElementBuilder().setItem(Items.AIR);
        }
        return this.factory.create(player, npc, currentGui);
    }

    public boolean test(ServerPlayer player, BaseNPCLikeEntity npc) {
        return this.predicate.isEnabled(player, npc) && ElementFactory.DEFAULT != this.factory;
    }

    public NPCMenuType factory(ElementFactory factory) {
        this.factory = factory;
        return this;
    }

    public NPCMenuType predicate(NPCPredicate predicate) {
        this.predicate = predicate;
        return this;
    }

    @FunctionalInterface
    public interface ElementFactory {
        ElementFactory DEFAULT = (player, npc, currentGui) -> new GuiElementBuilder();

        GuiElementBuilder create(ServerPlayer player, BaseNPCLikeEntity npc, @Nullable SimpleGui currentGui);

    }

    @FunctionalInterface
    public interface NPCPredicate {
        NPCPredicate DEFAULT = (player, npc) -> true;

        boolean isEnabled(ServerPlayer player, BaseNPCLikeEntity npc);

        default NPCPredicate andThen(NPCPredicate predicate) {
            return (player, npc) -> this.isEnabled(player, npc) && predicate.isEnabled(player, npc);
        }
    }
}
