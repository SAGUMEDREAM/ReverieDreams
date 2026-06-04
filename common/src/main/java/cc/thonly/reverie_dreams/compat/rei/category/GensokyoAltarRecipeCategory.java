//package cc.thonly.reverie_dreams.compat.rei.category;
//
//import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
//import cc.thonly.reverie_dreams.compat.rei.display.GensokyoAltarRecipeDisplay;
//import cc.thonly.reverie_dreams.gui.block.GensokyoAltarGui;
//import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
//import cc.thonly.reverie_dreams.util.item.REIItemUtils;
//import me.shedaniel.math.Point;
//import me.shedaniel.math.Rectangle;
//import me.shedaniel.rei.api.client.gui.Renderer;
//import me.shedaniel.rei.api.client.gui.widgets.Widget;
//import me.shedaniel.rei.api.client.gui.widgets.Widgets;
//import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
//import me.shedaniel.rei.api.common.category.CategoryIdentifier;
//import me.shedaniel.rei.api.common.util.EntryStacks;
//import net.minecraft.network.chat.Component;
//
//import java.util.List;
//
//public class GensokyoAltarRecipeCategory implements DisplayCategory<GensokyoAltarRecipeDisplay> {
//    @Override
//    public List<Widget> setupDisplay(GensokyoAltarRecipeDisplay display, Rectangle bounds) {
//        int startX = bounds.getX();
//        int startY = bounds.getY();
//
//        int slotSize = 18;
//
//        int width = 9 * 18;
//        int height = 5 * 18;
//
//        // ===== 居中整个UI =====
//        int xOffset = startX + (bounds.getWidth() - width) / 2;
//        int yOffset = startY + (bounds.getHeight() - height) / 2;
//
//        List<Widget> widgets = new java.util.ArrayList<>();
//        widgets.add(Widgets.createRecipeBase(bounds));
//
//        var recipe = display.getRecipe();
//
//        int invIndex = 0;
//
//        int coreX = 0;
//        int coreY = 0;
//
//        // 和 JEI 一致：整体往左偏移 2 格
//        int offsetX = -slotSize * 2;
//
//        // ===== GRID 布局 =====
//        for (int row = 0; row < GensokyoAltarGui.GRID.length; row++) {
//            for (int col = 0; col < GensokyoAltarGui.GRID[row].length; col++) {
//
//                String type = GensokyoAltarGui.GRID[row][col];
//
//                int x = xOffset + col * slotSize + offsetX;
//                int y = yOffset + row * slotSize;
//
//                // 空位
//                if (type.equals("X")) continue;
//
//                // ===== 普通输入 =====
//                if (type.equals("I")) {
//                    if (invIndex < recipe.getSlots().size()) {
//                        widgets.add(
//                                Widgets.createSlot(new Rectangle(x, y, slotSize, slotSize))
//                                        .entries(REIItemUtils.getItem(recipe.getSlots().get(invIndex)))
//                                        .markInput()
//                        );
//                    } else {
//                        widgets.add(
//                                Widgets.createSlot(new Rectangle(x, y, slotSize, slotSize))
//                                        .markInput()
//                        );
//                    }
//                    invIndex++;
//                    continue;
//                }
//
//                // ===== 核心 =====
//                if (type.equals("E")) {
//                    coreX = x;
//                    coreY = y;
//
//                    widgets.add(
//                            Widgets.createSlot(new Rectangle(x, y, slotSize, slotSize))
//                                    .entries(REIItemUtils.getItem(recipe.getCore()))
//                                    .markInput()
//                    );
//                }
//            }
//        }
//
//        // ===== 箭头（中间）=====
//        widgets.add(Widgets.createArrow(new Point(
//                coreX + 3 * slotSize,
//                coreY + 1
//        )));
//
//        // ===== 输出 =====
//        widgets.add(
//                Widgets.createSlot(new Rectangle(
//                                coreX + 6 * slotSize,
//                                coreY,
//                                slotSize,
//                                slotSize))
//                        .entries(display.getOutputEntries().get(0))
//                        .markOutput()
//        );
//
//        return widgets;
//    }
//
//    @Override
//    public int getDisplayWidth(GensokyoAltarRecipeDisplay display) {
//        return 10 * 18;
//    }
//
//    @Override
//    public int getDisplayHeight() {
//        return 6 * 18;
//    }
//
//    @Override
//    public CategoryIdentifier<? extends GensokyoAltarRecipeDisplay> getCategoryIdentifier() {
//        return REICategoryIdentifiers.GENSOKYO_ALTAR;
//    }
//
//    @Override
//    public Component getTitle() {
//        return RDBlocks.GENSOKYO_ALTAR.asBlock().getName();
//    }
//
//    @Override
//    public Renderer getIcon() {
//        return EntryStacks.of(RDBlocks.GENSOKYO_ALTAR);
//    }
//}
