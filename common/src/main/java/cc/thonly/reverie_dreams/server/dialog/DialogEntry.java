package cc.thonly.reverie_dreams.server.dialog;

import cc.thonly.reverie_dreams.util.LazySupplier;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;


@Slf4j
@ToString
@SuppressWarnings("ALL")
public class DialogEntry {
    final Identifier key;
    final Holder<Dialog> holder;

    protected DialogEntry(Identifier key, Dialog dialog) {
        this(key, Holder.direct(dialog));
    }

    protected DialogEntry(Identifier key, Holder<Dialog> holder) {
        this.key = key;
        this.holder = holder;
    }

    public void open(Player player) {
        try {
            player.openDialog(this.holder);
        } catch (Exception e) {
            log.error("Can't open dialog {}", this.key, e);
        }
    }

    public Identifier key() {
        return this.key;
    }

    public Holder<Dialog> get() {
        return this.holder;
    }

    public static LazySupplier<DialogBuilder> builder(Consumer<DialogBuilder> consumer) {
        return LazySupplier.of(() -> {
            DialogBuilder dialogBuilder = new DialogBuilder();
            consumer.accept(dialogBuilder);
            return dialogBuilder;
        });
    }

}
