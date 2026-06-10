package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.server.page.BookPage;
import cc.thonly.reverie_dreams.server.page.BookPageBuilder;
import cc.thonly.reverie_dreams.util.LazyFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("ALL")
public class BookPageManager {
    private static final BookPageManager INSTANCE = new BookPageManager();
    private final Map<Identifier, Function<RegistryAccess, BookPageBuilder>> registry = new Object2ObjectLinkedOpenHashMap<>(16);
    private final Map<Identifier, ItemStackTemplate> pageItems = new Object2ObjectOpenHashMap<>();

    private BookPageManager() {
    }

    public void register(Identifier key, Function<RegistryAccess, BookPageBuilder> pageSupplier) {
        if (this.registry.containsKey(key)) {
            throw new RuntimeException("Duplicate key %s".formatted(key));
        }
        if (this.registry.containsValue(pageSupplier)) {
            throw new RuntimeException("Duplicate page value %s".formatted(pageSupplier));
        }
        this.registry.put(key, pageSupplier);
    }

    public void open(Identifier id, ServerPlayer player) {
        BookPage page = this.getPage(id, player.registryAccess());
        if (page == null) {
            return;
        }
        page.open(player);
    }

    public void openRoot(String namespace, ServerPlayer player) {
        RegistryAccess registryAccess = player.registryAccess();
        List<Identifier> keys = new ArrayList<>();
        this.registry.forEach((id, func) -> {
            if (Objects.equals(id.getNamespace(), namespace)) {
                keys.add(id);
            }
        });
        DialogBuilder.builder(builder -> {
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
                                                              .append(Component.translatable(this.getPageId(key)))
                                                              .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.Custom(CustomClickActionRegistry.PAGE_GOTO_KEY, Optional.of(tag)))),
                                            128)),
                            true,
                            false,
                            128,
                            16
                    );

                }
            });
            builder.exitAction(Component.literal("Close"), 180, Optional.empty());
        });
    }

    public boolean openIfExists(Identifier id, ServerPlayer player) {
        BookPage page = this.getPage(id, player.registryAccess());
        if (page == null) {
            return false;
        }
        page.open(player);
        return true;
    }

    public BookPage getPage(Identifier key, RegistryAccess registryAccess) {
        Function<RegistryAccess, BookPageBuilder> function = this.registry.get(key);
        if (function == null) {
            return null;
        }
        return function.apply(registryAccess).build();
    }

    public void reload() {
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

    public static String getPageId(Identifier key) {
        return key.toLanguageKey();
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
        BookPage page = getInstance().getPage(key, player.registryAccess());
        page.open(player);
    }

    public static BookPageManager getInstance() {
        return INSTANCE;
    }
}
