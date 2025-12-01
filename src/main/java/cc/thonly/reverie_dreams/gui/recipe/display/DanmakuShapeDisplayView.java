package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.gui.PlayerHeadInfo;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Slf4j
@ToString(callSuper = true)
public class DanmakuShapeDisplayView extends SimpleGui implements DisplayView {
    public final RecipeEntryWrapper<DanmakuShapeDrawRecipe> key2ValueEntry;
    public final ResourceLocation key;
    public final DanmakuShapeDrawRecipe value;
    public final GuiElementBuilder back = new GuiElementBuilder().setItem(RDGuiItems.BACK).setSkullOwner(PlayerHeadInfo.GUI_ADD).setItemName(Component.nullToEmpty("Back")).setCallback(this::back);
    public final GuiOpeningPrevCallback prevGuiCallback;

    public DanmakuShapeDisplayView(ServerPlayer player, RecipeEntryWrapper<DanmakuShapeDrawRecipe> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
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
                    this.setSlot(counter, RDGuiItems.EMPTY_SLOT.getDefaultInstance());
                }
                if (Objects.equals(c, "X")) {
                    GuiElementBuilder builder = new GuiElementBuilder();
                    if (list.get(counter2)) {
                        builder.setItem(RDGuiItems.ENABLE);
                    } else {
                        builder.setItem(RDGuiItems.DISABLE);
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
                {"B", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"A", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"D", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"A", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"A", "A", "X", "X", "X", "X", "X", "X", "A"},
                {"A", "A", "X", "X", "X", "X", "X", "X", "A"},
        };
    }
}
