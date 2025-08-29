package cc.thonly.reverie_dreams.gui.recipe.block;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.entity.StrengthenTableBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.interfaces.IGuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.List;

@Getter
public class StrengthTableGui extends AnvilInputGui implements GuiCommon {
    StrengthenTableBlockEntity blockEntity;
    GuiElementBuilder output;
    String inputText = "";

    public StrengthTableGui(ServerPlayerEntity player, StrengthenTableBlockEntity blockEntity, boolean manipulatePlayerSlots) {
        super(player, manipulatePlayerSlots);
        this.blockEntity = blockEntity;
        this.init();
    }

    public void init() {
        this.setSlotRedirect(0, new Slot(this.blockEntity.getInventory(), 0, 0, 0));
        this.setSlotRedirect(1, new Slot(this.blockEntity.getInventory(), 1, 0, 0));
        this.output = new GuiElementBuilder()
                .setItem(Items.AIR)
                .setCallback((index, type, action) -> click()
                );
        this.setSlot(2, this.output);
    }

    @Override
    public void onTick() {
        super.onTick();
        if (this.blockEntity == null) return;
        if (this.blockEntity.getWorld() != null && this.blockEntity.getWorld().getBlockState(this.blockEntity.getPos()).getBlock() != ModBlocks.STRENGTH_TABLE) {
            this.close();
        }
//        this.inputText = this.getInput();
        ItemStack mainStack = this.blockEntity.getInventory().getStack(0).copy();
        ItemStack offStack = this.blockEntity.getInventory().getStack(1).copy();
        ItemStackWrapper mainSlot = new ItemStackWrapper(mainStack);
        ItemStackWrapper offSlot = new ItemStackWrapper(offStack);
        List<StrengthTableRecipe> entries = RecipeManager.STRENGTH_TABLE.getMatches(List.of(mainSlot, offSlot));
        if (entries.isEmpty()) {
            return;
        }
        StrengthTableRecipe entry = entries.getFirst();
        if (entry != null) {
            ItemStackWrapper outputItemWrapper = entry.getOutput();
            this.output = new GuiElementBuilder()
                    .setCallback((index, type, action) -> click());
            IGuiElementBuilderAccessor outputAccessor = (IGuiElementBuilderAccessor) this.output;
            outputAccessor.setItemStack(outputItemWrapper.getItemStack().copy());
        } else {
            this.output = new GuiElementBuilder().setItem(Items.AIR);
        }
        this.setSlot(2, this.output);
    }

    public void click() {
        if (this.blockEntity == null) return;
        ItemStack mainStack = this.blockEntity.getInventory().getStack(0);
        ItemStack offStack = this.blockEntity.getInventory().getStack(1);
        ItemStackWrapper mainSlot = new ItemStackWrapper(mainStack);
        ItemStackWrapper offSlot = new ItemStackWrapper(offStack);
        List<StrengthTableRecipe> entries = RecipeManager.STRENGTH_TABLE.getMatches(List.of(mainSlot, offSlot));
        if (entries.isEmpty()) {
            return;
        }
        StrengthTableRecipe entry = entries.getFirst();
        if (entry != null) {
            ItemStackWrapper mainItem = entry.getMainItem();
            ItemStackWrapper offItem = entry.getOffItem();
            ItemStackWrapper resultItem = entry.getOutput();

            this.blockEntity.getInventory().removeStack(0, mainItem.getCount());
            this.blockEntity.getInventory().removeStack(1, offItem.getCount());
            ItemStack itemStack = resultItem.getItemStack().copy();
//            itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.inputText));
            this.player.giveItemStack(itemStack);
        } else {
            this.output.setItem(Items.AIR);
        }
        this.player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void onOpen() {
        super.onOpen();
    }

    @Override
    public void onClose() {
        super.onClose();
        this.blockEntity.markDirty();
    }


}
