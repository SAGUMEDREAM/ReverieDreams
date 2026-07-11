package cc.thonly.reverie_dreams.gui.recipe;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.gui.BasePageGui;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

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

    public final List<GuiElementBuilder> recipeElements = new ArrayList<>();
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
                    this.setSlot(slot, new GuiElementBuilder().setItem(RDGuiItems.NEXT.value()).setCallback(this::next));
                }
                if (c.equalsIgnoreCase("P")) {
                    this.setSlot(slot,  new GuiElementBuilder().setItem(RDGuiItems.PREV.value()).setCallback(this::prev));
                }
            }
        }
    }

    public void clickIcon(int index, ClickType clickType, ContainerInput input, SlotBasedGui slotBasedGui) {
        int iconIndex = this.page * PER_PAGE_SIZE + index;
        if (RecipeTypeCategoryManager.CATEGORY_ENTRIES.size() > iconIndex) {
            RecipeTypeGuiInfo<? extends BasePageGui> info = RecipeTypeCategoryManager.CATEGORY_ENTRIES.get(iconIndex);
        }
    }

    public void next(int index, ClickType clickType, ContainerInput input, SlotBasedGui slotBasedGui) {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        if (this.page < getMaxPage()) {
            this.page++;
        }
    }

    public void prev(int index, ClickType clickType, ContainerInput input, SlotBasedGui slotBasedGui) {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
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
        this.setTitle(
                Component.empty()
                         .append(Component.translatable("space.-8"))
                         .append(Component.literal("\ub007")
                                          .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                                                .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                         .append(Component.translatable("space.-168"))
                         .append(Component.nullToEmpty("Recipes Manager" + " " + "(" + (this.page + 1) + "/" + (getMaxPage() + 1) + ")"))
        );
        int start = this.page * PER_PAGE_SIZE;

        for (int i = 0; i < PER_PAGE_SIZE; i++) {
            int slotIndex = i;
            int recipeIndex = start + i;

            if (recipeIndex < RecipeTypeCategoryManager.CATEGORY_ENTRIES.size()) {
                RecipeTypeGuiInfo<? extends BasePageGui> recipeTypeGuiInfo = RecipeTypeCategoryManager.CATEGORY_ENTRIES.get(recipeIndex + this.page * PER_PAGE_SIZE);
                GuiElementBuilder icon = new GuiElementBuilder()
                        .setItem(recipeTypeGuiInfo.getIcon().item().value())
                        .setItemName(Component.translatable(recipeTypeGuiInfo.getId().toLanguageKey()))
                        .setLore(List.of())
                        .setCallback((slot, click, input, basedGui) -> {
                            this.close();
                            SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                            recipeTypeGuiInfo.create(this.player, () -> new RecipeTypeCategoryGui(this.player, this.page));
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
