//package cc.thonly.reverie_dreams.compat.rei.category;
//
//import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
//import cc.thonly.reverie_dreams.compat.rei.display.KitchenDisplay;
//import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
//import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
//import cc.thonly.reverie_dreams.registry.content.item.RDItems;
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
//import net.minecraft.world.item.ItemStack;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class KitchenRecipeCategory implements DisplayCategory<KitchenDisplay> {
//
//    @Override
//    public List<Widget> setupDisplay(KitchenDisplay display, Rectangle bounds) {
//        int startX = bounds.getX();
//        int startY = bounds.getY();
//
//        int slotSize = 18;
//
//        int width = 10 * slotSize; // 180
//        int height = 27;
//
//        // ===== 居中 =====
//        int xOffset = startX + (bounds.getWidth() - width) / 2 + 4;
//        int yOffset = startY + (bounds.getHeight() - height) / 2 + 4;
//
//        List<Widget> widgets = new ArrayList<>();
//        widgets.add(Widgets.createRecipeBase(bounds));
//
//        var recipe = display.getRecipe();
//
//        int startInputX = xOffset + 2 * slotSize;
//        int centerY = yOffset;
//
//        // ===== 输入 =====
//        for (int i = 0; i < 5; i++) {
//            Rectangle rect = new Rectangle(
//                    startInputX + i * slotSize - 9,
//                    centerY,
//                    slotSize,
//                    slotSize
//            );
//
//            var slot = Widgets.createSlot(rect).markInput();
//
//            if (i < recipe.getIngredients().size()) {
//                slot.entries(REIItemUtils.getItem(recipe.getIngredients().get(i)));
//            }
//
//            widgets.add(slot);
//        }
//
//        // ===== 箭头 =====
//        widgets.add(Widgets.createArrow(new Point(
//                startInputX + 5 * slotSize - 8,
//                centerY + (slotSize - 16) / 2
//        )));
//
//        // ===== 输出 =====
//        widgets.add(
//                Widgets.createSlot(new Rectangle(
//                                startInputX + 6 * slotSize,
//                                centerY,
//                                slotSize,
//                                slotSize))
//                        .entries(display.getOutputEntries().getFirst())
//                        .markOutput()
//        );
//
//        // ===== 设备图标 =====
//        var type = recipe.getRecipeTypeMapping();
//
//        ItemStack iconStack;
//
//        if (type == KitchenRecipeType.MappingType.COOKING_POT) {
//            iconStack = KitchenBlocks.COOKING_POT.asItem().getDefaultInstance();
//        } else if (type == KitchenRecipeType.MappingType.CUTTING_BOARD) {
//            iconStack = KitchenBlocks.CUTTING_BOARD.asItem().getDefaultInstance();
//        } else if (type == KitchenRecipeType.MappingType.FRYING_PAN) {
//            iconStack = KitchenBlocks.FRYING_PAN.asItem().getDefaultInstance();
//        } else if (type == KitchenRecipeType.MappingType.GRILL) {
//            iconStack = KitchenBlocks.GRILL.asItem().getDefaultInstance();
//        } else if (type == KitchenRecipeType.MappingType.STEAMER) {
//            iconStack = KitchenBlocks.STEAMER.asItem().getDefaultInstance();
//        } else {
//            iconStack = RDItems.MYSTIA_ICON.createStack();
//        }
//
//        widgets.add(
//                Widgets.createSlot(new Rectangle(
//                                xOffset + 2,
//                                centerY,
//                                slotSize,
//                                slotSize))
//                        .entries(REIItemUtils.getItem(iconStack))
//                        .markInput()
//                        .disableBackground()
//        );
//
//        return widgets;
//    }
//
//    @Override
//    public int getDisplayWidth(KitchenDisplay display) {
//        return 18 * 10;
//    }
//
//    @Override
//    public int getDisplayHeight() {
//        return 27 + 9;
//    }
//
//    @Override
//    public CategoryIdentifier<? extends KitchenDisplay> getCategoryIdentifier() {
//        return REICategoryIdentifiers.KITCHEN;
//    }
//
//    @Override
//    public Component getTitle() {
//        return RDItems.MYSTIA_ICON.asItem().getName();
//    }
//
//    @Override
//    public Renderer getIcon() {
//        return EntryStacks.of(RDItems.MYSTIA_ICON);
//    }
//}
