package cc.thonly.reverie_dreams.gui.recipe.gui;

import cc.thonly.reverie_dreams.block.entity.StrengthenTableBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.item.GuiElementBuilderSetter;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

@Getter
public class StrengthTableGui extends AnvilInputGui implements GuiCommon {
    StrengthenTableBlockEntity blockEntity;
    GuiElementBuilder output;
    String inputText = "";

    public StrengthTableGui(ServerPlayer player, StrengthenTableBlockEntity blockEntity, boolean manipulatePlayerSlots) {
        super(player, manipulatePlayerSlots);
        this.blockEntity = blockEntity;
        this.init();
    }

    public void init() {
        this.setSlot(0, new Slot(this.blockEntity.getInventory(), 0, 0, 0));
        this.setSlot(1, new Slot(this.blockEntity.getInventory(), 1, 0, 0));
        this.output = new GuiElementBuilder()
                .setItem(Items.AIR)
                .setCallback((index, type, action, basedGui) -> click()
                );
        this.setSlot(2, this.output);
    }

    @Override
    public void onTick() {
        super.onTick();
        if (this.blockEntity == null) return;
        if (this.blockEntity.getLevel() != null && this.blockEntity.getLevel().getBlockState(this.blockEntity.getBlockPos()).getBlock() != RDBlocks.STRENGTH_TABLE.asBlock()) {
            this.close();
        }
//        this.inputText = this.getInput();
        ItemStack mainStack = this.blockEntity.getInventory().getItem(0).copy();
        ItemStack offStack = this.blockEntity.getInventory().getItem(1).copy();
        IngredientStack mainSlot = new IngredientStack(mainStack);
        IngredientStack offSlot = new IngredientStack(offStack);
        List<StrengthTableRecipe> entries = RecipeManager.STRENGTH_TABLE.getMatches(List.of(mainSlot, offSlot));
        if (entries.isEmpty()) {
            return;
        }
        StrengthTableRecipe entry = entries.getFirst();
        if (entry != null) {
            IngredientStack outputItemWrapper = entry.getOutput();
            this.output = new GuiElementBuilder()
                    .setCallback((index, type, action, basedGui) -> click());
            GuiElementBuilderSetter.setter(this.output, outputItemWrapper.getLazyStack().copy());
        } else {
            this.output = new GuiElementBuilder().setItem(Items.AIR);
        }
        this.setSlot(2, this.output);
    }

    public void click() {
        if (this.blockEntity == null) return;
        ItemStack mainStack = this.blockEntity.getInventory().getItem(0);
        ItemStack offStack = this.blockEntity.getInventory().getItem(1);
        IngredientStack mainSlot = new IngredientStack(mainStack);
        IngredientStack offSlot = new IngredientStack(offStack);
        List<StrengthTableRecipe> entries = RecipeManager.STRENGTH_TABLE.getMatches(List.of(mainSlot, offSlot));
        if (entries.isEmpty()) {
            return;
        }
        StrengthTableRecipe entry = entries.getFirst();
        if (entry != null) {
            IngredientStack mainItem = entry.getMainItem();
            IngredientStack offItem = entry.getOffItem();
            IngredientStack resultItem = entry.getOutput();

            this.blockEntity.getInventory().removeItem(0, mainItem.getCount());
            this.blockEntity.getInventory().removeItem(1, offItem.getCount());
            ItemStack itemStack = resultItem.getLazyStack().copy();
//            itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.inputText));
            this.player.addItem(itemStack);
        } else {
            this.output.setItem(Items.AIR);
        }
        SimpleTriggerFactory.create(SimpleTriggerKeys.DANMAKU_UPGRADE).trigger(this.player);
        this.player.playSound(SoundEvents.ANVIL_USE, 1.0f, 1.0f);
    }

    @Override
    public void onOpen() {
        super.onOpen();
    }

    @Override
    public void close() {
        super.close();
        this.blockEntity.setChanged();
    }
}
