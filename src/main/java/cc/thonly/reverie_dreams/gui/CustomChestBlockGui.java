package cc.thonly.reverie_dreams.gui;

import cc.thonly.reverie_dreams.block.CashBoxBlock;
import cc.thonly.reverie_dreams.block.CustomChestBlock;
import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.util.PredicateSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class CustomChestBlockGui extends SimpleGui implements GuiCommon {
    public static final List<Item> COIN_ITEMS = new ArrayList<>(List.of(
            ModItems.COPPER_COIN,
            ModItems.SILVER_COIN,
            ModItems.GOLD_COIN
    ));
    private final Block block;
    private final CustomChestBlockEntity chestBlockEntity;
    private final BlockPos blockPos;
    private final SlotFactory factory;

    public CustomChestBlockGui(Block block, CustomChestBlockEntity chestBlockEntity, ServerPlayerEntity player, SlotFactory factory) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.block = block;
        this.chestBlockEntity = chestBlockEntity;
        this.blockPos = chestBlockEntity.getPos();
        this.factory = factory;
        this.setTitle(Text.translatable(block.getTranslationKey()));
        this.init();
    }

    @Override
    public void onTick() {
        super.onTick();
        ServerWorld world = this.player.getWorld();
        if (!(world.getBlockState(this.blockPos).getBlock() instanceof CustomChestBlock) && !(world.getBlockState(this.blockPos).getBlock() instanceof CashBoxBlock)) {
            this.close();
        }
    }

    @Override
    public void init() {
        for (int i = 0; i < this.chestBlockEntity.size(); i++) {
            this.setSlotRedirect(i, this.factory.get(this.chestBlockEntity.getInventory(), i, 0, 0));
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.player.playSound(SoundEvents.BLOCK_CHEST_OPEN);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.chestBlockEntity.markDirty();
    }

    public interface SlotFactory {
        Slot get(Inventory inventory, int index, int x, int y);
    }
}
