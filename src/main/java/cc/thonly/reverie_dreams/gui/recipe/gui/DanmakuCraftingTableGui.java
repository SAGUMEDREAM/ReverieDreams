package cc.thonly.reverie_dreams.gui.recipe.gui;

import cc.thonly.reverie_dreams.block.entity.DanmakuCraftingTableBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.PredicateSlot;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public DanmakuCraftingTableGui(ServerPlayer player, Level world, BlockPos pos) {
        super(MenuType.GENERIC_9x3, player, false);
        this.setTitle(Component.translatable(RDBlocks.DANMAKU_CRAFTING_TABLE.getDescriptionId()));
        this.blockEntity = (DanmakuCraftingTableBlockEntity) world.getBlockEntity(pos);
        this.init();
    }

    @Override
    public void init() {
        Container inventory = this.blockEntity.getInventory();
        int counter = 0;
        int counter2 = 0;
        for (int i = 0; i < layout.length; i++) {
            for (int j = 0; j < layout[i].length(); j++) {
                char c = layout[i].charAt(j);
                if (c == '#') {
                    this.setSlot(counter, new GuiElementBuilder()
                            .setItem(RDGuiItems.EMPTY_SLOT));
                }
                if (c == 'X') {
                    Function<Integer, Function<ItemStack, Boolean>> iffib = index -> {
                        return switch (index) {
                            case 0 -> itemStack -> itemStack.getItem() instanceof DyeItem;
                            case 1 -> itemStack -> itemStack.getItem() == RDItems.DANMAKU_CORE;
                            case 2 -> itemStack -> itemStack.getItem() == RDItems.POWER;
                            case 3 -> itemStack -> itemStack.getItem() == RDItems.POINT;
                            case 4 -> itemStack -> itemStack.getItem() == RDItems.DANMAKU_SHAPE_CREATOR;
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
                            .setItem(RDGuiItems.PROGRESS_TO_RESULT_DOWN)
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
        if (this.blockEntity.getLevel() != null && this.blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock() != RDBlocks.DANMAKU_CRAFTING_TABLE) {
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
                itemStack.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.5f, Optional.of(ResourceLocation.parse(UUID.randomUUID().toString()))));

                this.setSlot(this.resultSlot, new GuiElementBuilder(itemStack).setCallback(new GuiElementInterface.ItemClickCallback() {
                    @Override
                    public void click(int i, ClickType clickType, net.minecraft.world.inventory.ClickType slotActionType) {
                        for (ItemStackWrapper countRecipeSlot : List.of(recipeEntry.getDye(), recipeEntry.getCore(), recipeEntry.getPower(), recipeEntry.getPoint(), recipeEntry.getMaterial())) {
                            if (countRecipeSlot.getItem() != Items.AIR) {
                                Item item = countRecipeSlot.getItem();
                                int count = countRecipeSlot.getCount();
                                DanmakuCraftingTableGui.this.blockEntity.getInventory().removeItemType(item, count);
                            }
                        }
                        DanmakuCraftingTableGui.this.player.addItem(itemStack.copy());
                        DanmakuCraftingTableGui.this.setSlot(DanmakuCraftingTableGui.this.resultSlot, new GuiElementBuilder()
                                .setItem(RDGuiItems.PROGRESS_TO_RESULT)
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
            ItemStack itemStack = this.blockEntity.getInventory().getItem(i);
            countRecipeSlotList.add(new ItemStackWrapper(itemStack));
        }
        return countRecipeSlotList;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.blockEntity.setChanged();
    }
}
