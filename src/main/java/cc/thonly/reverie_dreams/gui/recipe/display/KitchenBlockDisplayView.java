package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.mystias_izakaya.block.entity.KitchenwareBlockEntity;
import cc.thonly.mystias_izakaya.recipe.entry.KitchenRecipe;
import cc.thonly.mystias_izakaya.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.gui.PlayerHeadInfo;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.recipe.RecipeKey2ValueEntry;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.slot.ItemStackRecipeWrapper;
import com.google.common.collect.BiMap;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Getter
@Slf4j
@ToString(callSuper = true)
public class KitchenBlockDisplayView extends SimpleGui implements DisplayView {
    public final RecipeKey2ValueEntry<KitchenRecipe> key2ValueEntry;
    public final Identifier key;
    public final KitchenRecipe value;
    public final GuiElementBuilder back = new GuiElementBuilder().setItem(ModGuiItems.BACK).setSkullOwner(PlayerHeadInfo.GUI_ADD).setItemName(Text.of("Back")).setCallback(this::back);
    public final GuiOpeningPrevCallback prevGuiCallback;

    public KitchenBlockDisplayView(ServerPlayerEntity player, RecipeKey2ValueEntry<KitchenRecipe> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
        super(ScreenHandlerType.GENERIC_9X5, player, false);
        this.key2ValueEntry = key2ValueEntry;
        this.key = this.key2ValueEntry.getKey();
        this.value = this.key2ValueEntry.getValue();
        this.prevGuiCallback = prevGuiCallback;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(this.key2ValueEntry.getValue().getOutput().getItemStack().getName());
        List<ItemStackRecipeWrapper> ingredients = this.value.getIngredients();
        List<ItemStackRecipeWrapper> inputs = new LinkedList<>(ingredients);
        Iterator<ItemStackRecipeWrapper> slotIterator = inputs.iterator();

        String[][] grid = this.getGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                String c = grid[row][col];
                int slot = row * 9 + col;
                if (c.equalsIgnoreCase("X")) {
                    GuiElementBuilder builder = new GuiElementBuilder().setItem(ModGuiItems.EMPTY_SLOT);
                    this.setSlot(slot, builder);
                }
                if (c.equalsIgnoreCase("T")) {
                    GuiElementBuilder builder = new GuiElementBuilder().setItem(ModGuiItems.PROGRESS_TO_RESULT);
                    this.setSlot(slot, builder);
                }
                if (c.equalsIgnoreCase("P")) {
                    BiMap<KitchenRecipeType.KitchenType, Block> inverse = KitchenwareBlockEntity.BLOCK_2_KITCHEN_TYPE.inverse();
                    Block block = inverse.get(this.value.getType());
                    this.setSlot(slot, new GuiElementBuilder().setItem(block.asItem()));
                }
                if (c.equalsIgnoreCase("B")) {
                    this.setSlot(slot, this.back);
                }
                if (c.equalsIgnoreCase("W")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(Items.WHITE_STAINED_GLASS_PANE));
                }
                if (c.equalsIgnoreCase("I")) {
                    if(slotIterator.hasNext()) {
                        ItemStackRecipeWrapper next = slotIterator.next();
                        this.setSlot(slot, this.getGuiElementBuilder(next));
                    }
                }
                if (c.equalsIgnoreCase("O")) {
                    ItemStackRecipeWrapper output = this.value.getOutput();
                    this.setSlot(slot, this.getGuiElementBuilder(output));
                }
            }
        }
    }

    public void back(int index, ClickType clickType, SlotActionType action) {
        this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
        this.close();
        if (this.prevGuiCallback != null) {
            SimpleGui applyGui = this.prevGuiCallback.apply();
            applyGui.open();
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
