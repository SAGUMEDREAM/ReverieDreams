package cc.thonly.reverie_dreams.gui.recipe;

import cc.thonly.reverie_dreams.gui.BasePageGui;
import cc.thonly.reverie_dreams.gui.PlayerHeadInfo;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class RecipeTypeCategoryGui extends SimpleGui {
    public static final String[][] GRID = {
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"P", "W", "W", "W", "W", "W", "W", "W", "N"},
    };
    public static final int PER_PAGE_SIZE = 5 * 9;

    public final List<GuiElementBuilder> recipeElements = new LinkedList<>();
    public final GuiElementBuilder next = new GuiElementBuilder().setItem(Items.PLAYER_HEAD).setSkullOwner(PlayerHeadInfo.GUI_NEXT_PAGE).setItemName(Component.nullToEmpty("Next Page")).setCallback(this::next);
    public final GuiElementBuilder prev = new GuiElementBuilder().setItem(Items.PLAYER_HEAD).setSkullOwner(PlayerHeadInfo.GUI_PREVIOUS_PAGE).setItemName(Component.nullToEmpty("Prev Page")).setCallback(this::prev);
    public int page = 0;

    public RecipeTypeCategoryGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.init();
    }

    public RecipeTypeCategoryGui(ServerPlayer player, int page) {
        this(player);
        this.page = page;
    }

    public static RecipeTypeCategoryGui create(ServerPlayer player) {
        RecipeTypeCategoryGui recipeTypeCategoryGui = new RecipeTypeCategoryGui(player);
        recipeTypeCategoryGui.open();
        return recipeTypeCategoryGui;
    }

    public void init() {
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String c = GRID[row][col];
                int slot = row * 9 + col;
                if (c.equalsIgnoreCase("X")) {
                    GuiElementBuilder builder = new GuiElementBuilder().setItem(Items.AIR);
                    builder.setCallback(this::clickIcon);
                    this.recipeElements.add(builder);
                    this.setSlot(slot, builder);
                }
                if (c.equalsIgnoreCase("N")) {
                    this.setSlot(slot, this.next);
                }
                if (c.equalsIgnoreCase("P")) {
                    this.setSlot(slot, this.prev);
                }
                if (c.equalsIgnoreCase("W")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(Items.WHITE_STAINED_GLASS_PANE));
                }
            }
        }
    }

    public void clickIcon(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        int iconIndex = this.page * PER_PAGE_SIZE + index;
        if (RecipeTypeCategoryManager.CATEGORY_ENTRIES.size() > iconIndex) {
            RecipeTypeGuiInfo<? extends BasePageGui> info = RecipeTypeCategoryManager.CATEGORY_ENTRIES.get(iconIndex);
        }
    }

    public void next(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        if (this.page < getMaxPage()) {
            this.page++;
        }
    }

    public void prev(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);

        if (this.page > getMinPage()) {
            this.page--;
        }
    }

    public int getMinPage() {
        return 0;
    }

    public int getMaxPage() {
        return Math.max(0, (this.recipeElements.size() - 1) / PER_PAGE_SIZE);
    }

    @Override
    public void onTick() {
        super.onTick();
        this.setTitle(Component.nullToEmpty("Recipes Manager" + " " + "(" + (this.page + 1) + "/" + (getMaxPage() + 1) + ")"));

        int start = this.page * PER_PAGE_SIZE;

        for (int i = 0; i < PER_PAGE_SIZE; i++) {
            int slotIndex = i;
            int recipeIndex = start + i;

            if (recipeIndex < RecipeTypeCategoryManager.CATEGORY_ENTRIES.size()) {
                RecipeTypeGuiInfo<? extends BasePageGui> recipeTypeGuiInfo = RecipeTypeCategoryManager.CATEGORY_ENTRIES.get(recipeIndex + this.page * PER_PAGE_SIZE);
                GuiElementBuilder icon = new GuiElementBuilder()
                        .setItem(recipeTypeGuiInfo.getIcon().getItem())
                        .setItemName(Component.translatable(recipeTypeGuiInfo.getId().toLanguageKey()))
                        .setLore(List.of())
                        .setCallback((slot, click, action) -> {
                            this.close();
                            this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                            recipeTypeGuiInfo.create(player, () -> new RecipeTypeCategoryGui(player, this.page));
                        });
                this.setSlot(getGridSlot(slotIndex), icon);
            } else {
                this.setSlot(getGridSlot(slotIndex), new GuiElementBuilder().setItem(Items.AIR));
            }
        }
    }

    private int getGridSlot(int index) {
        int count = 0;
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                if (GRID[row][col].equalsIgnoreCase("X")) {
                    if (count == index) {
                        return row * 9 + col;
                    }
                    count++;
                }
            }
        }
        return -1;
    }

}
