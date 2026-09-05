package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.StrengthenTableBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.item.GuiElementBuilderSetter;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

@Getter
public class StrengthTableGui extends SimpleGui implements GuiCommon {
    public static final String[][] GRID = new String[][]{
            {"X", "X", "I", "I", "X", "X", "I", "X", "X"}
    };
    StrengthenTableBlockEntity blockEntity;
    GuiElementBuilder output;

    public StrengthTableGui(ServerPlayer player, StrengthenTableBlockEntity blockEntity, boolean manipulatePlayerSlots) {
        super(MenuType.GENERIC_9x1, player, manipulatePlayerSlots);
        this.blockEntity = blockEntity;
        this.init();
    }

    public void init() {
        this.setTitle(
                Component.empty()
                        .append(Component.translatable("space.-8"))
                        .append(Component.literal("\ub004")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                        .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                        .append(Component.translatable("space.-168"))
                        .append(Component.translatable(RDBlocks.STRENGTH_TABLE.asBlock().getDescriptionId()))
        );
        this.setSlotRedirect(2, new Slot(this.blockEntity.getInventory(), 0, 0, 0));
        this.setSlotRedirect(3, new Slot(this.blockEntity.getInventory(), 1, 0, 0));
        this.output = new GuiElementBuilder()
                .setItem(Items.AIR)
                .setCallback((index, type, action, basedGui) -> click());
        this.setSlot(6, this.output);
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
        this.setSlot(6, this.output);
    }

    public void click() {
        if (this.blockEntity == null) return;
        ItemStack mainStack = this.blockEntity.getInventory().getItem(2);
        ItemStack offStack = this.blockEntity.getInventory().getItem(3);
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

            this.blockEntity.getInventory().removeItem(2, mainItem.getCount());
            this.blockEntity.getInventory().removeItem(3, offItem.getCount());
            ItemStack itemStack = resultItem.getLazyStack().copy();
//            itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.inputText));
            this.player.addItem(itemStack);
            SimpleTriggerFactory.create(SimpleTriggerKeys.DANMAKU_UPGRADE).trigger(this.player);
            SoundEventPlayUtils.playSound(this.player, SoundEvents.ANVIL_USE, SoundSource.UI);
        } else {
            this.output.setItem(Items.AIR);
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        this.blockEntity.setChanged();
    }
}
