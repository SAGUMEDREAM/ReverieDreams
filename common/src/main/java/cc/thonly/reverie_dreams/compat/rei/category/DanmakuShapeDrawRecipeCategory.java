package cc.thonly.reverie_dreams.compat.rei.category;

import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
import cc.thonly.reverie_dreams.compat.rei.display.DanmakuShapeDrawDisplay;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.item.REIItemUtils;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DanmakuShapeDrawRecipeCategory implements DisplayCategory<DanmakuShapeDrawDisplay> {
    @Override
    public List<Widget> setupDisplay(DanmakuShapeDrawDisplay display, Rectangle bounds) {
        int startX = bounds.getX();
        int startY = bounds.getY();

        int slotSize = 18;

        int gridW = 6 * slotSize;
        int gridH = 6 * slotSize;

        int width = gridW + 60;  // 左工具 + 右输出空间
        int height = gridH + 20;

        int xOffset = startX + (bounds.getWidth() - width) / 2;
        int yOffset = startY + (bounds.getHeight() - height) / 2;

        List<Widget> widgets = new java.util.ArrayList<>();

        widgets.add(Widgets.createRecipeBase(bounds));

        int gridStartX = xOffset + 30;
        int gridStartY = yOffset + 10;

        var shape = display.getRecipe().getShape();

        // ===== 6x6 网格 =====
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {

                boolean state = false;
                if (y < shape.size() && x < shape.get(y).size()) {
                    state = shape.get(y).get(x);
                }
                widgets.add(
                        Widgets.createSlot(new Rectangle(
                                        gridStartX + x * slotSize,
                                        gridStartY + y * slotSize,
                                        slotSize,
                                        slotSize))
                                .entries(state ? REIItemUtils.getItem(RDGuiItems.ENABLE) : REIItemUtils.getItem(RDGuiItems.DISABLE))
                                .markInput()
                );
            }
        }

        // ===== 左边：工具 =====
        widgets.add(
                Widgets.createSlot(new Rectangle(
                                gridStartX - 24,
                                gridStartY + gridH / 2 - slotSize / 2,
                                slotSize,
                                slotSize))
                        .entries(REIItemUtils.getItem(RDItems.DANMAKU_SHAPE_CREATOR))
                        .markInput()
        );

        // ===== 右边：输出 =====
        widgets.add(
                Widgets.createSlot(new Rectangle(
                                gridStartX + gridW + 8,
                                gridStartY + gridH / 2 - slotSize / 2,
                                slotSize,
                                slotSize))
                        .entries(display.getOutputEntries().getFirst())
                        .markOutput()
        );

        return widgets;
    }

    @Override
    public int getDisplayWidth(DanmakuShapeDrawDisplay display) {
        return 6 * 18 + 60; // ≈148
    }

    @Override
    public int getDisplayHeight() {
        return 6 * 18 + 20; // ≈128
    }

    @Override
    public CategoryIdentifier<? extends DanmakuShapeDrawDisplay> getCategoryIdentifier() {
        return REICategoryIdentifiers.DANMAKU_SHAPE_DRAW;
    }

    @Override
    public Component getTitle() {
        return RDItems.DANMAKU_SHAPE_CREATOR.asItem().getName(RDItems.DANMAKU_SHAPE_CREATOR.asItem().getDefaultInstance());
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.ofItemHolder(RDItems.DANMAKU_SHAPE_CREATOR);
    }
}
