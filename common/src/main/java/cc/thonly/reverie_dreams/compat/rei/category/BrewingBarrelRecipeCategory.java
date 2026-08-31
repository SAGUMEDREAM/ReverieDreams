package cc.thonly.reverie_dreams.compat.rei.category;

import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
import cc.thonly.reverie_dreams.compat.rei.display.BrewingBarrelDisplay;
import cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.util.item.REIItemUtils;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BrewingBarrelRecipeCategory implements DisplayCategory<BrewingBarrelDisplay> {

    @Override
    public List<Widget> setupDisplay(BrewingBarrelDisplay display, Rectangle bounds) {
        int startX = bounds.getX();
        int startY = bounds.getY();

        int slotSize = 18;
        int width = 18 * 11;
        int height = 27;

        int xOffset = startX + (bounds.getWidth() - width) / 2 + 4;
        int yOffset = startY + (bounds.getHeight() - height) / 2 + 4;

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        BrewingBarrelRecipe recipe = display.getRecipe();

        int startInputX = xOffset;
        int centerY = yOffset;

        // 输入
        for (int i = 0; i < 9; i++) {
            Rectangle rect = new Rectangle(
                    startInputX + i * slotSize - 9,
                    centerY,
                    slotSize,
                    slotSize
            );

            var slot = Widgets.createSlot(rect).markInput();

            if (i < recipe.getMaterials().size()) {
                slot.entries(
                        REIItemUtils.getItem(
                                recipe.getMaterials().get(i)
                        )
                );
            }

            widgets.add(slot);
        }

        // 箭头
        widgets.add(
                Widgets.createArrow(new Point(
                        startInputX + 9 * slotSize - 8,
                        centerY + (slotSize - 16) / 2
                ))
        );

        // 输出
        widgets.add(
                Widgets.createSlot(new Rectangle(
                                startInputX + 10 * slotSize,
                                centerY,
                                slotSize,
                                slotSize
                        ))
                        .entries(
                                REIItemUtils.getItem(recipe.getOutput())
                        )
                        .markOutput()
        );

        return widgets;
    }

    @Override
    public int getDisplayWidth(BrewingBarrelDisplay display) {
        return 18 * 11;
    }

    @Override
    public int getDisplayHeight() {
        return 27 + 9;
    }

    @Override
    public CategoryIdentifier<? extends BrewingBarrelDisplay> getCategoryIdentifier() {
        return REICategoryIdentifiers.BREWING_BARREL;
    }

    @Override
    public Component getTitle() {
        return RDBlocks.BREWING_BARREL.asBlock().getName();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.ofItemHolder(RDBlocks.BREWING_BARREL);
    }
}