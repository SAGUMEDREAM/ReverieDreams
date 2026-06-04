package cc.thonly.reverie_dreams.server.dialog;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.action.*;

import java.util.Optional;

@SuppressWarnings("ALL")
public class ActionBuilder {
    public Action commandTemplate(ParsedTemplate template) {
        return new CommandTemplate(template);
    }

    public Action customAll(Identifier id, Optional<CompoundTag> additions) {
        return new CustomAll(id, additions);
    }

    public Action customAll(Identifier id, CompoundTag additions) {
        return new CustomAll(id, Optional.ofNullable(additions));
    }

    public Action staticAction(ClickEvent value) {
        return new StaticAction(value);
    }

    public Action customAction(Identifier id, Tag tag) {
        return new StaticAction(new ClickEvent.Custom(id, Optional.ofNullable(tag)));
    }

    public Action customAction(Identifier id, CompoundTag tag) {
        return new StaticAction(new ClickEvent.Custom(id, Optional.ofNullable(tag)));
    }

    public Action customAction(Identifier id) {
        return new StaticAction(new ClickEvent.Custom(id, Optional.empty()));
    }
}
