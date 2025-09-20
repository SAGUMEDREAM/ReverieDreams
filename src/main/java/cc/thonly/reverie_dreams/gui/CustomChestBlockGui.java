package cc.thonly.reverie_dreams.gui;

import cc.thonly.reverie_dreams.block.CashBoxBlock;
import cc.thonly.reverie_dreams.block.CustomChestBlock;
import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.Block;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;


public class CustomChestBlockGui extends SimpleGui implements GuiCommon {
    private final Block block;
    private final CustomChestBlockEntity chestBlockEntity;
    private final BlockPos blockPos;

    public CustomChestBlockGui(Block block, CustomChestBlockEntity chestBlockEntity, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.block = block;
        this.chestBlockEntity = chestBlockEntity;
        this.blockPos = chestBlockEntity.getPos();
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
            this.setSlotRedirect(i, new Slot(this.chestBlockEntity.getInventory(), i, 0, 0));
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

}
