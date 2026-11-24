package cc.thonly.reverie_dreams.server.dialog.role;

import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogListDialog;
import net.minecraft.server.dialog.body.PlainMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleCardDialogTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test void roleAndRandomChoicesAreClickableTextWithoutAnyButtons() {
        Dialog dialog = RoleCardDialog.menu("session-one", Component.literal("角色卡"),
                List.of(new RoleCardDialog.Choice(Component.literal("灵梦"), "test:reimu", Optional.empty())), true, false);
        var textOnly = assertInstanceOf(DialogListDialog.class, dialog);
        assertEquals(0, textOnly.dialogs().size());
        assertTrue(textOnly.exitAction().isEmpty());
        assertEquals(4, textOnly.common().body().size());
        CompoundTag role = payload(textOnly, 1);
        assertEquals("session-one", role.getString("session_id").orElseThrow());
        assertEquals("test:reimu", role.getString("entity_id").orElseThrow());
        assertEquals("random", payload(textOnly, 2).getString("entity_id").orElseThrow());
        assertEquals("close", payload(textOnly, 3).getString("entity_id").orElseThrow());
        assertTrue(((PlainMessage) textOnly.common().body().get(1)).contents().getStyle().isUnderlined());
    }

    @Test void groupAndBackLinksCarryTheSameSessionAndUseTheExistingServerCallback() {
        var group = Identifier.parse("test:scarlet");
        var dialog = (DialogListDialog) RoleCardDialog.menu("session-two", Component.literal("分组"),
                List.of(new RoleCardDialog.Choice(Component.literal("红魔乡"), "menu", Optional.of(group))), false, true);
        assertEquals("test:scarlet", payload(dialog, 1).getString("group_id").orElseThrow());
        assertEquals("menu", payload(dialog, 1).getString("entity_id").orElseThrow());
        CompoundTag back = payload(dialog, 2);
        assertEquals("session-two", back.getString("session_id").orElseThrow());
        assertEquals("menu", back.getString("entity_id").orElseThrow());
        assertFalse(back.contains("group_id"));
        assertEquals("close", payload(dialog, 3).getString("entity_id").orElseThrow());
    }

    @Test void anEmptyCardStillHasAnExitLinkAndEscapeIsAllowed() {
        var dialog = (DialogListDialog) RoleCardDialog.menu("empty", Component.literal("Empty"), List.of(), false, false);
        assertEquals(2, dialog.common().body().size());
        assertTrue(dialog.common().canCloseWithEscape());
        assertFalse(dialog.common().pause());
        assertEquals("close", payload(dialog, 1).getString("entity_id").orElseThrow());
    }

    @Test void textOnlyDialogsRoundTripThroughTheActualMinecraftCodec() {
        Dialog original = RoleCardDialog.menu("wire-session", Component.literal("Card"),
                List.of(new RoleCardDialog.Choice(Component.literal("Marisa"), "test:marisa", Optional.empty())), true, true);
        var json = Dialog.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        assertEquals("minecraft:dialog_list", json.getAsJsonObject().get("type").getAsString());
        var decoded = assertInstanceOf(DialogListDialog.class, Dialog.DIRECT_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        assertEquals(0, decoded.dialogs().size());
        assertTrue(decoded.exitAction().isEmpty());
        assertEquals("wire-session", payload(decoded, 1).getString("session_id").orElseThrow());
        assertEquals("test:marisa", payload(decoded, 1).getString("entity_id").orElseThrow());
    }

    @Test void theTextOnlyBuilderRejectsAccidentalButtonConfiguration() {
        var builder = DialogBuilder.builder(b -> {
            b.common(c -> c.title("Card").addTextBody("Content"));
            b.actions(a -> a.addButton(Component.literal("Button"), Optional.empty()));
        }).get();
        assertThrows(IllegalStateException.class, builder::buildTextOnlyOrThrow);
    }

    private static CompoundTag payload(DialogListDialog dialog, int line) {
        var text = assertInstanceOf(PlainMessage.class, dialog.common().body().get(line));
        var click = assertInstanceOf(ClickEvent.Custom.class, text.contents().getStyle().getClickEvent());
        assertEquals(Identifier.parse("reverie_dreams:role/summon"), click.id());
        return assertInstanceOf(CompoundTag.class, click.payload().orElseThrow());
    }
}
