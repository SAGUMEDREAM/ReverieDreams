package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.BookPageManager;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.page.BookPage;
import cc.thonly.reverie_dreams.server.page.BookPageBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

@Slf4j
public class DefaultBookPages {
    private static boolean INIT = false;
    public static final Identifier BASIC_GETTING_STARTED = key("basic/getting_started");
    public static final Identifier BASIC_GENSOKYO_ALTAR = key("basic/gensokyo_altar");
    public static final Identifier BASIC_DANMAKU_TUTORIAL = key("basic/danmaku_tutorial");
    public static final Identifier BASIC_FUMO_TUTORIAL = key("basic/fumo_tutorial");
    public static final Identifier BASIC_ROLE_AND_PARTNER = key("basic/role_and_partner");
    public static final Identifier BASIC_TOUHOU_MYSTIA = key("basic/touhou_mystia");
    public static final Identifier OTHER_COMPAT = key("other/compat");

    public static synchronized void initialize() {
        if (INIT) {
            return;
        }
        BookPageManager.addInitializer(DefaultBookPages::bootstrap);
        INIT = true;
    }

    private static void bootstrap(BookPageManager manager) {
        Identifier rootId = ReverieDreams.id("root/reverie_dreams");
        manager.register(BASIC_GETTING_STARTED, registryAccess -> {
            BookPageBuilder builder = BookPage.builder(registryAccess);
            builder.key(BASIC_GETTING_STARTED);
            builder.common(common -> {
                common.title(builder.getTitleKey(BASIC_GETTING_STARTED));
                common.addTextBody(builder.getContentKey(BASIC_GETTING_STARTED));
            });
            builder.prev(rootId);
            return builder;
        });
        manager.register(BASIC_GENSOKYO_ALTAR, registryAccess -> {
            BookPageBuilder builder = BookPage.builder(registryAccess);
            builder.key(BASIC_GENSOKYO_ALTAR);
            builder.common(common -> {
                common.title(builder.getTitleKey(BASIC_GENSOKYO_ALTAR));
                common.addTextBody(builder.getContentKey(BASIC_GENSOKYO_ALTAR));
            });
            builder.prev(rootId);
            return builder;
        });
        manager.register(BASIC_DANMAKU_TUTORIAL, registryAccess -> {
            BookPageBuilder builder = BookPage.builder(registryAccess);
            builder.key(BASIC_DANMAKU_TUTORIAL);
            builder.common(common -> {
                common.title(builder.getTitleKey(BASIC_DANMAKU_TUTORIAL));
                common.addTextBody(builder.getContentKey(BASIC_DANMAKU_TUTORIAL));
            });
            builder.prev(rootId);
            return builder;
        });
        manager.register(BASIC_FUMO_TUTORIAL, registryAccess -> {
            BookPageBuilder builder = BookPage.builder(registryAccess);
            builder.key(BASIC_FUMO_TUTORIAL);
            builder.common(common -> {
                common.title(builder.getTitleKey(BASIC_FUMO_TUTORIAL));
                common.addTextBody(builder.getContentKey(BASIC_FUMO_TUTORIAL));
            });
            builder.prev(rootId);
            return builder;
        });
        manager.register(BASIC_ROLE_AND_PARTNER, registryAccess -> {
            BookPageBuilder builder = BookPage.builder(registryAccess);
            builder.key(BASIC_ROLE_AND_PARTNER);
            builder.common(common -> {
                common.title(builder.getTitleKey(BASIC_ROLE_AND_PARTNER));
                common.addTextBody(builder.getContentKey(BASIC_ROLE_AND_PARTNER));
            });
            builder.prev(rootId);
            return builder;
        });
        manager.register(BASIC_TOUHOU_MYSTIA, registryAccess -> {
            BookPageBuilder builder = BookPage.builder(registryAccess);
            builder.key(BASIC_TOUHOU_MYSTIA);
            builder.common(common -> {
                common.title(builder.getTitleKey(BASIC_TOUHOU_MYSTIA));
                common.addTextBody(builder.getContentKey(BASIC_TOUHOU_MYSTIA));
            });
            builder.prev(rootId);
            return builder;
        });
        manager.register(OTHER_COMPAT, registryAccess -> {
            BookPageBuilder builder = BookPage.builder(registryAccess);
            builder.key(OTHER_COMPAT);
            builder.common(common -> {
                common.title(builder.getTitleKey(OTHER_COMPAT));
                common.addTextBody(builder.getContentKey(OTHER_COMPAT));
            });
            builder.prev(rootId);
            return builder;
        });

        manager.bindItem(BASIC_GETTING_STARTED, new ItemStackTemplate(Items.PLAYER_HEAD));
        manager.bindItem(BASIC_GENSOKYO_ALTAR, new ItemStackTemplate(RDBlocks.GENSOKYO_ALTAR.asItem()));
        manager.bindItem(BASIC_DANMAKU_TUTORIAL, new ItemStackTemplate(RDItems.DANMAKU.asItem()));
        manager.bindItem(BASIC_FUMO_TUTORIAL, new ItemStackTemplate(RDItems.FUMO_ICON.asItem()));
        manager.bindItem(BASIC_ROLE_AND_PARTNER, RoleCards.KOUMAKYOU.getTemplate());
        manager.bindItem(BASIC_TOUHOU_MYSTIA, new ItemStackTemplate(RDItems.MYSTIA_ICON.asItem()));
        manager.bindItem(OTHER_COMPAT, new ItemStackTemplate(Items.COMMAND_BLOCK));
    }

    public static Identifier key(String name) {
        return ReverieDreams.id("page/" + name);
    }
}
