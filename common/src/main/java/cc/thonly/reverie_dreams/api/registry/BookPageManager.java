package cc.thonly.reverie_dreams.api.registry;

import cc.thonly.reverie_dreams.server.page.BookPage;
import cc.thonly.reverie_dreams.server.page.BookPageBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStackTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
        Holder.addInitializer(initializer);
    }

    static BookPageManager getInstance() {
        return Holder.getInstance();
    }

    static void setInstance(BookPageManager manager) {
        Holder.instance = manager;
    }

    static List<Consumer<BookPageManager>> initializers() {
        return Holder.initializers();
    }

    final class Holder {
        private static final Logger LOG = LoggerFactory.getLogger(BookPageManager.class);
        private static final List<Consumer<BookPageManager>> INITIALIZERS = new ArrayList<>();
        private static volatile BookPageManager instance;

        private static void addInitializer(Consumer<BookPageManager> initializer) {
            if (INITIALIZERS.contains(initializer)) {
                LOG.error("Duplicate initializer {}", initializer);
                return;
            }
            INITIALIZERS.add(initializer);
        }

        private static BookPageManager getInstance() {
            BookPageManager manager = instance;
            if (manager == null) {
                throw new IllegalStateException("Book page manager implementation is not registered");
            }
            return manager;
        }

        private static List<Consumer<BookPageManager>> initializers() {
            return List.copyOf(INITIALIZERS);
        }
    }
}
