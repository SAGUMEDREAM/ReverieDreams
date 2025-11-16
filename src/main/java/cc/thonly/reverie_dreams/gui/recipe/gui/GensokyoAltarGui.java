package cc.thonly.reverie_dreams.gui.recipe.gui;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GensokyoAltarGui extends SimpleGui {
    public static final String[][] GRID = {
            {"X", "X", "I", "X", "I", "X", "I", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "I", "X", "E", "X", "I", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "I", "X", "I", "X", "I", "X", "X"},
    };
    ServerPlayer player;
    GensokyoAltarBlockEntity blockEntity;
    BlockState state;
    BlockPos pos;

    public GensokyoAltarGui(ServerPlayer player, BlockState state, Level world, BlockPos pos) {
        super(MenuType.GENERIC_9x5, player, false);
        this.player = player;
        this.state = state;
        this.blockEntity = (GensokyoAltarBlockEntity) world.getBlockEntity(pos);
        this.pos = pos;
        this.init();
    }

    public void init() {
        this.setTitle(Component.translatable(RDBlocks.GENSOKYO_ALTAR.getDescriptionId()));
        int invSlot = 0;
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String pos = GRID[row][col];
                int index = row * 9 + col;
                if (pos.equalsIgnoreCase("X")) {
                    this.setSlot(index, new GuiElementBuilder()
                            .setItem(RDGuiItems.EMPTY_SLOT));
                    continue;
                }
                if (pos.equalsIgnoreCase("I")) {
                    this.setSlotRedirect(index, new Slot(blockEntity.getInventory(), invSlot, 0, 0));
                    invSlot++;
                    continue;
                }
                if (pos.equalsIgnoreCase("E")) {
                    this.setSlotRedirect(index, new Slot(blockEntity.getInventory(), 8, 0, 0));
                }
            }
        }
    }

    @Override
    public void onTick() {
        super.onTick();
        if (blockEntity == null) return;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock() != RDBlocks.GENSOKYO_ALTAR) {
            this.close();
            return;
        }
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        return super.onAnyClick(index, type, action);
    }

    @Override
    public void onOpen() {
        super.onOpen();
    }

    @Override
    public void onClose() {
        super.onClose();

        blockEntity.setChanged();
    }
}
