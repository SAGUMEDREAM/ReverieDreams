package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.gui.PlayerHeadInfo;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.recipe.view.RecipeKeyEntry;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Getter
@Slf4j
@ToString(callSuper = true)
public class BrewingBlockDisplayView extends SimpleGui implements DisplayView {
    public final RecipeKeyEntry<BrewingBarrelRecipe> key2ValueEntry;
    public final Identifier key;
    public final BrewingBarrelRecipe value;
    public final GuiElementBuilder back = new GuiElementBuilder()
            .setItem(RDGuiPlaceholderItems.CLOSE.asItem())
            .setProfileSkinTexture(PlayerHeadInfo.GUI_ADD)
            .setItemName(Component.nullToEmpty("Back"))
            .setCallback(this::back);
    public final GuiOpeningPrevCallback prevGuiCallback;

    public BrewingBlockDisplayView(
            ServerPlayer player,
            RecipeKeyEntry<BrewingBarrelRecipe> key2ValueEntry,
            GuiOpeningPrevCallback prevGuiCallback
    ) {
        super(MenuType.GENERIC_9x5, player, false);
        this.key2ValueEntry = key2ValueEntry;
        this.key = this.key2ValueEntry.getKey();
        this.value = this.key2ValueEntry.getValue();
        this.prevGuiCallback = prevGuiCallback;
        this.init();
    }

    @Override
    public void init() {
        this.setTitle(
                Component.empty().append(this.key2ValueEntry.getValue().getOutput().build().getHoverName())
        );

        List<IngredientStack> ingredients = this.value.getMaterials();
        List<IngredientStack> inputs = new LinkedList<>(ingredients);
        Iterator<IngredientStack> slotIterator = inputs.iterator();
        String[][] grid = this.getGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                int slot = row * 9 + col;
                this.setSlot(slot, new ItemStack(RDGuiPlaceholderItems.EMPTY_SLOT.asItem()));
            }
        }
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                String c = grid[row][col];
                int slot = row * 9 + col;

                if (c.equalsIgnoreCase("B")) {
                    this.setSlot(slot, this.back);
                }

                if (c.equalsIgnoreCase("I")) {
                    if (slotIterator.hasNext()) {
                        IngredientStack next = slotIterator.next();
                        this.setSlot(slot, this.getGuiElementBuilder(next));
                    } else {
                        this.setSlot(slot, new GuiElementBuilder(Items.AIR));
                    }
                }

                if (c.equalsIgnoreCase("T")) {
                    this.setSlot(
                            slot,
                            new GuiElementBuilder(
                                    RDGuiPlaceholderItems.PROGRESS_TO_RESULT.asItem()
                            )
                    );
                }

                if (c.equalsIgnoreCase("O")) {
                    IngredientStack output = this.value.getOutput();
                    this.setSlot(slot, this.getGuiElementBuilder(output));
                }
            }
        }
    }

    public void back(
            int index,
            ClickType clickType,
            ContainerInput input,
            SlotBasedGui slotBasedGui
    ) {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
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
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"I", "I", "I", "I", "I", "I", "I", "I", "I"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"T", "X", "O", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "B"},
        };
    }
}