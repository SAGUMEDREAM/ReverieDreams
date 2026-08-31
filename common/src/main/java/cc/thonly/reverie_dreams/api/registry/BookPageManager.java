package cc.thonly.reverie_dreams.api.registry;

import cc.thonly.reverie_dreams.server.BookPageManagerImpl;
import cc.thonly.reverie_dreams.server.page.BookPage;
import cc.thonly.reverie_dreams.server.page.BookPageBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BookPageManager {
    void bindItem(Identifier key, ItemStackTemplate icon);

    void register(Identifier key, Function<RegistryAccess, BookPageBuilder> pageSupplier);

    void open(Identifier id, ServerPlayer player);

    void openRoot(String namespace, ServerPlayer player);

    boolean openIfExists(Identifier id, ServerPlayer player);

    BookPage getPage(Identifier key, RegistryAccess registryAccess);

    void reload();

    static void addInitializer(Consumer<BookPageManager> initializer) {
        BookPageManagerImpl.addInitializer(initializer);
    }

    static BookPageManagerImpl getInstance() {
        return BookPageManagerImpl.getInstance();
    }
}
