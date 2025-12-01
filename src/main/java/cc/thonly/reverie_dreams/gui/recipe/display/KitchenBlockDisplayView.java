package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.block.KitchenBlockType;
import cc.thonly.reverie_dreams.gui.PlayerHeadInfo;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Getter
@Slf4j
@ToString(callSuper = true)
public class KitchenBlockDisplayView extends SimpleGui implements DisplayView {
    public final RecipeEntryWrapper<KitchenRecipe> key2ValueEntry;
    public final ResourceLocation key;
    public final KitchenRecipe value;
    public final GuiElementBuilder back = new GuiElementBuilder().setItem(RDGuiItems.BACK).setSkullOwner(PlayerHeadInfo.GUI_ADD).setItemName(Component.nullToEmpty("Back")).setCallback(this::back);
    public final GuiOpeningPrevCallback prevGuiCallback;

    public KitchenBlockDisplayView(ServerPlayer player, RecipeEntryWrapper<KitchenRecipe> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
        super(MenuType.GENERIC_9x5, player, false);
        this.key2ValueEntry = key2ValueEntry;
        this.key = this.key2ValueEntry.getKey();
        this.value = this.key2ValueEntry.getValue();
        this.prevGuiCallback = prevGuiCallback;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(this.key2ValueEntry.getValue().getOutput().getItemStack().getHoverName());
        List<ItemStackWrapper> ingredients = this.value.getIngredients();
        List<ItemStackWrapper> inputs = new LinkedList<>(ingredients);
        Iterator<ItemStackWrapper> slotIterator = inputs.iterator();

        String[][] grid = this.getGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                String c = grid[row][col];
                int slot = row * 9 + col;
                if (c.equalsIgnoreCase("X")) {
                    GuiElementBuilder builder = new GuiElementBuilder(RDGuiItems.EMPTY_SLOT);
                    this.setSlot(slot, builder);
                }
                if (c.equalsIgnoreCase("T")) {
                    GuiElementBuilder builder = new GuiElementBuilder(RDGuiItems.PROGRESS_TO_RESULT);
                    this.setSlot(slot, builder);
                }
                if (c.equalsIgnoreCase("P")) {
                    Block block = KitchenBlockType.KITCHEN_TYPE_2_BLOCK.get(this.value.getType());
                    this.setSlot(slot, new GuiElementBuilder(block.asItem()));
                }
                if (c.equalsIgnoreCase("B")) {
                    this.setSlot(slot, this.back);
                }
                if (c.equalsIgnoreCase("W")) {
                    this.setSlot(slot, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE));
                }
                if (c.equalsIgnoreCase("I")) {
                    if (slotIterator.hasNext()) {
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

    public void back(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action) {
        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
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
                {"X", "I", "I", "I", "I", "I", "T", "O", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "P", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
        };
    }
}
