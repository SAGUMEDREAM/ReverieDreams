package cc.thonly.reverie_dreams.server.page;

import cc.thonly.reverie_dreams.server.dialog.ActionButtonBuilder;
import cc.thonly.reverie_dreams.server.dialog.CommonDialogDataBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class BookPageBuilder {
    Identifier key;
    RegistryAccess registryAccess;
    CommonDialogDataBuilder common = new CommonDialogDataBuilder();
    ActionButtonBuilder actions = new ActionButtonBuilder();

    protected BookPageBuilder(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public BookPageBuilder key(Identifier key) {
        this.key = key;
        return this;
    }

    public BookPageBuilder common(Consumer<CommonDialogDataBuilder> function) {
        function.accept(this.common);
        return this;
    }

    public BookPageBuilder action(Consumer<ActionButtonBuilder> function) {
        function.accept(this.actions);
        return this;
    }

    public Identifier getKey() {
        return this.key;
    }

    public BookPage build() {
        return new BookPage(this.key, this.registryAccess, this.common, this.actions);
    }
}
