//package cc.thonly.mystias_izakaya.gui.recipe.block;
//
//import cc.thonly.mystias_izakaya.block.MIBlocks;
//import cc.thonly.mystias_izakaya.block.entity.CooktopBlockEntity;
//import cc.thonly.reverie_dreams.item.ModGuiItems;
//import eu.pb4.sgui.api.elements.GuiElementBuilder;
//import eu.pb4.sgui.api.gui.SimpleGui;
//import lombok.Getter;
//import net.minecraft.screen.ScreenHandlerType;
//import net.minecraft.screen.slot.Slot;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.text.Text;
//
//@Getter
//@Deprecated
//public class CooktopBlockGui extends SimpleGui {
//    private final CooktopBlockEntity blockEntity;
//    public static final String[] GRID = new String[]{
//            "X","X","I","X","X"
//    };
//    public CooktopBlockGui(ServerPlayerEntity entity, CooktopBlockEntity blockEntity) {
//        super(ScreenHandlerType.HOPPER, entity, false);
//        this.blockEntity = blockEntity;
//        this.init();
//    }
//
//    public void init() {
//        this.setTitle(Text.translatable(MIBlocks.COOKTOP.getTranslationKey()));
//        for (int i = 0; i < GRID.length; i++) {
//            if (GRID[i].equals("X")) {
//                this.setSlot(i, new GuiElementBuilder(ModGuiItems.EMPTY_SLOT));
//            } else if (GRID[i].equals("I")) {
//                this.setSlotRedirect(i, new Slot(this.blockEntity.getInventory(), 0, 0, 0));
//            }
//        }
//    }
//
//    @Override
//    public void onClose() {
//        super.onClose();
//        this.blockEntity.markDirty();
//    }
//}
