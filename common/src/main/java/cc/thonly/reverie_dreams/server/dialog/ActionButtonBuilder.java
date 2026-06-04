package cc.thonly.reverie_dreams.server.dialog;

import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
public class ActionButtonBuilder {
    final List<ActionButton> actions = new ArrayList<>();
    final ActionBuilder actionBuilder = new ActionBuilder();

    public ActionBuilder actionBuilder() {
        return this.actionBuilder;
    }

    public ActionButtonBuilder addButton(Component label, int width, Optional<Action> action) {
        this.actions.add(new ActionButton(new CommonButtonData(label, width), action));
        return this;
    }

    public ActionButtonBuilder addButton(Component label, Optional<Component> tooltip, int width, Optional<Action> action) {
        this.actions.add(new ActionButton(new CommonButtonData(label, tooltip, width), action));
        return this;
    }

    public ActionButtonBuilder addButton(Component label, Optional<Action> action) {
        this.actions.add(new ActionButton(new CommonButtonData(label, 180), action));
        return this;
    }

    public ActionButtonBuilder addButton(Component label, Optional<Component> tooltip, Optional<Action> action) {
        this.actions.add(new ActionButton(new CommonButtonData(label, tooltip, 180), action));
        return this;
    }

    public List<ActionButton> build() {
        return new ArrayList<>(this.actions);
    }
}
