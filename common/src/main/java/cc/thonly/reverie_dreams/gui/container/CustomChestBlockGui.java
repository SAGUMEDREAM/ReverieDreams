package cc.thonly.reverie_dreams.gui.container;

import cc.thonly.reverie_dreams.block.props.CashBoxBlock;
import cc.thonly.reverie_dreams.block.props.CustomChestBlock;
import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;


public class CustomChestBlockGui extends SimpleGui implements GuiCommon {
    public static final List<ItemLike> COIN_ITEMS = new ArrayList<>(List.of(
            RDItems.COPPER_COIN,
            RDItems.SILVER_COIN,
            RDItems.GOLD_COIN
    ));
    private final Block block;
    private final CustomChestBlockEntity chestBlockEntity;
    private final BlockPos blockPos;
    private final SlotFactory factory;

    public CustomChestBlockGui(Block block, CustomChestBlockEntity chestBlockEntity, ServerPlayer player, SlotFactory factory) {
        super(MenuType.GENERIC_9x3, player, false);
        this.block = block;
        this.chestBlockEntity = chestBlockEntity;
        this.blockPos = chestBlockEntity.getBlockPos();
        this.factory = factory;
        this.setTitle(Component.translatable(block.getDescriptionId()));
        this.init();
    }

    @Override
    public void onTick() {
        super.onTick();
        ServerLevel world = this.player.level();
        if (!(world.getBlockState(this.blockPos).getBlock() instanceof CustomChestBlock) && !(world.getBlockState(this.blockPos).getBlock() instanceof CashBoxBlock)) {
            this.close();
        }
    }

    @Override
    public void init() {
        for (int i = 0; i < this.chestBlockEntity.getContainerSize(); i++) {
            this.setSlot(i, this.factory.get(this.chestBlockEntity.getInventory(), i, 0, 0));
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.player.makeSound(SoundEvents.CHEST_OPEN);
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        this.player.makeSound(SoundEvents.CHEST_CLOSE);
        this.chestBlockEntity.setChanged();
    }

    public interface SlotFactory {
        Slot get(Container inventory, int index, int x, int y);
    }
}
