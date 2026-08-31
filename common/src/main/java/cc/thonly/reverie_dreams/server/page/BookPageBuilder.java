package cc.thonly.reverie_dreams.server.page;

import cc.thonly.reverie_dreams.server.dialog.ActionButtonBuilder;
import cc.thonly.reverie_dreams.server.dialog.CommonDialogDataBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class BookPageBuilder {
    Identifier key;
    RegistryAccess registryAccess;
    CommonDialogDataBuilder common = new CommonDialogDataBuilder();
    ActionButtonBuilder actions = new ActionButtonBuilder();
    Identifier prev = null;

    protected BookPageBuilder(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public BookPageBuilder key(Identifier key) {
        this.key = key;
        return this;
    }

    public BookPageBuilder prev(Identifier prev) {
        this.prev = prev;
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

    public MutableComponent getTitleKey(Identifier registryKey) {
        return Component.empty().append(Component.translatable(this.getTitleLangKey()));
    }

    public MutableComponent getContentKey(Identifier registryKey) {
        return Component.empty().append(Component.translatable(this.getContentLangKey()));
    }

    public String getTitleLangKey() {
        return this.key.toLanguageKey() + ".title";
    }

    public String getContentLangKey() {
        return this.key.toLanguageKey() + ".content";
    }

    public BookPage build() {
        return new BookPage(this.key, this.registryAccess, this.common, this.actions, this.prev);
    }
}
