package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ContainerInput;

@Getter
@Slf4j
@ToString(callSuper = true)
public class StrengthTableDisplayView extends AnvilInputGui implements DisplayView {
    public final RecipeEntryWrapper<StrengthTableRecipe> key2ValueEntry;
    public final Identifier key;
    public final StrengthTableRecipe value;
    public final GuiOpeningPrevCallback prevGuiCallback;

    public StrengthTableDisplayView(ServerPlayer player, RecipeEntryWrapper<StrengthTableRecipe> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
        super(player, false);
        this.key2ValueEntry = key2ValueEntry;
        this.key = this.key2ValueEntry.getKey();
        this.value = this.key2ValueEntry.getValue();
        this.prevGuiCallback = prevGuiCallback;
        this.init();
    }
    @Override
    public void init() {
        this.setSlot(0, new GuiElementBuilder().setItem(this.getValue().getMainItem().getItem()));
        this.setSlot(1, new GuiElementBuilder().setItem(this.getValue().getOffItem().getItem()));
        this.setSlot(2, new GuiElementBuilder().setItem(this.getValue().getOutput().getItem()));
    }

    @Override
    public void close() {
        super.close();
        this.back(0,null,null, null);
    }

    public void back(int index, ClickType clickType, ContainerInput input, SlotBasedGui slotBasedGui) {
        this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        this.close();
        if (this.prevGuiCallback != null) {
            SimpleGui applyGui = this.prevGuiCallback.apply();
            applyGui.open();
        }
    }
}
