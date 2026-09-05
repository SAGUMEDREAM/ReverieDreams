package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.BookPageManager;
import cc.thonly.reverie_dreams.server.dialog.ActionBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogEntry;
import cc.thonly.reverie_dreams.server.page.BookPage;
import cc.thonly.reverie_dreams.server.page.BookPageBuilder;
import cc.thonly.reverie_dreams.util.LazyFunction;
import cc.thonly.reverie_dreams.util.LazySupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SuppressWarnings("ALL")
public class BookPageManagerImpl implements BookPageManager {
    private static final BookPageManagerImpl INSTANCE = new BookPageManagerImpl();
    private static final List<Consumer<BookPageManager>> INITIALIZERS = new ArrayList<>();
    private final Map<Identifier, Function<RegistryAccess, BookPageBuilder>> registry = new Object2ObjectLinkedOpenHashMap<>(16);
    private final Map<Identifier, ItemStackTemplate> pageItems = new Object2ObjectOpenHashMap<>();

    protected BookPageManagerImpl() {
    }

    public static void addInitializer(Consumer<BookPageManager> initializer) {
        if (INITIALIZERS.contains(initializer)) {
            log.error("Duplicate initializer {}", initializer);
            return;
        }
        INITIALIZERS.add(initializer);
    }

    @Override
    public void bindItem(Identifier key, ItemStackTemplate icon) {
        if (this.pageItems.containsKey(key)) {
            throw new RuntimeException("Duplicate key %s".formatted(key));
        }
        if (this.pageItems.containsValue(icon)) {
            throw new RuntimeException("Duplicate page value %s".formatted(icon));
        }
        this.pageItems.put(key, icon);
    }

    @Override
    public void register(Identifier key, Function<RegistryAccess, BookPageBuilder> pageSupplier) {
        if (this.registry.containsKey(key)) {
            throw new RuntimeException("Duplicate key %s".formatted(key));
        }
        if (this.registry.containsValue(pageSupplier)) {
            throw new RuntimeException("Duplicate page value %s".formatted(pageSupplier));
        }
        this.registry.put(key, pageSupplier);
    }

    @Override
    public void open(Identifier id, ServerPlayer player) {
        BookPage page = this.getPage(id, player.registryAccess());
        if (page == null) {
            return;
        }
        page.open(player);
    }

    @Override
    public void openRoot(String namespace, ServerPlayer player) {
        RegistryAccess registryAccess = player.registryAccess();
        List<Identifier> keys = new ArrayList<>();
        this.registry.forEach((id, func) -> {
            if (Objects.equals(id.getNamespace(), namespace)) {
                keys.add(id);
            }
        });
        LazySupplier<DialogBuilder> dialogBuilderLazy = DialogBuilder.builder(builder -> {
            builder.common(common -> {
                common.title(Component.translatable(getRootId(namespace)));
                common.addTextBody(Component.translatable(this.getRootDescription(namespace)));
                for (Identifier key : keys) {
                    ItemStackTemplate template = this.pageItems.get(key);
                    if (template == null) {
                        template = new ItemStackTemplate(Items.BOOK);
                    }
                    CompoundTag tag = new CompoundTag();
                    tag.putString("id", key.toString());
                    common.addItemBody(template,
                            Optional.of(
                                    new PlainMessage(Component.empty()
                                                              .append(Component.translatable(titleLangKey(key)))
                                                              .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.Custom(CustomClickActionRegistry.PAGE_GOTO_KEY, Optional.of(tag)))),
                                            128)),
                            false,
                            false,
                            16,
                            16
                    );
                }
            });
            builder.actions(action -> {
                ActionBuilder actionBuilder = action.actionBuilder();
                action.addButton(Component.literal("gui.reverie_dreams.close"), Optional.empty(), 180, Optional.empty());
            });
        });
        DialogBuilder dialogBuilder = dialogBuilderLazy.get();
        DialogEntry dialogEntry = dialogBuilder.buildOrThrow();
        dialogEntry.open(player);
    }

    public static MutableComponent titleKey(Identifier registryKey) {
        return Component.empty().append(Component.translatable(titleLangKey(registryKey)));
    }

    public static MutableComponent contentKey(Identifier registryKey) {
        return Component.empty().append(Component.translatable(contentLangKey(registryKey)));
    }

    public static String titleLangKey(Identifier registryKey) {
        return registryKey.toLanguageKey() + ".title";
    }

    public static String contentLangKey(Identifier registryKey) {
        return registryKey.toLanguageKey() + ".content";
    }

    @Override
    public boolean openIfExists(Identifier id, ServerPlayer player) {
        BookPage page = this.getPage(id, player.registryAccess());
        if (page == null) {
            return false;
        }
        page.open(player);
        return true;
    }

    @Override
    public BookPage getPage(Identifier key, RegistryAccess registryAccess) {
        Function<RegistryAccess, BookPageBuilder> function = this.registry.get(key);
        if (function == null) {
            return null;
        }
        return function.apply(registryAccess).build();
    }

    @Override
    public void reload() {
        this.pageItems.clear();
        this.registry.clear();
        INITIALIZERS.forEach(consumer -> consumer.accept(this));
        this.registry.forEach((id, func) -> {
            if (func instanceof LazyFunction<RegistryAccess, BookPageBuilder> lazyFunc) {
                lazyFunc.unbound();
            }
        });
    }

    public static String getRootDescription(String namespace) {
        return "book_page.%s.root.description".formatted(namespace);
    }

    public static String getRootId(String namespace) {
        return "book_page.%s.root.title".formatted(namespace);
    }

    public static Identifier getParent(Identifier id) {
        String path = id.getPath();
        int i = path.lastIndexOf('/');
        if (i <= 0) {
            return null;
        }
        return Identifier.fromNamespaceAndPath(id.getNamespace(), path.substring(0, i));
    }

    public static void handlePageBack(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        MinecraftServer server = ReverieDreams.getServer();
        if (server == null) {
            return;
        }
        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
            return;
        }
        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
            return;
        }
        String idStr = compound.getStringOr("id", "");
        if (idStr.isEmpty()) {
            return;
        }
        Identifier key = Identifier.tryParse(idStr);
        if (key == null) {
            return;
        }

        String path = key.getPath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return;
        }
        if (path.isEmpty()) {
            return;
        }
        String prevPath = path.substring(0, lastSlash);
        Identifier prevKey = Identifier.fromNamespaceAndPath(key.getNamespace(), prevPath);
        getInstance().openIfExists(prevKey, player);
    }

    public static void handlePageGoto(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        MinecraftServer server = ReverieDreams.getServer();
        if (server == null) {
            return;
        }
        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
            return;
        }
        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
            return;
        }
        String idStr = compound.getStringOr("id", "");
        if (idStr.isEmpty()) {
            return;
        }
        Identifier key = Identifier.tryParse(idStr);
        if (key == null) {
            return;
        }
        String path = key.getPath();
        if (path.startsWith("root/")) {
            String namespace = path.substring("root/".length());
            getInstance().openRoot(namespace, player);
            return;
        }
        BookPage page = getInstance().getPage(key, player.registryAccess());
        page.open(player);
    }

    public static BookPageManagerImpl getInstance() {
        return INSTANCE;
    }
}
