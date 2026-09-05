package cc.thonly.reverie_dreams.server.dialog;

import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import cc.thonly.keine.item.ItemStackTemplate;

import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "UnusedReturnValue"})
public class DialogBodyFactory {
    static DialogBodyFactory INSTANCE = new DialogBodyFactory();

    public ItemBody itemBody(ItemStackTemplate item, Optional<PlainMessage> description, boolean showDecorations, boolean showTooltip, int width, int height) {
        return new ItemBody(item.create(), description, showDecorations, showTooltip, width, height);
    }

    public ItemBody itemBody(ItemStackTemplate item) {
        return new ItemBody(item.create(), Optional.empty(), true, true, 16, 16);
    }

    public PlainMessage plainMessage(String text, int width) {
        return new PlainMessage(Component.literal(text), width);
    }

    public PlainMessage plainMessage(Component contents, int width) {
        return new PlainMessage(contents, width);
    }

    public PlainMessage plainMessage(String text) {
        return new PlainMessage(Component.literal(text), 180);
    }

    public PlainMessage plainMessage(Component contents) {
        return new PlainMessage(contents, 280);
    }
}
