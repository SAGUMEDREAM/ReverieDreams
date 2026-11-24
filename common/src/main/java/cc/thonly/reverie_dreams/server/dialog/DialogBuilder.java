package cc.thonly.reverie_dreams.server.dialog;

import cc.thonly.reverie_dreams.util.lazy.LazySupplier;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.DialogListDialog;
import net.minecraft.server.dialog.action.Action;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("ALL")
public class DialogBuilder {
    static int NEXT_ID = 0;
    Identifier key;
    CommonDialogDataBuilder common = new CommonDialogDataBuilder();
    ActionButtonBuilder actions = new ActionButtonBuilder();
    Optional<ActionButton> exitAction = Optional.empty();
    int columns = 1;

    DialogBuilder() {
    }

    public static LazySupplier<DialogBuilder> builder(Consumer<DialogBuilder> consumer) {
        return LazySupplier.of(() -> {
            DialogBuilder dialogBuilder = new DialogBuilder();
            consumer.accept(dialogBuilder);
            return dialogBuilder;
        });
    }

    public DialogBuilder key(Identifier key) {
        this.key = key;
        return this;
    }

    public DialogBuilder common(Consumer<CommonDialogDataBuilder> function) {
        function.accept(this.common);
        return this;
    }

    public DialogBuilder actions(Consumer<ActionButtonBuilder> function) {
        function.accept(this.actions);
        return this;
    }

    public DialogBuilder replaceCommonBuilder(CommonDialogDataBuilder builder) {
        this.common = builder;
        return this;
    }

    public DialogBuilder replaceActionsBuilder(ActionButtonBuilder builder) {
        this.actions = builder;
        return this;
    }

    public DialogBuilder columns(int columns) {
        this.columns = Math.max(1, columns);
        return this;
    }

    public DialogBuilder exitAction(Component label, int width, Optional<Action> action) {
        this.exitAction = Optional.ofNullable(new ActionButton(new CommonButtonData(label, width), action));
        return this;
    }

    public DialogBuilder exitAction(Component label, Optional<Component> tooltip, int width, Optional<Action> action) {
        this.exitAction = Optional.ofNullable(new ActionButton(new CommonButtonData(label, tooltip, width), action));
        return this;
    }

    public Supplier<DialogEntry> build() {
        if (this.key == null) {
            this.key = Identifier.withDefaultNamespace(String.valueOf("default_dialog_id_" + ++NEXT_ID));
        }
        return () -> new DialogEntry(this.key, new MultiActionDialog(
                this.common.build(),
                this.actions.build(),
                this.exitAction,
                this.columns)
        );
    }

    public DialogEntry buildOrThrow() {
        return this.build().get();
    }

    public DialogEntry buildTextOnlyOrThrow() {
        if (!this.actions.build().isEmpty() || this.exitAction.isPresent()) {
            throw new IllegalStateException("A text-only dialog cannot contain action buttons");
        }
        if (this.key == null) {
            this.key = Identifier.withDefaultNamespace("default_dialog_id_" + ++NEXT_ID);
        }
        // MultiActionDialog requires a non-empty actions list on the wire.
        // An empty native dialog list supports clickable body text without adding buttons.
        return new DialogEntry(this.key, new DialogListDialog(
                this.common.build(), HolderSet.direct(), Optional.empty(), 1, 180));
    }

}
