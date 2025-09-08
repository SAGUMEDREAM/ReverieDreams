package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.gui.DanmakuShapeEditGui;
import cc.thonly.reverie_dreams.gui.PlayerHeadInfo;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

@Getter
@Slf4j
@ToString(callSuper = true)
public class DanmakuShapeDisplayView extends SimpleGui implements DisplayView {
    public final RecipeEntryWrapper<DanmakuShapeDrawRecipe> key2ValueEntry;
    public final Identifier key;
    public final DanmakuShapeDrawRecipe value;
    public final GuiElementBuilder back = new GuiElementBuilder().setItem(ModGuiItems.BACK).setSkullOwner(PlayerHeadInfo.GUI_ADD).setItemName(Text.of("Back")).setCallback(this::back);
    public final GuiOpeningPrevCallback prevGuiCallback;

    public DanmakuShapeDisplayView(ServerPlayerEntity player, RecipeEntryWrapper<DanmakuShapeDrawRecipe> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.key2ValueEntry = key2ValueEntry;
        this.key = this.key2ValueEntry.getKey();
        this.value = this.key2ValueEntry.getValue();
        this.prevGuiCallback = prevGuiCallback;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(this.key2ValueEntry.getValue().getOutput().getItemStack().getName());
        int counter = 0;
        int counter2 = 0;
        List<List<Boolean>> shape = this.value.getShape();
        List<Boolean> list = new ArrayList<>();
        for (List<Boolean> booleans : shape) {
            list.addAll(booleans);
        }
        for (int y = 0; y < this.getGrid().length; y++) {
            for (int x = 0; x < this.getGrid()[y].length; x++) {
                String c = this.getGrid()[y][x];
                if (Objects.equals(c, "A")) {
                    this.setSlot(counter, ModGuiItems.EMPTY_SLOT.getDefaultStack());
                }
                if (Objects.equals(c, "X")) {
                    GuiElementBuilder builder = new GuiElementBuilder();
                    if (list.get(counter2)) {
                        builder.setItem(ModGuiItems.ENABLE);
                    } else {
                        builder.setItem(ModGuiItems.DISABLE);
                    }
                    this.setSlot(counter, builder);
                    counter2++;
                }
                if (Objects.equals(c, "B")) {
                    this.setSlot(counter, this.back);
                }
                if (Objects.equals(c, "D")) {
                    this.setSlot(counter, new GuiElementBuilder(this.value.getOutput().getItemStack()));
                }
                counter++;
            }
        }
    }

    public void back(int index, ClickType clickType, SlotActionType action) {
        this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
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
                {"B", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"A", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"D", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"A", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"A", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"A", "A", "X", "X", "X", "X", "X", "X", "A"},
        };
    }
}
