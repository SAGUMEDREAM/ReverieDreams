package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.recipe.view.RecipeKeyEntry;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;

@Getter
@Slf4j
@ToString(callSuper = true)
public class StrengthTableDisplayView extends SimpleGui implements DisplayView {
    public static final String[][] GRID = new String[][]{
            {"X", "X", "I", "I", "X", "X", "I", "X", "X"}
    };
    public final RecipeKeyEntry<StrengthTableRecipe> key2ValueEntry;
    public final Identifier key;
    public final StrengthTableRecipe value;
    public final GuiOpeningPrevCallback prevGuiCallback;

    public StrengthTableDisplayView(ServerPlayer player, RecipeKeyEntry<StrengthTableRecipe> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
        super(MenuType.GENERIC_9x1, player, false);
        this.key2ValueEntry = key2ValueEntry;
        this.key = this.key2ValueEntry.getKey();
        this.value = this.key2ValueEntry.getValue();
        this.prevGuiCallback = prevGuiCallback;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(
                Component.empty()
                         .append(Component.translatable("space.-8"))
                         .append(Component.literal("\ub004")
                                          .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                                                .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                         .append(Component.translatable("space.-168"))
                         .append(this.key2ValueEntry.getValue().getOutput().build().getHoverName())
        );
        this.setSlot(2, new GuiElementBuilder(this.getValue().getMainItem().build()));
        this.setSlot(3, new GuiElementBuilder(this.getValue().getOffItem().build()));
        this.setSlot(6, new GuiElementBuilder(this.getValue().getOutput().build()));
        this.setSlot(8, new GuiElementBuilder(RDGuiItems.CLOSE.asItem()).setCallback(() -> {
            SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
            this.prevGuiCallback.apply().open();
        }));
    }

    @Override
    public void onManualClose() {
        super.onManualClose();
        this.back(0, null, null, null);
    }

    public void back(int index, ClickType clickType, ContainerInput input, SlotBasedGui slotBasedGui) {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        this.close();
        if (this.prevGuiCallback != null) {
            SimpleGui applyGui = this.prevGuiCallback.apply();
            applyGui.open();
        }
    }
}
