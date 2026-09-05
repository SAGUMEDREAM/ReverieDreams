package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.block.entity.BrewingBarrelBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.gui.slot.LimitedSlot;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.util.DistributedTickTask;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingBarrelGui extends SimpleGui implements GuiCommon {
    private final ServerPlayer player;
    private final BrewingBarrelBlockEntity blockEntity;
    private final BlockState state;
    private final BlockPos pos;
    private final DistributedTickTask closeCheckTickTask;

    public BrewingBarrelGui(
            ServerPlayer player,
            BlockState state,
            Level world,
            BlockPos pos
    ) {
        super(MenuType.GENERIC_9x1, player, false);
        this.player = player;
        this.state = state;
        this.blockEntity = (BrewingBarrelBlockEntity) world.getBlockEntity(pos);
        this.pos = pos;
        this.closeCheckTickTask = DistributedTickTask.createTickTask(()->{
            if (this.blockEntity == null) {
                return;
            }
            Level level = this.blockEntity.getLevel();
            if (level == null) {
                this.close();
                return;
            }

            if (level.getBlockState(this.pos).getBlock()
                    != RDBlocks.BREWING_BARREL.asBlock()) {
                this.close();
            }
        }, 3);

        this.init();
    }

    @Override
    public void init() {
        this.setTitle(Component.translatable(
                        RDBlocks.BREWING_BARREL.asBlock().getDescriptionId()
                )
        );
        if (this.blockEntity == null) {
            return;
        }
        SimpleContainer inventory = this.blockEntity.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (i >= BrewingBarrelBlockEntity.SIZE) {
                break;
            }
            this.setSlotRedirect(i, new LimitedSlot(inventory, i, 1));
        }
    }

    @Override
    public void onTick() {
        super.onTick();

        if (this.blockEntity == null) {
            this.close();
            return;
        }

        this.closeCheckTickTask.tick();
    }

    @Override
    public boolean onAnyClick(
            int index,
            ClickType type,
            net.minecraft.world.inventory.ClickType action
    ) {
        Level level = this.blockEntity.getLevel();

        if (level != null) {
            level.sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_ALL
            );

            level.sendBlockUpdated(
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
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);

        Level level = this.blockEntity.getLevel();

        if (level != null) {
            level.sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_ALL
            );

            level.sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_CLIENTS
            );

            this.blockEntity.setChanged();
        }
    }

}