package cc.thonly.reverie_dreams.gui.recipe.gui;

import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.inf.IGuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.util.WeakHashSet;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class KitchenBlockGui<R extends BaseRecipe> extends SimpleGui implements GuiCommon {
    public static final String[][] GRID = new String[][]{
            {"X", "Z", "Z", "Z", "Z", "Z", "Z", "Z", "X"},
            {"X", "Z", "Z", "Z", "Z", "Z", "Z", "Z", "X"},
            {"X", "Z", "Z", "Z", "Z", "Z", "Z", "Z", "X"},
            {"X", "P", "X", "X", "X", "X", "X", "N", "X"},
            {"X", "Q", "W", "E", "R", "T", "X", "O", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
    };
    public static final Map<String, Integer> CHAR2INDEX = Map.of(
            "Q", 0,
            "W", 1,
            "E", 2,
            "R", 3,
            "T", 4,
            "O", 5
    );

    private final Block block;
    private final KitchenwareBlockEntity blockEntity;
    private final KitchenRecipeType.KitchenType recipeType;
    private final Map<Integer, GuiElementBuilder> displayed = new HashMap<>();
    private final List<Integer> displayIndexes = new ArrayList<>();
    private int page = 0;
    private int maxPage = 0;

    public KitchenBlockGui(Block block, KitchenwareBlockEntity blockEntity, ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.block = block;
        this.blockEntity = blockEntity;
        this.recipeType = blockEntity.getRecipeType();
        this.init();
    }

    @Override
    public void init() {
        Set<KitchenBlockGui<?>> session = KitchenwareBlockEntity.SESSIONS.computeIfAbsent(this.blockEntity.getUuid(), (map) -> new WeakHashSet<>());
        session.add(this);
        this.setTitle(Component.translatable(this.block.getDescriptionId()));
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String posChar = GRID[row][col];
                int index = row * 9 + col;

                switch (posChar) {
                    case "X" -> this.setSlot(index, new GuiElementBuilder(RDGuiItems.EMPTY_SLOT));
                    case "N" -> this.setSlot(index, new GuiElementBuilder(RDGuiItems.NEXT).setCallback((i, t, sat) -> {
                        this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                        if (this.page < this.maxPage) {
                            this.page++;
                            this.onTick();
                        }
                    }));
                    case "P" -> this.setSlot(index, new GuiElementBuilder(RDGuiItems.PREV).setCallback((i, t, sat) -> {
                        this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                        if (this.page > 0) {
                            this.page--;
                            this.onTick();
                        }
                    }));
                    case "Z" -> {
                        GuiElementBuilder guiElementBuilder = new GuiElementBuilder().setItem(Items.AIR);
                        this.displayed.put(index, guiElementBuilder);
                        this.displayIndexes.add(index);
                        this.setSlot(index, guiElementBuilder);
                    }
                    default -> {
                        Integer invIndex = CHAR2INDEX.get(posChar);
                        if (invIndex != null) {
                            this.setSlotRedirect(index, new Slot(this.blockEntity.getInventory(), invIndex, 0, 0));
                        }
                    }
                }
            }
        }
    }

    private ItemStackWrapper buildFoodTags(KitchenRecipe recipe, ItemStackWrapper output, List<ItemStackWrapper> inputs) {
        ItemStack base = output.getItemStack().copy();

        List<ItemStackWrapper> ingredients = recipe.getIngredients();

        // 🔑 用来标记“哪些 input 已经被匹配掉”
        List<ItemStackWrapper> remainingInputs = new ArrayList<>(inputs);

        // ① 一对一匹配并移除（关键逻辑）
        for (ItemStackWrapper ingredient : ingredients) {
            ItemStack ingredientStack = ingredient.getItemStack();

            Iterator<ItemStackWrapper> iterator = remainingInputs.iterator();
            while (iterator.hasNext()) {
                ItemStackWrapper input = iterator.next();
                ItemStack inputStack = input.getItemStack();

                if (ItemStack.isSameItemSameComponents(inputStack, ingredientStack)) {
                    // ✅ 找到一个匹配 → 消耗掉
                    iterator.remove();
                    break; // ⚠️ 只匹配一次！
                }
            }
        }

        // ② 剩下的就是“额外输入”
        Set<FoodProperty> temp = new LinkedHashSet<>();

        // base 已有词条
        List<FoodProperty> baseTags = base.getOrDefault(RDDataComponents.FOOD_PROPERTIES, List.of());
        temp.addAll(baseTags);

        // ③ 只处理剩余输入的词条
        for (ItemStackWrapper input : remainingInputs) {
            ItemStack stack = input.getItemStack();
            List<FoodProperty> props = stack.getOrDefault(RDDataComponents.FOOD_PROPERTIES, List.of());
            temp.addAll(props);
        }

        List<FoodProperty> resultTags = new ArrayList<>(temp);
        base.set(RDDataComponents.FOOD_PROPERTIES, resultTags);

        if (resultTags.size() >= 5) {
            SimpleTriggerFactory
                    .create(SimpleTriggerKeys.KITCHEN_COOKING_AMOUNT_OF_5_TAG)
                    .trigger(this.player);
        }

        return new ItemStackWrapper(base.copy());
    }

    private void handleCrafting(ItemStack output, List<ItemStackWrapper> inputs, KitchenRecipe recipe) {
        this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        SimpleContainer inventory = this.blockEntity.getInventory();
        for (int i = 0; i < 5; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                UseRemainder useRemainderComponent = stack.get(DataComponents.USE_REMAINDER);
                if (useRemainderComponent != null) {
                    ItemStack itemStack = useRemainderComponent.convertInto();
                    this.blockEntity.throwItem((ServerLevel) blockEntity.getLevel(), itemStack);
                }
                stack.shrink(1);
            }
        }
        this.blockEntity.setOutput(new ItemStackWrapper(output.copy()), recipe.getCostTime() * 20.0 + 20 * 0.25 * inputs.size());
        Set<KitchenBlockGui<?>> session = KitchenwareBlockEntity.SESSIONS.computeIfAbsent(this.blockEntity.getUuid(), (map) -> new WeakHashSet<>());
        for (KitchenBlockGui<?> gui : session) {
            if (gui.isOpen()) {
                gui.close();
            }
        }
        session.clear();
        this.close();
    }

    @Override
    public void onTick() {
        super.onTick();

        if (this.blockEntity == null || this.blockEntity.getLevel() == null) {
            this.close();
            return;
        }

        for (Integer index : this.displayIndexes) {
            GuiElementBuilder guiElementBuilder = new GuiElementBuilder().setItem(Items.AIR);
            this.displayed.put(index, guiElementBuilder);
            this.setSlot(index, guiElementBuilder);
        }

        Block block = this.blockEntity.getLevel().getBlockState(this.blockEntity.getBlockPos()).getBlock();
        if (!(block instanceof AbstractKitchenwareBlock)) {
            this.close();
            return;
        }

        KitchenRecipeType kitchenRecipeType = getRecipeTypeInstance();
        SimpleContainer inventory = this.blockEntity.getInventory();
        List<ItemStackWrapper> inputs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            inputs.add(new ItemStackWrapper(inventory.getItem(i).copy()));
        }

        List<KitchenRecipe> matches = kitchenRecipeType.getMatches(this.recipeType, inputs);
        int recipesPerPage = this.displayed.size();
        this.maxPage = (matches.size() - 1) / recipesPerPage;
        List<KitchenRecipe> pageRecipes = getRecipesForCurrentPage(matches, recipesPerPage);

        // 清空旧显示
        for (GuiElementBuilder builder : this.displayed.values()) {
            IGuiElementBuilderAccessor accessor = (IGuiElementBuilderAccessor) builder;
            accessor.reverie_dreams$setItemStack(RDGuiItems.EMPTY_SLOT.getDefaultInstance());
        }

        int i = 0;
        for (Map.Entry<Integer, GuiElementBuilder> entry : this.displayed.entrySet()) {
            if (i >= pageRecipes.size()) break;

            KitchenRecipe recipe = pageRecipes.get(i);
            ItemStack outputShow = this.buildFoodTags(recipe, new ItemStackWrapper(recipe.getOutput().getItemStack().copy()), inputs).getItemStack();
            AtomicReference<ItemStack> output = new AtomicReference<>(outputShow);

            GuiElementBuilder builder = entry.getValue();
            IGuiElementBuilderAccessor accessor = (IGuiElementBuilderAccessor) builder;
            accessor.reverie_dreams$setItemStack(outputShow);

            builder.setCallback((slotIndex, clickType, actionType) -> {
                ItemStack itemStack = output.get();
                for (CraftingConflict conflict : RegistryHandlers.CRAFTING_CONFLICT.values()) {
                    if (conflict.test(itemStack)) {
                        SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_DARK_CUISINE).trigger(this.player);
                        output.set(RDFoodItems.DARK_CUISINE.getDefaultInstance());
                    }
                }
                SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING).trigger(this.player);
                handleCrafting(output.get(), inputs, recipe);
            });

            this.setSlot(entry.getKey(), builder);
            i++;
        }
    }

    private List<KitchenRecipe> getRecipesForCurrentPage(List<KitchenRecipe> all, int pageSize) {
        int start = this.page * pageSize;
        return all.stream().skip(start).limit(pageSize).toList();
    }

    public KitchenRecipeType getRecipeTypeInstance() {
        BaseRecipeType<KitchenRecipe> recipeType = RecipeManager.KITCHEN_TYPE;
        return (KitchenRecipeType) recipeType;
    }

    @Override
    public void onClose() {
        this.blockEntity.setChanged();
        super.onClose();
    }
}
