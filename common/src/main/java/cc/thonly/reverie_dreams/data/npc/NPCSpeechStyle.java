package cc.thonly.reverie_dreams.data.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

/**
 * Shared presentation metadata; ordinary clients can still render the vanilla text display.
 */
public final class NPCSpeechStyle {
    private static final String MARKER = "reverie_dreams:npc_speech/";
    public static final int BODY_COLOR = 0xF2F6F3;
    public static final int NAME_COLOR = 0xAFCAE6;
    public static final int BORDER_COLOR = 0x405FA5;
    public static final int BACKGROUND_RGB = 0x1C284C;
    public static final int BACKGROUND_ALPHA = 242;
    public static final int LINE_WIDTH = 176;
    public static final float SCALE = 0.72F;
    public static final int PADDING = 6;
    public static final int LINE_HEIGHT = 12;

    private NPCSpeechStyle() {
    }

    public static Component format(Entity speaker, Component line) {
        // Insertion is invisible display metadata. Do not reserve vanilla flags or add a packet type.
        return Component.empty().withStyle(style -> style.withInsertion(MARKER + speaker.getUUID()))
                .append(speaker.getName().copy().withStyle(style -> style.withColor(NAME_COLOR).withBold(true)))
                .append("\n")
                .append(Component.empty().withColor(BODY_COLOR).append(line.copy()));
    }

    public static boolean isSpeech(Component text) {
        String insertion = text.getStyle().getInsertion();
        return insertion != null && insertion.startsWith(MARKER);
    }

    public static boolean belongsTo(Component text, Entity speaker) {
        return (MARKER + speaker.getUUID()).equals(text.getStyle().getInsertion());
    }
}
