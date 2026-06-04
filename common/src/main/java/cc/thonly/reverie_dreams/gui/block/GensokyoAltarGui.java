package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
        this.setTitle(
                Component.empty()
                         .append(Component.translatable("space.-8"))
                         .append(Component.literal("\ub002")
                                          .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                                                .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                         .append(Component.translatable("space.-168"))
                         .append(Component.translatable(RDBlocks.GENSOKYO_ALTAR.asBlock().getDescriptionId()))
        );
        int invSlot = 0;
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String pos = GRID[row][col];
                int index = row * 9 + col;
                if (pos.equalsIgnoreCase("X")) {
                    continue;
                }
                if (pos.equalsIgnoreCase("I")) {
                    this.setSlot(index, new Slot(blockEntity.getInventory(), invSlot, 0, 0));
                    invSlot++;
                    continue;
                }
                if (pos.equalsIgnoreCase("E")) {
                    this.setSlot(index, new Slot(blockEntity.getInventory(), 8, 0, 0));
                }
            }
        }
    }

    @Override
    public void onTick() {
        super.onTick();
        if (blockEntity == null) return;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock() != RDBlocks.GENSOKYO_ALTAR.asBlock()) {
            this.close();
            return;
        }
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, ContainerInput action) {
        if (this.blockEntity.getLevel() != null) {
            this.blockEntity.getLevel().sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_ALL
            );
            this.blockEntity.getLevel().sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_CLIENTS
            );
            this.blockEntity.setChanged();
        }
        return super.onAnyClick(index, type, action);
    }

    @Override
    public void onOpen() {
        super.onOpen();
    }

    @Override
    public void onManualClose() {
        super.onManualClose();
        if (this.blockEntity.getLevel() != null) {
            this.blockEntity.getLevel().sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_ALL
            );
            this.blockEntity.getLevel().sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_CLIENTS
            );
            this.blockEntity.setChanged();
        }
    }

}
