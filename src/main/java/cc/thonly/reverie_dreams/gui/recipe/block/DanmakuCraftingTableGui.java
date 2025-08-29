package cc.thonly.reverie_dreams.gui.recipe.block;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.entity.DanmakuCraftingTableBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.interfaces.IGuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public class DanmakuCraftingTableGui extends SimpleGui implements GuiCommon {
    private final DanmakuCraftingTableBlockEntity blockEntity;
    private static final String[] layout = {
            "CCCCC###X",
            "CCCCC###X",
            "CCCCC#B#X",
            "CCCCC###X",
            "CCCCC###X",
    };

    private final List<Integer> cSlots = new ArrayList<>();

    public DanmakuCraftingTableGui(ServerPlayerEntity player, World world, BlockPos pos) {
        super(ScreenHandlerType.GENERIC_9X5, player, false);
        this.setTitle(Text.translatable(ModBlocks.DANMAKU_CRAFTING_TABLE.getTranslationKey()));
        this.blockEntity = (DanmakuCraftingTableBlockEntity) world.getBlockEntity(pos);
        this.init();
    }

    @Override
    public void init() {
        Inventory inventory = this.blockEntity.getInventory();

        for (int row = 0; row < layout.length; ++row) {
            for (int col = 0; col < layout[row].length(); ++col) {
                int slot = row * 9 + col;
                char slotType = layout[row].charAt(col);
                switch (slotType) {
                    case '#' -> {
                        this.setSlot(slot, new GuiElementBuilder(ModGuiItems.EMPTY_SLOT));
                    }
                    case 'C' -> {
                        cSlots.add(slot);
                        this.setSlot(slot, new GuiElementBuilder()
                                .setItem(Items.AIR)
                        );
                    }
                    case 'T' -> {
                        this.setSlot(slot, new GuiElementBuilder(ModGuiItems.PROGRESS_TO_RESULT)
                                .setCallback((index, type, action) -> {

                                })
                        );
                    }
                    case 'X' -> {
                        int invSlot = row;
                        this.setSlotRedirect(slot, new Slot(inventory, invSlot, 0, 0));
                    }
                    case 'B' -> {
                        this.setSlot(slot, new GuiElementBuilder()
                                .setItem(ModGuiItems.PROGRESS_TO_RESULT_REVERSE)
                        );
                    }
                }
            }
        }
    }

    int tick = 0;

    @Override
    public void onTick() {
        super.onTick();
        if (blockEntity.getWorld() != null && blockEntity.getWorld().getBlockState(blockEntity.getPos()).getBlock() != ModBlocks.DANMAKU_CRAFTING_TABLE) {
            this.close();
            return;
        }
        this.tick++;

        if (this.tick > 2) {
            List<ItemStackWrapper> slots = getInputs();
//            System.out.println(slots);
            List<DanmakuRecipe> recipeEntries = RecipeManager.DANMAKU_TYPE.getMatches(slots);
//            System.out.println(recipeEntries);
            int resultIndex = 0;

            for (int slot : this.cSlots) {
                if (resultIndex < recipeEntries.size()) {
                    DanmakuRecipe recipeEntry = recipeEntries.get(resultIndex++);
                    ItemStackWrapper resultSlot = recipeEntry.getOutput();
                    ItemStack resultItemStack = resultSlot.getItemStack().copy();
                    GuiElementBuilder elementBuilder = new GuiElementBuilder()
                            .setItem(resultItemStack.getItem())
                            .setCount(resultItemStack.getCount())
                            .setCallback((index, type, action) -> handleCrafting(index, recipeEntry));
                    IGuiElementBuilderAccessor elementBuilderAccessor = (IGuiElementBuilderAccessor) elementBuilder;
                    elementBuilderAccessor.setItemStack(resultItemStack);
                    this.setSlot(slot, elementBuilder);
                } else {
                    this.setSlot(slot, new GuiElementBuilder()
                            .setItem(Items.AIR)
                    );
                }
            }

            this.tick = 0;
        }
    }

    private void handleCrafting(int index, DanmakuRecipe recipeEntry) {
        SimpleInventory inventory = blockEntity.getInventory();
        // 移除配方中所需的物品
        for (ItemStackWrapper countRecipeSlot : List.of(recipeEntry.getDye(), recipeEntry.getCore(), recipeEntry.getPower(), recipeEntry.getPoint(), recipeEntry.getMaterial())) {
            if (countRecipeSlot.getItem() != Items.AIR) {
                Item item = countRecipeSlot.getItem();
                int count = countRecipeSlot.getCount();
                inventory.removeItem(item, count);
            }
        }

        // 获取合成结果并给予玩家
        ItemStackWrapper resultSlot = recipeEntry.getOutput();
        ItemStack resultItemStack = resultSlot.getItemStack().copy();

        this.player.giveItemStack(resultItemStack);
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
