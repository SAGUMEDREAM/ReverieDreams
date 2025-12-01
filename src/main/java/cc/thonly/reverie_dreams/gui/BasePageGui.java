package cc.thonly.reverie_dreams.gui;

import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.gui.recipe.GuiStackBuilder;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeGuiInfo;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeInfo;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
public class BasePageGui extends SimpleGui {
    public static final String[][] GRID = {
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"P", "W", "W", "W", "B", "W", "W", "W", "N"},
    };
    public static final int PER_PAGE_SIZE = 5 * 9;
    public final GuiElementBuilder back = new GuiElementBuilder(RDGuiItems.BACK).setItemName(Component.nullToEmpty("Back")).setCallback(this::back);
    public final GuiElementBuilder next = new GuiElementBuilder(RDGuiItems.NEXT).setItemName(Component.nullToEmpty("Next Page")).setCallback(this::next);
    public final GuiElementBuilder prev = new GuiElementBuilder(RDGuiItems.PREV).setItemName(Component.nullToEmpty("Prev Page")).setCallback(this::prev);
    public int page = 0;
    public final int maxSize;
    public final List<GuiElementBuilder> displayList = new LinkedList<>();
    public final RecipeTypeGuiInfo<? extends BasePageGui> recipeGuiInfo;
    public final RecipeTypeInfo recipeTypeInfo;
    public final List<RecipeEntryWrapper<?>> entries;
    public boolean updated = true;
    public GuiOpeningPrevCallback prevGuiCallback;

    public BasePageGui(ServerPlayer player, RecipeTypeGuiInfo<? extends BasePageGui> recipeGuiInfo, RecipeTypeInfo recipeTypeInfo, GuiOpeningPrevCallback prevGuiCallback) {
        super(MenuType.GENERIC_9x6, player, false);
        this.recipeGuiInfo = recipeGuiInfo;
        this.recipeTypeInfo = recipeTypeInfo;
        this.entries = new LinkedList<>();
        Map<ResourceLocation, ?> registryView = this.recipeTypeInfo.getRecipeType().getRegistryView();
        for (Map.Entry<ResourceLocation, ?> entry : registryView.entrySet()) {
            this.entries.add(new RecipeEntryWrapper<>(entry.getKey(), entry.getValue()));
        }
        this.maxSize = this.entries.size();
        this.prevGuiCallback = prevGuiCallback;
        this.init();
        this.setTitle(Component.empty().append(Component.translatable(this.recipeGuiInfo.getId().toLanguageKey())).append(Component.nullToEmpty(" (" + (this.page + 1) + "/" + (getMaxPage() + 1) + ")")));

    }

    public void init() {
        this.setTitle(Component.empty().append(Component.translatable(this.recipeGuiInfo.getId().toLanguageKey())).append(Component.nullToEmpty(" (" + (this.page + 1) + "/" + (getMaxPage() + 1) + ")")));
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String c = GRID[row][col];
                int slot = row * 9 + col;
                if (c.equalsIgnoreCase("X")) {
                    GuiElementBuilder builder = new GuiElementBuilder().setItem(Items.AIR);
                    builder.setCallback(this::clickIcon);
                    this.displayList.add(builder);
                    this.setSlot(slot, builder);
                }
                if (c.equalsIgnoreCase("B")) {
                    this.setSlot(slot, this.back);
                }
                if (c.equalsIgnoreCase("N")) {
                    this.setSlot(slot, this.next);
                }
                if (c.equalsIgnoreCase("P")) {
                    this.setSlot(slot, this.prev);
                }
                if (c.equalsIgnoreCase("W")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(RDGuiItems.EMPTY_SLOT));
                }
            }
        }
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        this.updated = true;
        return super.onAnyClick(index, type, action);
    }

    public void clickIcon(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        int iconIndex = this.page * PER_PAGE_SIZE + index;
        if (this.maxSize > iconIndex) {

        }
    }

    public void back(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        if (this.prevGuiCallback != null) {
            SimpleGui applyGui = this.prevGuiCallback.apply();
            applyGui.open();
        }
    }

    public void next(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        if (this.page < getMaxPage()) {
            this.page++;
            this.displayList.clear();
        }
    }

    public void prev(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);

        if (this.page > getMinPage()) {
            this.page--;
            this.displayList.clear();
        }
    }

    public int getMinPage() {
        return 0;
    }

    public int getMaxPage() {
        return Math.max(0, (this.maxSize - 1) / PER_PAGE_SIZE);
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!this.updated) return;
        this.updated = false;

        this.setTitle(Component.empty().append(Component.translatable(this.recipeGuiInfo.getId().toLanguageKey())).append(Component.nullToEmpty(" (" + (this.page + 1) + "/" + (getMaxPage() + 1) + ")")));
        int start = this.page * PER_PAGE_SIZE;

        for (int i = 0; i < PER_PAGE_SIZE; i++) {
            int slotIndex = i;
            int recipeIndex = start + i;

            if (recipeIndex < this.maxSize) {
                GuiStackBuilder stackGetter = this.recipeTypeInfo.getGetter();
                stackGetter.apply(this, slotIndex);
            } else {
                this.setSlot(getGridSlot(slotIndex), new GuiElementBuilder().setItem(Items.AIR));
            }
        }
    }

    public int getGridSlot(int index) {
        int counter = 0;
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                if (GRID[row][col].equalsIgnoreCase("X")) {
                    if (counter == index) {
                        return row * 9 + col;
                    }
                    counter++;
                }
            }
        }
        return -1;
    }
}
