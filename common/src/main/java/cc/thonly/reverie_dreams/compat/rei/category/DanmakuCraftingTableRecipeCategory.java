//package cc.thonly.reverie_dreams.compat.rei.category;
//
//import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
//import cc.thonly.reverie_dreams.compat.rei.display.DanmakuCraftingTableDisplay;
//import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
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
//import java.util.ArrayList;
//import java.util.List;
//
//public class DanmakuCraftingTableRecipeCategory implements DisplayCategory<DanmakuCraftingTableDisplay> {
//    @Override
//    public List<Widget> setupDisplay(DanmakuCraftingTableDisplay display, Rectangle bounds) {
//        int startX = bounds.getX();
//        int startY = bounds.getY();
//
//        int inputCount = 5;
//        int slotSize = 18;
//        int gap = 2;
//
//        int inputsWidth = inputCount * (slotSize + gap);
//        int arrowWidth = 24;
//        int arrowGap = 6;
//        int outputWidth = slotSize;
//
//        int width = inputsWidth + arrowGap + arrowWidth + arrowGap + outputWidth;
//        int height = 60;
//
//        int xOffset = startX + (bounds.getWidth() - width) / 2;
//        int yOffset = startY + (bounds.getHeight() - height) / 2;
//
//        List<Widget> widgets = new ArrayList<>();
//
//        widgets.add(Widgets.createRecipeBase(bounds));
//
//        int slotY = yOffset + (height - slotSize) / 2;
//
//        for (int i = 0; i < inputCount; i++) {
//            widgets.add(Widgets.createSlot(new Rectangle(
//                            xOffset + i * (slotSize + gap),
//                            slotY,
//                            slotSize,
//                            slotSize))
//                    .entries(display.getInputEntries().get(i))
//                    .markInput());
//        }
//
//        int arrowX = xOffset + inputsWidth + arrowGap;
//        widgets.add(Widgets.createArrow(new Point(arrowX, slotY + 1)));
//
//        int outputX = arrowX + arrowWidth + arrowGap;
//        widgets.add(Widgets.createSlot(new Rectangle(
//                        outputX,
//                        slotY,
//                        slotSize,
//                        slotSize))
//                .entries(display.getOutputEntries().getFirst())
//                .markOutput());
//
//        return widgets;
//    }
//
//    @Override
//    public CategoryIdentifier<? extends DanmakuCraftingTableDisplay> getCategoryIdentifier() {
//        return REICategoryIdentifiers.DANMAKU_CRAFTING_TABLE;
//    }
//
//    @Override
//    public int getDisplayWidth(DanmakuCraftingTableDisplay display) {
//        return 180;
//    }
//
//    @Override
//    public int getDisplayHeight() {
//        return 60;
//    }
//
//    @Override
//    public Component getTitle() {
//        return RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock().getName();
//    }
//
//    @Override
//    public Renderer getIcon() {
//        return EntryStacks.of(RDBlocks.DANMAKU_CRAFTING_TABLE);
//    }
//}
