package cc.thonly.reverie_dreams.gui.recipe.block;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.entity.DanmakuCraftingTableBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.interfaces.IGuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.other.GuiSlotItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.util.PredicateSlot;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Function;

@Getter
public class DanmakuCraftingTableGui extends SimpleGui implements GuiCommon {
    private final DanmakuCraftingTableBlockEntity blockEntity;
    private static final String[] layout = {
            "X#X#X#X#X",
            "####R####",
            "####E####"
    };

    private int resultSlot = -1;

    public DanmakuCraftingTableGui(ServerPlayerEntity player, World world, BlockPos pos) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.setTitle(Text.translatable(ModBlocks.DANMAKU_CRAFTING_TABLE.getTranslationKey()));
        this.blockEntity = (DanmakuCraftingTableBlockEntity) world.getBlockEntity(pos);
        this.init();
    }

    @Override
    public void init() {
        Inventory inventory = this.blockEntity.getInventory();
        int counter = 0;
        int counter2 = 0;
        for (int i = 0; i < layout.length; i++) {
            for (int j = 0; j < layout[i].length(); j++) {
                char c = layout[i].charAt(j);
                if (c == '#') {
                    this.setSlot(counter, new GuiElementBuilder()
                            .setItem(ModGuiItems.EMPTY_SLOT));
                }
                if (c == 'X') {
                    Function<Integer, Function<ItemStack, Boolean>> iffib = index -> {
                        return switch (index) {
                            case 0 -> itemStack -> itemStack.getItem() instanceof DyeItem;
                            case 1 -> itemStack -> itemStack.getItem() == ModItems.DANMAKU_CORE;
                            case 2 -> itemStack -> itemStack.getItem() == ModItems.POWER;
                            case 3 -> itemStack -> itemStack.getItem() == ModItems.POINT;
                            case 4 -> itemStack -> itemStack.getItem() == ModItems.DANMAKU_SHAPE_CREATOR;
                            default -> itemStack -> true;
                        };
                    };
                    this.setSlotRedirect(counter, new PredicateSlot(inventory, counter2, 0, 0, iffib.apply(counter2)));
                    counter2++;
                }
                if (c == 'E') {
                    this.resultSlot = counter;
                    this.setSlot(counter, new GuiElementBuilder()
                            .setItem(Items.AIR)
                    );
                }
                if (c == 'R') {
                    this.setSlot(counter, new GuiElementBuilder()
                            .setItem(ModGuiItems.PROGRESS_TO_RESULT_DOWN)
                    );
                }
                counter++;
            }
        }
    }

    int tick = 0;

    @Override
    public void onTick() {
        super.onTick();
        if (this.blockEntity.getWorld() != null && this.blockEntity.getWorld().getBlockState(blockEntity.getPos()).getBlock() != ModBlocks.DANMAKU_CRAFTING_TABLE) {
            this.close();
            return;
        }

        if (this.resultSlot == -1) {
            return;
        }

        this.tick++;
        if (this.tick > 2) {
            List<ItemStackWrapper> slots = this.getInputs();
            List<DanmakuRecipe> recipeEntries = RecipeManager.DANMAKU_TYPE.getMatches(slots);
            if (!recipeEntries.isEmpty()) {
                DanmakuRecipe recipeEntry = recipeEntries.getFirst();
                ItemStackWrapper resultWrapper = recipeEntry.getOutput().copy();
                ItemStack itemStack = resultWrapper.getItemStack();
                itemStack.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.5f, Optional.of(Identifier.of(UUID.randomUUID().toString()))));

                this.setSlot(this.resultSlot, new GuiElementBuilder(itemStack).setCallback(new GuiElementInterface.ItemClickCallback() {
                    @Override
                    public void click(int i, ClickType clickType, SlotActionType slotActionType) {
                        for (ItemStackWrapper countRecipeSlot : List.of(recipeEntry.getDye(), recipeEntry.getCore(), recipeEntry.getPower(), recipeEntry.getPoint(), recipeEntry.getMaterial())) {
                            if (countRecipeSlot.getItem() != Items.AIR) {
                                Item item = countRecipeSlot.getItem();
                                int count = countRecipeSlot.getCount();
                                DanmakuCraftingTableGui.this.blockEntity.getInventory().removeItem(item, count);
                            }
                        }
                        DanmakuCraftingTableGui.this.player.giveItemStack(itemStack.copy());
                        DanmakuCraftingTableGui.this.setSlot(DanmakuCraftingTableGui.this.resultSlot, new GuiElementBuilder()
                                .setItem(ModGuiItems.PROGRESS_TO_RESULT)
                        );
                    }
                }));
            }
            this.tick = 0;
        }
    }

    private List<ItemStackWrapper> getInputs() {
        List<ItemStackWrapper> countRecipeSlotList = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            ItemStack itemStack = this.blockEntity.getInventory().getStack(i);
            countRecipeSlotList.add(new ItemStackWrapper(itemStack));
        }
        return countRecipeSlotList;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.blockEntity.markDirty();
    }
}
