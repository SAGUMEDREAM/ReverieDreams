package cc.thonly.reverie_dreams.server.dialog;

import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings("ALL")
public class CommonDialogDataBuilder {
    static final CommonDialogData DEFAULT = new CommonDialogData(Component.empty(),
            Optional.empty(),
            true,
            true,
            DialogAction.CLOSE,
            new ArrayList<>(),
            new ArrayList<>()
    );
    Component title = Component.empty();
    Optional<Component> externalTitle = Optional.empty();
    boolean canCloseWithEscape = true;
    boolean pause = true;
    DialogAction afterAction = DialogAction.CLOSE;
    List<DialogBody> body = new ArrayList<>();
    List<Input> inputs = new ArrayList<>();

    public CommonDialogDataBuilder() {
    }

    public CommonDialogDataBuilder title(String text) {
        this.title = Component.literal(text);
        return this;
    }

    public CommonDialogDataBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public CommonDialogDataBuilder externalTitle(String text) {
        this.externalTitle = Optional.ofNullable(Component.literal(text));
        return this;
    }

    public CommonDialogDataBuilder externalTitle(Component externalTitle) {
        this.externalTitle = Optional.ofNullable(externalTitle);
        return this;
    }

    public CommonDialogDataBuilder setCanCloseWithEscape(boolean canCloseWithEscape) {
        this.canCloseWithEscape = canCloseWithEscape;
        return this;
    }

    public CommonDialogDataBuilder setPause(boolean pause) {
        this.pause = pause;
        return this;
    }

    public CommonDialogDataBuilder setAfterAction(DialogAction afterAction) {
        this.afterAction = afterAction;
        return this;
    }

    public CommonDialogDataBuilder addBody(BiConsumer<List<DialogBody>, DialogBodyFactory> function) {
        List<DialogBody> list = new ArrayList<>();
        function.accept(list, DialogBodyFactory.INSTANCE);
        this.body.addAll(list);
        return this;
    }

    public CommonDialogDataBuilder input(BiConsumer<List<Input>, InputFactory> function) {
        List<Input> list = new ArrayList<>();
        function.accept(list, InputFactory.INSTANCE);
        this.inputs.addAll(list);
        return this;
    }

    public CommonDialogDataBuilder addTextBody(String text, int width) {
        this.addBody(new BiConsumer<List<DialogBody>, DialogBodyFactory>() {
            @Override
            public void accept(List<DialogBody> dialogBodies, DialogBodyFactory dialogBodyFactory) {
                dialogBodies.add(dialogBodyFactory.plainMessage(text, width));
            }
        });
        return this;
    }

    public CommonDialogDataBuilder addTextBody(Component component, int width) {
        this.addBody(new BiConsumer<List<DialogBody>, DialogBodyFactory>() {
            @Override
            public void accept(List<DialogBody> dialogBodies, DialogBodyFactory dialogBodyFactory) {
                dialogBodies.add(dialogBodyFactory.plainMessage(component, width));
            }
        });
        return this;
    }

    public CommonDialogDataBuilder addTextBody(String text) {
        this.addBody(new BiConsumer<List<DialogBody>, DialogBodyFactory>() {
            @Override
            public void accept(List<DialogBody> dialogBodies, DialogBodyFactory dialogBodyFactory) {
                dialogBodies.add(dialogBodyFactory.plainMessage(text));
            }
        });
        return this;
    }

    public CommonDialogDataBuilder addTextBody(Component component) {
        this.addBody(new BiConsumer<List<DialogBody>, DialogBodyFactory>() {
            @Override
            public void accept(List<DialogBody> dialogBodies, DialogBodyFactory dialogBodyFactory) {
                dialogBodies.add(dialogBodyFactory.plainMessage(component));
            }
        });
        return this;
    }

    public CommonDialogDataBuilder addItemBody(ItemStackTemplate item, Optional<PlainMessage> description, boolean showDecorations, boolean showTooltip, int width, int height) {
        this.addBody(new BiConsumer<List<DialogBody>, DialogBodyFactory>() {
            @Override
            public void accept(List<DialogBody> dialogBodies, DialogBodyFactory dialogBodyFactory) {
                dialogBodies.add(dialogBodyFactory.itemBody(item, description, showDecorations, showTooltip, width, height));
            }
        });
        return this;
    }

    public CommonDialogDataBuilder addItemBody(ItemStackTemplate item, Optional<PlainMessage> description) {
        this.addBody(new BiConsumer<List<DialogBody>, DialogBodyFactory>() {
            @Override
            public void accept(List<DialogBody> dialogBodies, DialogBodyFactory dialogBodyFactory) {
                dialogBodies.add(dialogBodyFactory.itemBody(item, description, true, true, 16, 16));
            }
        });
        return this;
    }

    public CommonDialogDataBuilder test() {
        return this;
    }

    public CommonDialogData build() {
        if (this.body.isEmpty()) {
            throw new RuntimeException("Body is empty");
        }
        return new CommonDialogData(
                this.title,
                this.externalTitle,
                this.canCloseWithEscape,
                this.pause,
                this.afterAction,
                this.body,
                this.inputs
        );
    }
}
