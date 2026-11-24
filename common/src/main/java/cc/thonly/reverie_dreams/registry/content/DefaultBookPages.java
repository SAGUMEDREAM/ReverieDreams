package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.BookPageManager;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.page.BookPage;
import cc.thonly.reverie_dreams.server.page.BookPageBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class DefaultBookPages {
    private static boolean INIT = false;
    public static final Identifier BASIC_GETTING_STARTED = key("basic/getting_started");
    public static final Identifier BASIC_GENSOKYO_ALTAR = key("basic/gensokyo_altar");
    public static final Identifier BASIC_DANMAKU_TUTORIAL = key("basic/danmaku_tutorial");
    public static final Identifier BASIC_FUMO_TUTORIAL = key("basic/fumo_tutorial");
    public static final Identifier BASIC_ROLE_AND_PARTNER = key("basic/role_and_partner");
    public static final Identifier BASIC_TOUHOU_MYSTIA = key("basic/touhou_mystia");
    public static final Identifier BASIC_BEVERAGE = key("basic/beverage");
    public static final Identifier OTHER_COMPAT = key("other/compat");
    public static final Identifier ALTAR_CRAFTING = key("basic/altar_crafting");
    public static final Identifier DANMAKU_STRENGTHENING = key("basic/danmaku_strengthening");
    public static final Identifier WEAPONS = key("basic/weapons");
    public static final Identifier NPC_WORK = key("basic/npc_work");
    public static final Identifier NPC_LOGISTICS = key("basic/npc_logistics");
    public static final Identifier NPC_STORAGE = key("basic/npc_storage");
    public static final Identifier CROPS = key("basic/crops");
    public static final Identifier RESTAURANT = key("basic/restaurant");
    public static final Identifier RESTAURANT_ORDERS = key("basic/restaurant_orders");
    public static final Identifier EXPLORATION = key("basic/exploration");
    public static final Identifier MOON = key("basic/moon");
    public static final Identifier TROUBLESHOOTING = key("other/troubleshooting");
    public static final Identifier WORKSHOP = key("basic/workshop");
    public static final Identifier RESTAURANT_STAFF = key("basic/restaurant_staff");
    public static final Identifier GENSOKYO_ROUTE = key("basic/gensokyo_route");
    public static final Identifier AUTHORIZED = key("other/authorized");

    // Keep existing IDs valid for guide items, addons and saved clickable links.
    public static final List<Chapter> CHAPTERS = List.of(
            new Chapter(BASIC_GETTING_STARTED, List.of()),
            new Chapter(BASIC_GENSOKYO_ALTAR, List.of(ALTAR_CRAFTING)),
            new Chapter(BASIC_DANMAKU_TUTORIAL, List.of(DANMAKU_STRENGTHENING, WEAPONS)),
            new Chapter(BASIC_ROLE_AND_PARTNER, List.of(NPC_WORK, NPC_LOGISTICS, NPC_STORAGE)),
            new Chapter(BASIC_TOUHOU_MYSTIA, List.of(CROPS, BASIC_BEVERAGE, WORKSHOP)),
            new Chapter(RESTAURANT, List.of(RESTAURANT_ORDERS, RESTAURANT_STAFF)),
            new Chapter(EXPLORATION, List.of(GENSOKYO_ROUTE, MOON)),
            new Chapter(BASIC_FUMO_TUTORIAL, List.of(OTHER_COMPAT, TROUBLESHOOTING)),
            new Chapter(AUTHORIZED, List.of())
    );

    public record Chapter(Identifier id, List<Identifier> sections) {
        public Chapter {
            sections = List.copyOf(sections);
        }
    }

    public static synchronized void initialize() {
        if (INIT) {
            return;
        }
        BookPageManager.addInitializer(DefaultBookPages::bootstrap);
        INIT = true;
    }

    private static final Identifier ROOT_ID = ReverieDreams.id("root/reverie_dreams");

    private static void bootstrap(BookPageManager manager) {
        registerPages(manager);

        manager.bindItem(BASIC_GETTING_STARTED, new ItemStackTemplate(Items.PLAYER_HEAD));
        manager.bindItem(BASIC_GENSOKYO_ALTAR, new ItemStackTemplate(RDBlocks.GENSOKYO_ALTAR.asItem()));
        manager.bindItem(BASIC_DANMAKU_TUTORIAL, new ItemStackTemplate(RDItems.DANMAKU.asItem()));
        manager.bindItem(BASIC_FUMO_TUTORIAL, new ItemStackTemplate(RDItems.FUMO_ICON.asItem()));
        manager.bindItem(BASIC_ROLE_AND_PARTNER, RoleCards.KOUMAKYOU.getTemplate());
        manager.bindItem(BASIC_TOUHOU_MYSTIA, new ItemStackTemplate(RDItems.MYSTIA_ICON.asItem()));
        manager.bindItem(RESTAURANT, new ItemStackTemplate(RDBlocks.MYSTIA_BELL.asItem()));
        manager.bindItem(EXPLORATION, new ItemStackTemplate(RDItems.BOUNDARY_OFUDA.asItem()));
        manager.bindItem(AUTHORIZED, new ItemStackTemplate(Items.WRITABLE_BOOK));
    }

    /** Register text and navigation independently of the optional item icons. */
    public static void registerPages(BookPageManager manager) {
        List<Identifier> readingOrder = CHAPTERS.stream()
                .flatMap(chapter -> Stream.concat(Stream.of(chapter.id()), chapter.sections().stream()))
                .toList();
        for (Chapter chapter : CHAPTERS) {
            List<Identifier> pages = Stream.concat(Stream.of(chapter.id()), chapter.sections().stream()).toList();
            for (Identifier id : pages) {
                int index = readingOrder.indexOf(id);
                manager.register(id, access -> buildPage(access, id, chapter, readingOrder, index));
            }
        }
    }

    private static BookPageBuilder buildPage(RegistryAccess access, Identifier id, Chapter chapter,
                                             List<Identifier> readingOrder, int index) {
        boolean isChapter = id.equals(chapter.id());
        BookPageBuilder builder = BookPage.builder(access).key(id).prev(isChapter ? ROOT_ID : chapter.id()).rootEntry(isChapter);
        builder.common(common -> {
            common.title(builder.getTitleKey(id));
            common.addTextBody(builder.getContentKey(id), 320);
            if (id.equals(BASIC_GETTING_STARTED)) {
                for (Identifier target : List.of(BASIC_GENSOKYO_ALTAR, BASIC_ROLE_AND_PARTNER, BASIC_TOUHOU_MYSTIA,
                        RESTAURANT, EXPLORATION, TROUBLESHOOTING)) {
                    common.addTextBody(readLink(target), 320);
                }
            }
            if (id.equals(AUTHORIZED)) {
                common.addTextBody(sourceLink(Component.literal("大猫凌霜 · Bilibili"), "https://space.bilibili.com/7039204/"), 320);
                common.addTextBody(sourceLink(Component.translatable("guidebook.reverie_dreams.map_source"),
                        "https://www.bilibili.com/video/BV1adYF66Ehj/"), 320);
            }
            if (isChapter) {
                for (Identifier section : chapter.sections()) {
                    common.addTextBody(readLink(section), 320);
                }
            } else {
                common.addTextBody(link(chapter.id(), Component.translatable("guidebook.reverie_dreams.chapter",
                        Component.translatable(chapter.id().toLanguageKey() + ".title"))), 320);
            }
            if (index > 0) {
                Identifier previous = readingOrder.get(index - 1);
                common.addTextBody(link(previous, Component.translatable("guidebook.reverie_dreams.previous",
                        Component.translatable(previous.toLanguageKey() + ".title"))), 320);
            }
            if (index + 1 < readingOrder.size()) {
                Identifier next = readingOrder.get(index + 1);
                common.addTextBody(link(next, Component.translatable("guidebook.reverie_dreams.next",
                        Component.translatable(next.toLanguageKey() + ".title"))), 320);
            }
            common.addTextBody(link(ROOT_ID, Component.translatable("guidebook.reverie_dreams.contents")), 320);
        });
        return builder;
    }

    private static Component readLink(Identifier target) {
        return link(target, Component.translatable("guidebook.reverie_dreams.read",
                Component.translatable(target.toLanguageKey() + ".title")));
    }

    private static Component sourceLink(Component label, String url) {
        return label.copy().withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenUrl(URI.create(url))));
    }

    private static Component link(Identifier target, Component label) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", target.toString());
        return label.copy().withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withUnderlined(true)
                .withClickEvent(new ClickEvent.Custom(CustomClickActionRegistry.PAGE_GOTO_KEY, Optional.of(tag))));
    }

    public static Identifier key(String name) {
        return ReverieDreams.id("page/" + name);
    }
}
