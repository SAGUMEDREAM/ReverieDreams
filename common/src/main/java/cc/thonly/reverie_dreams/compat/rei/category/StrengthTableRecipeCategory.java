package cc.thonly.reverie_dreams.compat.rei.category;

import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
import cc.thonly.reverie_dreams.compat.rei.display.StrengthTableDisplay;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

import java.util.List;

public class StrengthTableRecipeCategory implements DisplayCategory<StrengthTableDisplay> {

    @Override
    public List<Widget> setupDisplay(StrengthTableDisplay display, Rectangle bounds) {
        int startX = bounds.getX();
        int startY = bounds.getY();

        int slotSize = 18;

        int width = 116;
        int height = 27;

        int xOffset = startX + (bounds.getWidth() - width) / 2;
        int yOffset = startY + (bounds.getHeight() - height) / 2;

        List<Widget> widgets = new java.util.ArrayList<>();

        widgets.add(Widgets.createRecipeBase(bounds));

        int centerY = yOffset + (height - slotSize) / 2;

        widgets.add(Widgets.createSlot(new Rectangle(
                        xOffset + 0,
                        centerY,
                        slotSize,
                        slotSize))
                .entries(display.getInputEntries().get(0))
                .markInput());

        widgets.add(Widgets.createSlot(new Rectangle(
                        xOffset + 18,
                        centerY,
                        slotSize,
                        slotSize))
                .entries(display.getInputEntries().get(1))
                .markInput());

        widgets.add(Widgets.createArrow(new Point(
                xOffset + 42,
                centerY + 1
        )));

        // ===== 输出 =====
        widgets.add(Widgets.createSlot(new Rectangle(
                        xOffset + 78,
                        centerY,
                        slotSize,
                        slotSize))
                .entries(display.getOutputEntries().get(0))
                .markOutput());

        return widgets;
    }

    @Override
    public int getDisplayWidth(StrengthTableDisplay display) {
        return 116 + 18;
    }

    @Override
    public int getDisplayHeight() {
        return 27;
    }

    @Override
    public CategoryIdentifier<? extends StrengthTableDisplay> getCategoryIdentifier() {
        return REICategoryIdentifiers.STRENGTH_TABLE;
    }

    @Override
    public Component getTitle() {
        return RDBlocks.STRENGTH_TABLE.asBlock().getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(RDBlocks.STRENGTH_TABLE);
    }
}
