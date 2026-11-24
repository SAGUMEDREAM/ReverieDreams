package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.registry.content.DefaultBookPages;
import cc.thonly.reverie_dreams.server.page.BookPage;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GuidebookNavigationTest {
    private static final Identifier ROOT = Identifier.parse("reverie_dreams:root/reverie_dreams");
    private BookPageManagerImpl manager;

    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach void registerPages() {
        manager = new BookPageManagerImpl();
        DefaultBookPages.registerPages(manager);
    }

    @Test void rootListsNineChaptersInGameplayOrderAndSurvivesTheWireCodec() {
        var dialog = roundTrip(manager.createRootDialog("reverie_dreams", RegistryAccess.EMPTY).get().value());
        var expected = DefaultBookPages.CHAPTERS.stream().map(DefaultBookPages.Chapter::id).toList();
        var actual = dialog.common().body().stream().filter(ItemBody.class::isInstance)
                .map(ItemBody.class::cast).map(item -> target(item.description().orElseThrow().contents())).toList();
        assertEquals(9, actual.size());
        assertEquals(DefaultBookPages.AUTHORIZED, actual.getLast());
        assertEquals(expected, actual);
        assertEquals(expected, manager.getRootPages("reverie_dreams", RegistryAccess.EMPTY));
        assertEquals(1, dialog.actions().size());
    }

    @Test void everyPageHasValidLinksAndSubpagesCanReturnToTheirChapterAndContents() {
        int count = 0;
        for (var chapter : DefaultBookPages.CHAPTERS) {
            List<Identifier> ids = new ArrayList<>(List.of(chapter.id()));
            ids.addAll(chapter.sections());
            for (var id : ids) {
                BookPage page = manager.getPage(id, RegistryAccess.EMPTY);
                assertNotNull(page, id.toString());
                var dialog = roundTrip(page.createDialog().get().value());
                var links = dialog.common().body().stream().filter(PlainMessage.class::isInstance)
                        .map(PlainMessage.class::cast).map(PlainMessage::contents)
                        .filter(text -> text.getStyle().getClickEvent() instanceof ClickEvent.Custom).toList();
                var targets = links.stream().map(GuidebookNavigationTest::target).toList();
                assertTrue(targets.contains(ROOT), id.toString());
                if (id.equals(chapter.id())) assertTrue(targets.containsAll(chapter.sections()));
                else assertTrue(targets.contains(chapter.id()), id.toString());
                for (var link : links) {
                    assertTrue(link.getStyle().isUnderlined());
                    Identifier destination = target(link);
                    if (!destination.equals(ROOT)) assertNotNull(manager.getPage(destination, RegistryAccess.EMPTY), destination.toString());
                }
                assertEquals(1, dialog.actions().size());
                assertEquals(1, page.createDialog().get().value() instanceof MultiActionDialog reopened ? reopened.actions().size() : -1,
                        "Opening again must not accumulate back buttons");
                count++;
            }
        }
        assertEquals(24, count);
    }

    @Test void nextAndPreviousWalkAllPagesExactlyOnceInBothDirections() {
        var expected = DefaultBookPages.CHAPTERS.stream()
                .flatMap(chapter -> Stream.concat(Stream.of(chapter.id()), chapter.sections().stream())).toList();
        var visited = new HashSet<Identifier>();
        Identifier current = expected.getFirst();
        Identifier previous = null;
        while (current != null) {
            assertTrue(visited.add(current), "Navigation cycle at " + current);
            var dialog = roundTrip(manager.getPage(current, RegistryAccess.EMPTY).createDialog().get().value());
            assertEquals(previous, navigation(dialog, "previous"));
            previous = current;
            current = navigation(dialog, "next");
        }
        assertEquals(new HashSet<>(expected), visited);
        int count = 0;
        for (current = expected.getLast(); current != null; count++) {
            var dialog = roundTrip(manager.getPage(current, RegistryAccess.EMPTY).createDialog().get().value());
            current = navigation(dialog, "previous");
            assertTrue(count < expected.size(), "Previous links must terminate");
        }
        assertEquals(expected.size(), count);
    }

    @Test void oldPageIdsRemainAddressableAndAddonPagesStayVisibleByDefault() {
        assertNotNull(manager.getPage(DefaultBookPages.BASIC_BEVERAGE, RegistryAccess.EMPTY));
        assertNotNull(manager.getPage(DefaultBookPages.OTHER_COMPAT, RegistryAccess.EMPTY));
        assertFalse(manager.getRootPages("reverie_dreams", RegistryAccess.EMPTY).contains(DefaultBookPages.BASIC_BEVERAGE));
        var addon = Identifier.parse("test:page/addon");
        manager.register(addon, access -> BookPage.builder(access).key(addon)
                .common(common -> common.title("Addon").addTextBody("Text")));
        assertEquals(List.of(addon), manager.getRootPages("test", RegistryAccess.EMPTY));
        assertEquals(9, manager.getRootPages("reverie_dreams", RegistryAccess.EMPTY).size());
    }

    @Test void quickStartLinksToPlayerGoalsAndPermissionsLinksToOriginalSources() {
        var start = roundTrip(manager.getPage(DefaultBookPages.BASIC_GETTING_STARTED, RegistryAccess.EMPTY).createDialog().get().value());
        var shortcuts = start.common().body().stream().filter(PlainMessage.class::isInstance)
                .map(PlainMessage.class::cast).map(PlainMessage::contents)
                .filter(text -> text.getContents() instanceof TranslatableContents contents
                        && contents.getKey().equals("guidebook.reverie_dreams.read"))
                .map(GuidebookNavigationTest::target).toList();
        assertEquals(List.of(DefaultBookPages.BASIC_GENSOKYO_ALTAR, DefaultBookPages.BASIC_ROLE_AND_PARTNER,
                DefaultBookPages.BASIC_TOUHOU_MYSTIA, DefaultBookPages.RESTAURANT, DefaultBookPages.EXPLORATION,
                DefaultBookPages.TROUBLESHOOTING), shortcuts);

        var authorized = roundTrip(manager.getPage(DefaultBookPages.AUTHORIZED, RegistryAccess.EMPTY).createDialog().get().value());
        var urls = authorized.common().body().stream().filter(PlainMessage.class::isInstance)
                .map(PlainMessage.class::cast).map(PlainMessage::contents).map(text -> text.getStyle().getClickEvent())
                .filter(ClickEvent.OpenUrl.class::isInstance).map(ClickEvent.OpenUrl.class::cast)
                .map(event -> event.uri().toString()).toList();
        assertEquals(List.of("https://space.bilibili.com/7039204/", "https://www.bilibili.com/video/BV1adYF66Ehj/"), urls);
    }

    private static Identifier navigation(MultiActionDialog dialog, String direction) {
        return dialog.common().body().stream().filter(PlainMessage.class::isInstance).map(PlainMessage.class::cast)
                .map(PlainMessage::contents)
                .filter(text -> text.getContents() instanceof TranslatableContents contents
                        && contents.getKey().equals("guidebook.reverie_dreams." + direction))
                .map(GuidebookNavigationTest::target).findFirst().orElse(null);
    }

    private static Identifier target(Component text) {
        var click = assertInstanceOf(ClickEvent.Custom.class, text.getStyle().getClickEvent());
        assertEquals(Identifier.parse("reverie_dreams:page/goto"), click.id());
        var payload = assertInstanceOf(CompoundTag.class, click.payload().orElseThrow());
        return Identifier.parse(payload.getString("id").orElseThrow());
    }

    private static MultiActionDialog roundTrip(Dialog dialog) {
        var encoded = Dialog.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, dialog).getOrThrow();
        return assertInstanceOf(MultiActionDialog.class, Dialog.DIRECT_CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }
}
