package cc.thonly.mystias_izakaya.gui.recipe.block;

import cc.thonly.mystias_izakaya.block.AbstractKitchenwareBlock;
import cc.thonly.mystias_izakaya.block.entity.KitchenwareBlockEntity;
import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.component.MIDataComponentTypes;
import cc.thonly.mystias_izakaya.recipe.MiRecipeManager;
import cc.thonly.mystias_izakaya.recipe.entry.KitchenRecipe;
import cc.thonly.mystias_izakaya.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.gui.access.GuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.slot.ItemStackRecipeWrapper;
import cc.thonly.reverie_dreams.util.WeakSet;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.Block;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.*;

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

    public KitchenBlockGui(Block block, KitchenwareBlockEntity blockEntity, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.block = block;
        this.blockEntity = blockEntity;
        this.recipeType = blockEntity.getRecipeType();
        this.init();
    }

    @Override
    public void init() {
        Set<KitchenBlockGui<?>> session = KitchenwareBlockEntity.SESSIONS.computeIfAbsent(this.blockEntity.getUuid(), (map) -> new WeakSet<>());
        session.add(this);
        this.setTitle(Text.translatable(this.block.getTranslationKey()));
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String posChar = GRID[row][col];
                int index = row * 9 + col;

                switch (posChar) {
                    case "X" -> this.setSlot(index, new GuiElementBuilder().setItem(ModGuiItems.EMPTY_SLOT));
                    case "N" ->
                            this.setSlot(index, new GuiElementBuilder().setItem(ModGuiItems.NEXT).setCallback((i, t, sat) -> {
                                this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                if (this.page < this.maxPage) {
                                    this.page++;
                                    this.onTick();
                                }
                            }));
                    case "P" ->
                            this.setSlot(index, new GuiElementBuilder().setItem(ModGuiItems.PREV).setCallback((i, t, sat) -> {
                                this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
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

    private ItemStackRecipeWrapper buildFoodTags(KitchenRecipe recipe, ItemStackRecipeWrapper output, List<ItemStackRecipeWrapper> inputs) {
        ItemStack base = output.getItemStack().copy();
        List<String> baseTags = base.get(MIDataComponentTypes.MI_FOOD_PROPERTIES);
        if (baseTags == null) {
            baseTags = new ArrayList<>();
        }
        HashSet<String> propertyIds = new HashSet<>(baseTags);
        List<ItemStackRecipeWrapper> ingredients = recipe.getIngredients();
        List<Item> ingredientItems = ingredients
                .stream()
                .filter(wrapper -> !wrapper.isEmpty())
                .map(ItemStackRecipeWrapper::getItem)
                .toList();
        for (ItemStackRecipeWrapper input : inputs) {
            ItemStack itemStack = input.getItemStack();
            Item item = itemStack.getItem();
            if (ingredientItems.contains(item)) {
                continue;
            }
            List<FoodProperty> ingredientProperties = FoodProperty.getIngredientProperties(item);
            ingredientProperties.forEach(property -> propertyIds.add(property.getId().toString()));

        }
        List<String> tagList = new ArrayList<>(propertyIds);
        base.set(MIDataComponentTypes.MI_FOOD_PROPERTIES, tagList);
        return new ItemStackRecipeWrapper(base.copy());
    }

    private ItemStackRecipeWrapper buildAllFoodTags(ItemStackRecipeWrapper output, List<ItemStackRecipeWrapper> inputs) {
        ItemStack itemStack = output.getItemStack().copy();
        List<String> outputTags = itemStack.get(MIDataComponentTypes.MI_FOOD_PROPERTIES);
        if (outputTags == null) {
            outputTags = new ArrayList<>();
        }
        HashSet<String> propertyIds = new HashSet<>(outputTags);
        for (ItemStackRecipeWrapper wrapper : inputs) {
            ItemStack wrapperItemStack = wrapper.getItemStack();
            if (wrapperItemStack.isEmpty()) {
                continue;
            }
            List<FoodProperty> ingredientProperties = FoodProperty.getIngredientProperties(wrapperItemStack.getItem());
            ingredientProperties.forEach(property -> propertyIds.add(property.getId().toString()));
        }
        List<String> tagList = new ArrayList<>(propertyIds);
        itemStack.set(MIDataComponentTypes.MI_FOOD_PROPERTIES, tagList);
        return new ItemStackRecipeWrapper(itemStack.copy());
    }

    private void handleCrafting(ItemStack output, KitchenRecipe recipe) {
        this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
        SimpleInventory inventory = this.blockEntity.getInventory();
        for (int i = 0; i < 5; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                stack.decrement(1);
            }
        }
        this.blockEntity.setPreOutput(new ItemStackRecipeWrapper(output.copy()));
        this.blockEntity.setTickLeft(recipe.getCostTime() * 20.0);
        Set<KitchenBlockGui<?>> session = KitchenwareBlockEntity.SESSIONS.computeIfAbsent(this.blockEntity.getUuid(), (map) -> new WeakSet<>());
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

        if (this.blockEntity == null || this.blockEntity.getWorld() == null) {
            this.close();
            return;
        }

        for (Integer index : this.displayIndexes) {
            GuiElementBuilder guiElementBuilder = new GuiElementBuilder().setItem(Items.AIR);
            this.displayed.put(index, guiElementBuilder);
            this.setSlot(index, guiElementBuilder);
        }

        Block block = this.blockEntity.getWorld().getBlockState(this.blockEntity.getPos()).getBlock();
        if (!(block instanceof AbstractKitchenwareBlock)) {
            this.close();
            return;
        }

        KitchenRecipeType kitchenRecipeType = getRecipeTypeInstance();
        SimpleInventory inventory = this.blockEntity.getInventory();
        List<ItemStackRecipeWrapper> inputs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            inputs.add(new ItemStackRecipeWrapper(inventory.getStack(i).copy()));
        }

        List<KitchenRecipe> matches = kitchenRecipeType.getMatches(this.recipeType, inputs);
        int recipesPerPage = this.displayed.size();
        this.maxPage = (matches.size() - 1) / recipesPerPage;
        List<KitchenRecipe> pageRecipes = getRecipesForCurrentPage(matches, recipesPerPage);

        // 清空旧显示
        for (GuiElementBuilder builder : this.displayed.values()) {
            builder.setItem(ModGuiItems.EMPTY_SLOT);
        }

        int i = 0;
        for (Map.Entry<Integer, GuiElementBuilder> entry : this.displayed.entrySet()) {
            if (i >= pageRecipes.size()) break;

            KitchenRecipe recipe = pageRecipes.get(i);
            ItemStack output = this.buildFoodTags(recipe, new ItemStackRecipeWrapper(recipe.getOutput().getItemStack().copy()), inputs).getItemStack();

            GuiElementBuilder builder = entry.getValue();
            GuiElementBuilderAccessor accessor = (GuiElementBuilderAccessor) builder;
            accessor.setItemStack(output);
            builder.setCallback((slotIndex, clickType, actionType) -> {
                handleCrafting(output, recipe);
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
        BaseRecipeType<KitchenRecipe> recipeType = MiRecipeManager.KITCHEN_RECIPE;
        return (KitchenRecipeType) recipeType;
    }

    @Override
    public void onClose() {
        this.blockEntity.markDirty();
        super.onClose();
    }
}
