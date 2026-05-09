package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.gui.PlayerHeadInfo;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Getter
@Slf4j
@ToString(callSuper = true)
public class DanmakuTableDisplayView extends SimpleGui implements DisplayView {
    public final RecipeEntryWrapper<DanmakuRecipe> key2ValueEntry;
    public final Identifier key;
    public final DanmakuRecipe value;
    public final GuiElementBuilder back = new GuiElementBuilder().setItem(RDGuiItems.BACK.asItem()).setProfileSkinTexture(PlayerHeadInfo.GUI_ADD).setItemName(Component.nullToEmpty("Back")).setCallback(this::back);
    public final GuiOpeningPrevCallback prevGuiCallback;

    public DanmakuTableDisplayView(ServerPlayer player, RecipeEntryWrapper<DanmakuRecipe> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
        super(MenuType.GENERIC_9x6, player, false);
        this.key2ValueEntry = key2ValueEntry;
        this.key = this.key2ValueEntry.getKey();
        this.value = this.key2ValueEntry.getValue();
        this.prevGuiCallback = prevGuiCallback;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(this.key2ValueEntry.getValue().getOutput().getItemStack().getHoverName());
        List<ItemStackWrapper> inputs = new LinkedList<>();
        inputs.add(this.value.getDye());
        inputs.add(this.value.getCore());
        inputs.add(this.value.getPower());
        inputs.add(this.value.getPoint());
        inputs.add(this.value.getMaterial());
        Iterator<ItemStackWrapper> slotIterator = inputs.iterator();

        String[][] grid = this.getGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                String c = grid[row][col];
                int slot = row * 9 + col;
                if (c.equalsIgnoreCase("X")) {
                    GuiElementBuilder builder = new GuiElementBuilder(RDGuiItems.EMPTY_SLOT.asItem());
                    this.setSlot(slot, builder);
                }
                if (c.equalsIgnoreCase("T")) {
                    GuiElementBuilder builder = new GuiElementBuilder(RDGuiItems.PROGRESS_TO_RESULT.asItem());
                    this.setSlot(slot, builder);
                }
                if (c.equalsIgnoreCase("B")) {
                    this.setSlot(slot, this.back);
                }
                if (c.equalsIgnoreCase("W")) {
                    this.setSlot(slot, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE));
                }
                if (c.equalsIgnoreCase("I")) {
                    if(slotIterator.hasNext()) {
                        ItemStackWrapper next = slotIterator.next();
                        this.setSlot(slot, this.getGuiElementBuilder(next));
                    }
                }
                if (c.equalsIgnoreCase("O")) {
                    ItemStackWrapper output = this.value.getOutput();
                    this.setSlot(slot, this.getGuiElementBuilder(output));
                }
            }
        }
    }

    public void back(int index, ClickType clickType, ContainerInput input, SlotBasedGui slotBasedGui) {
        this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        this.close();
        if (this.prevGuiCallback != null) {
            SimpleGui applyGui = this.prevGuiCallback.apply();
            if (applyGui != null) {
                applyGui.open();
            }
        }
    }

    @Override
    public String[][] getGrid() {
        return new String[][]{
                {"B", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "I", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "I", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "I", "X", "T", "X", "O", "X", "X"},
                {"X", "X", "I", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "I", "X", "X", "X", "X", "X", "X"},
        };
    }
}
