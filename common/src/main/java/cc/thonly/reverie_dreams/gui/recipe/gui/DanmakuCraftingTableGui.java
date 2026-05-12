package cc.thonly.reverie_dreams.gui.recipe.gui;

import cc.thonly.reverie_dreams.block.entity.DanmakuCraftingTableBlockEntity;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.PredicateSlot;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerInput;
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
        this.setTitle(Component.translatable(RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock().getDescriptionId()));
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
                            .setItem(RDGuiItems.EMPTY_SLOT.asItem()));
                }
                if (c == 'X') {
                    Function<Integer, Function<ItemStack, Boolean>> iffib = index -> switch (index) {
                        case 0 -> itemStack -> itemStack.getItem() instanceof DyeItem;
                        case 1 -> itemStack -> itemStack.getItem() == RDItems.DANMAKU_CORE.asItem();
                        case 2 -> itemStack -> itemStack.getItem() == RDItems.POWER.asItem();
                        case 3 -> itemStack -> itemStack.getItem() == RDItems.POINT.asItem();
                        case 4 -> itemStack -> itemStack.getItem() == RDItems.DANMAKU_SHAPE_CREATOR.asItem();
                        default -> itemStack -> true;
                    };
                    this.setSlot(counter, new PredicateSlot(inventory, counter2, 0, 0, iffib.apply(counter2)));
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
                            .setItem(RDGuiItems.PROGRESS_TO_RESULT_DOWN.asItem())
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
        if (this.blockEntity.getLevel() != null && this.blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock() != RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock()) {
            this.close();
            return;
        }

        if (this.resultSlot == -1) {
            return;
        }

        this.tick++;
        if (this.tick > 2) {
            List<IngredientStack> slots = this.getInputs();
            List<DanmakuRecipe> recipeEntries = RecipeManager.DANMAKU.getMatches(slots);
            if (!recipeEntries.isEmpty()) {
                DanmakuRecipe recipeEntry = recipeEntries.getFirst();
                IngredientStack resultStack = recipeEntry.getOutput().copy();
                ItemStack itemStack = resultStack.build();
                itemStack.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.5f, Optional.of(Identifier.parse(UUID.randomUUID().toString()))));

                this.setSlot(this.resultSlot, new GuiElementBuilder(itemStack).setCallback(new GuiElement.ClickCallback() {
                    @Override
                    public void click(int i, ClickType clickType, ContainerInput input, SlotBasedGui slotBasedGui) {
                        for (IngredientStack countRecipeSlot : List.of(recipeEntry.getDye(), recipeEntry.getCore(), recipeEntry.getPower(), recipeEntry.getPoint(), recipeEntry.getMaterial())) {
                            if (countRecipeSlot.getItem() != Items.AIR) {
                                Item item = countRecipeSlot.getItem();
                                int count = countRecipeSlot.getCount();
                                DanmakuCraftingTableGui.this.blockEntity.getInventory().removeItemType(item, count);
                            }
                        }
                        DanmakuCraftingTableGui.this.player.addItem(itemStack.copy());
                        DanmakuCraftingTableGui.this.setSlot(DanmakuCraftingTableGui.this.resultSlot, new GuiElementBuilder()
                                .setItem(RDGuiItems.PROGRESS_TO_RESULT.asItem())
                        );
                    }
                }));
            } else {
                this.setSlot(this.resultSlot, new GuiElementBuilder(ItemStack.EMPTY));
            }
            this.tick = 0;
        }
    }

    private List<IngredientStack> getInputs() {
        List<IngredientStack> countRecipeSlotList = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            ItemStack itemStack = this.blockEntity.getInventory().getItem(i);
            countRecipeSlotList.add(new IngredientStack(itemStack));
        }
        return countRecipeSlotList;
    }

    @Override
    public void onManualClose() {
        super.onManualClose();
        this.blockEntity.setChanged();
    }
}
