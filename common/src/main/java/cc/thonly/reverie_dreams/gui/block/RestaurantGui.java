package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RestaurantBlockEntity.OpeningIssue;
import cc.thonly.reverie_dreams.entity.ai.work.WorkWorld;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import cc.thonly.reverie_dreams.restaurant.RestaurantService;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.*;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;

import java.util.*;

/**
 * Ghost catalogue: clicks change IDs, never take items from either inventory.
 */
public final class RestaurantGui extends SimpleGui {
    private final RestaurantBlockEntity facility;
    private boolean drinks, selected;
    private int page;
    private String query = "";
    private Component bellStatus;
    private RestaurantBlockEntity displayedBook;
    private int displayedRadius;

    public RestaurantGui(ServerPlayer player, RestaurantBlockEntity facility) {
        super(MenuType.GENERIC_9x6, player, false);
        this.facility = facility;
        setLockPlayerInventory(true);
        if (!facility.isBell())
            setPanelTitle("\uE000", text("menu_settings"));
        refresh();
    }

    private void setPanelTitle(String glyph, Component title) {
        setTitle(Component.empty()
                .append(Component.translatable("space.-8"))
                .append(Component.literal(glyph).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                        .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                .append(Component.translatable("space.-168"))
                .append(title.copy().withStyle(style -> style.withColor(0x4C2B2A))));
    }

    private Component text(String key, Object... args) {
        return Component.translatable("restaurant.reverie_dreams." + key, args);
    }

    private boolean allowed() {
        return player.isAlive() && player.level() == facility.getLevel() && !facility.isRemoved() && facility.canEdit(player)
                && player.distanceToSqr(facility.getBlockPos().getCenter()) <= 64 && player.level().hasChunkAt(facility.getBlockPos())
                && player.level().getBlockEntity(facility.getBlockPos()) == facility;
    }

    private void button(int slot, Item icon, String name, Runnable action) {
        button(slot, icon, name, false, action);
    }

    private void button(int slot, Item icon, String name, boolean highlighted, Runnable action) {
        var element = new GuiElementBuilder(icon).setName(text(name));
        if (highlighted) {
            element.glow();
        }
        button(slot, element, action);
    }

    private void button(int slot, GuiElementBuilder element, Runnable action) {
        setSlot(slot, element.setCallback((a, b, c, d) -> {
            if (allowed()) {
                action.run();
                refresh();
            }
        }));
    }

    private void refresh() {
        for (int i = 0; i < 54; i++)
            clearSlot(i);
        if (facility.isBell()) {
            refreshBell();
            return;
        } else {
            var items = BuiltInRegistries.ITEM.stream().filter(i -> RestaurantService.valid(BuiltInRegistries.ITEM.getKey(i), drinks))
                    .filter(i -> !selected || facility.menu().contains(drinks, BuiltInRegistries.ITEM.getKey(i)))
                    .filter(i -> query.isBlank() || BuiltInRegistries.ITEM.getKey(i).toString().contains(query.toLowerCase(Locale.ROOT))
                            || i.getDefaultInstance().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                    .sorted(Comparator.comparing(i -> BuiltInRegistries.ITEM.getKey(i).toString())).toList();
            int max = Math.max(0, (items.size() - 1) / 36);
            page = Math.clamp(page, 0, max);
            for (int i = 0; i < 36 && page * 36 + i < items.size(); i++) {
                var item = items.get(page * 36 + i);
                var id = BuiltInRegistries.ITEM.getKey(item);
                boolean beverage = drinks;
                var icon = new GuiElementBuilder(item).addLoreLine(text(facility.menu().contains(drinks, id) ? "remove" : "add"));
                if (facility.menu().contains(drinks, id))
                    icon.glow();
                icon.setCallback((a, b, c, d) -> {
                    if (allowed() && RestaurantService.valid(id, beverage)) {
                        if (!facility.menu().toggle(beverage, id))
                            player.sendSystemMessage(text("menu_limit"));
                        facility.menuChanged();
                        refresh();
                    }
                });
                setSlot(i, icon);
            }
            button(36, Items.COOKED_BEEF, "foods", !drinks, () -> {
                drinks = false;
                page = 0;
            });
            button(37, Items.POTION, "drinks", drinks, () -> {
                drinks = true;
                page = 0;
            });
            button(38, Items.WRITABLE_BOOK, selected ? "all" : "listed", selected, () -> {
                selected = !selected;
                page = 0;
            });
            button(40, Items.SPYGLASS, "search", !query.isBlank(), this::search);
            button(45, RDGuiPlaceholderItems.PREV.value(), "previous", () -> page--);
            button(53, RDGuiPlaceholderItems.NEXT.value(), "next", () -> page++);
            setSlot(49, new GuiElementBuilder(Items.BOOK).setName(text("page", page + 1, max + 1))
                    .addLoreLine(text("menu_counts", facility.menu().entries(false).size(), facility.menu().entries(true).size())));
        }
        setSlot(51, new GuiElementBuilder(Items.PAPER).setName(text(facility.open() ? "opened" : "closed"))
                .addLoreLine(text("help")));
    }

    private void refreshBell() {
        var linkedBook = facility.linked();
        boolean open = facility.open();
        var books = WorkWorld.blockEntities((ServerLevel) player.level(), facility.area()).stream()
                .filter(b -> b instanceof RestaurantBlockEntity r && !r.isBell() && r.canEdit(player))
                .map(b -> (RestaurantBlockEntity) b)
                .sorted(Comparator.comparing((RestaurantBlockEntity book) -> book != linkedBook)
                        .thenComparingDouble(book -> book.getBlockPos().distSqr(facility.getBlockPos())))
                .limit(36).toList();
        for (int slot = 0; slot < books.size(); slot++) {
            var book = books.get(slot);
            boolean bound = book == linkedBook && book.linked() == facility;
            var icon = new GuiElementBuilder(bound ? Items.ENCHANTED_BOOK : Items.WRITABLE_BOOK)
                    .setName(text(bound ? "bound_menu" : "bind_menu", book.getBlockPos().toShortString()))
                    .addLoreLine(text("menu_counts", book.menu().entries(false).size(), book.menu().entries(true).size()));
            if (open)
                icon.addLoreLine(text("settings_locked"));
            button(slot, icon, () -> {
                if (facility.open()) {
                    player.sendSystemMessage(text("settings_locked"));
                } else if ((facility.linked() != book || book.linked() != facility) && !facility.bind(book, player)) {
                    player.sendSystemMessage(text("bind_failed"));
                }
            });
        }

        var issue = open ? null : facility.openingIssue();
        var status = bellStatus(open, issue);
        var power = new GuiElementBuilder(Items.BELL)
                .setName(text(open ? "close" : "open"))
                .addLoreLine(issue == null ? status : issue.hint());
        if (open)
            power.glow();
        button(49, power, () -> facility.toggleBusiness(player));

        radiusButton(45, "radius_less", -1);
        radiusButton(47, "radius_more", 1);
        var radius = new GuiElementBuilder(Items.COMPASS)
                .setCount(facility.radius()).setName(text("radius", facility.radius()));
        if (open)
            radius.addLoreLine(text("settings_locked"));
        setSlot(46, radius);

        var indicator = new GuiElementBuilder(open ? Items.LANTERN : issue == null ? Items.SOUL_LANTERN : Items.BARRIER)
                .setName(status).addLoreLine(issue == null ? text("help") : issue.hint());
        setSlot(52, indicator);
        displayedBook = linkedBook;
        displayedRadius = facility.radius();
        if (!status.equals(bellStatus)) {
            bellStatus = status;
            var coloredStatus = status.copy().withStyle(style -> style.withColor(
                    open ? 0x365332 : issue == null ? 0x4C2B2A : 0x8B302D));
            setPanelTitle("\uE001", text("bell_status", coloredStatus));
        }
    }

    private Component bellStatus(boolean open, OpeningIssue issue) {
        if (open)
            return text("opened");
        return issue == null ? text("ready_to_open") : issue.label();
    }

    private void radiusButton(int slot, String name, int delta) {
        var element = new GuiElementBuilder(RDGuiPlaceholderItems.EMPTY_ITEM.value()).setName(text(name));
        button(slot, element, () -> {
            if (facility.open())
                player.sendSystemMessage(text("settings_locked"));
            else
                facility.radius(facility.radius() + delta);
        });
    }

    private void search() {
        var parent = this;
        var gui = new AnvilInputGui(player, false) {
            private boolean returnQueued;

            private void returnToParent() {
                if (returnQueued)
                    return;
                returnQueued = true;
                var input = getInput().strip();
                // The close packet still resets containerMenu after onPlayerClose returns.
                DelayedTask.create(player.level().getServer(), 2, () -> {
                    if (player.hasDisconnected() || !parent.allowed() || player.containerMenu != player.inventoryMenu)
                        return;
                    parent.query = input;
                    parent.page = 0;
                    parent.refresh();
                    parent.open();
                });
            }

            @Override
            public void onPlayerClose(boolean success) {
                super.onPlayerClose(success);
                if (success)
                    returnToParent();
            }

            @Override
            public void onManualClose() {
                super.onManualClose();
                returnToParent();
            }

            @Override
            public void onTick() {
                super.onTick();
                if (!parent.allowed())
                    close();
            }
        };
        gui.setLockPlayerInventory(true);
        gui.setTitle(text("search"));
        gui.setDefaultInputValue(query);
        gui.setSlot(2, new GuiElementBuilder(Items.PAPER).setName(text("apply")).setCallback(() -> {
            SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);
            gui.close();
        }));
        gui.open();
    }

    @Override
    public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
        if (action == ContainerInput.PICKUP && element != null && !element.getItemStack().isEmpty() && allowed()) {
            SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);
        }
        return super.onClick(index, type, action, element);
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!allowed()) {
            close();
        } else if (facility.isBell() && player.tickCount % 20 == 0) {
            boolean open = facility.open();
            if (facility.linked() != displayedBook || facility.radius() != displayedRadius
                    || !bellStatus(open, open ? null : facility.openingIssue()).equals(bellStatus))
                refresh();
        }
    }
}
