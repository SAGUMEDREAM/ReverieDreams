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
                        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                        if (this.page < this.maxPage) {
                            this.page++;
                            this.onTick();
                        }
                    }));
                    case "P" -> this.setSlot(index, new GuiElementBuilder(RDGuiItems.PREV).setCallback((i, t, sat) -> {
                        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
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
        List<String> baseTags = base.getOrDefault(RDDataComponents.FOOD_PROPERTIES, new ArrayList<>());

        HashSet<String> propertyIds = new HashSet<>(baseTags);
        List<ItemStackWrapper> ingredients = recipe.getIngredients();
        List<Item> ingredientItems = ingredients
                .stream()
                .filter(wrapper -> !wrapper.isEmpty())
                .map(ItemStackWrapper::getItem)
                .toList();
        for (ItemStackWrapper input : inputs) {
            ItemStack itemStack = input.getItemStack();
            Item item = itemStack.getItem();
            if (ingredientItems.contains(item)) {
                continue;
            }
            List<FoodProperty> ingredientProperties = FoodProperty.getIngredientProperties(item);
            ingredientProperties.forEach(property -> propertyIds.add(property.getId().toString()));
        }
        List<String> tagList = new ArrayList<>(propertyIds);
        base.set(RDDataComponents.FOOD_PROPERTIES, tagList);
        if (tagList.size() >= 5) {
            SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING_AMOUNT_OF_5_TAG).trigger(this.player);
        }
        return new ItemStackWrapper(base.copy());
    }

    private ItemStackWrapper buildAllFoodTags(ItemStackWrapper output, List<ItemStackWrapper> inputs) {
        ItemStack itemStack = output.getItemStack().copy();
        List<String> outputTags = itemStack.get(RDDataComponents.FOOD_PROPERTIES);
        if (outputTags == null) {
            outputTags = new ArrayList<>();
        }
        HashSet<String> propertyIds = new HashSet<>(outputTags);
        for (ItemStackWrapper wrapper : inputs) {
            ItemStack wrapperItemStack = wrapper.getItemStack();
            if (wrapperItemStack.isEmpty()) {
                continue;
            }
            List<FoodProperty> ingredientProperties = FoodProperty.getIngredientProperties(wrapperItemStack.getItem());
            ingredientProperties.forEach(property -> propertyIds.add(property.getId().toString()));
        }
        List<String> tagList = new ArrayList<>(propertyIds);
        itemStack.set(RDDataComponents.FOOD_PROPERTIES, tagList);
        return new ItemStackWrapper(itemStack.copy());
    }

    private void handleCrafting(ItemStack output, List<ItemStackWrapper> inputs, KitchenRecipe recipe) {
        this.player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
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
            SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING).trigger(this.player);
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
