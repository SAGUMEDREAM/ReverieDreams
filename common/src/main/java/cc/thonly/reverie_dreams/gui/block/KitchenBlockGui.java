package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.cooking.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import cc.thonly.reverie_dreams.util.WeakHashSet;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.block.Block;

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
    private static final int PREVIOUS_SLOT = 28;
    private static final int STATUS_SLOT = 31;
    private static final int NEXT_SLOT = 34;

    private final Block block;
    private final KitchenwareBlockEntity blockEntity;
    private final KitchenRecipeType.TypeInstance recipeType;
    private final List<Integer> displayIndexes = new ArrayList<>();
    private int page = 0;
    private int maxPage = 0;

    public KitchenBlockGui(Block block, KitchenwareBlockEntity blockEntity, ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.block = block;
        this.blockEntity = blockEntity;
        this.recipeType = blockEntity.getTypeInstance();
        this.init();
    }

    @Override
    public void init() {
        Set<KitchenBlockGui<?>> session = KitchenwareBlockEntity.SESSIONS.computeIfAbsent(this.blockEntity.getUuid(), (map) -> new WeakHashSet<>());
        session.add(this);
        this.displayIndexes.clear();
        this.setTitle(
                Component.empty()
                        .append(Component.translatable("space.-8"))
                        .append(Component.literal("\ub003")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                        .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                        .append(Component.translatable("space.-168"))
                        .append(Component.translatable(this.block.getDescriptionId()).withStyle(ChatFormatting.WHITE))
        );
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String posChar = GRID[row][col];
                int index = row * 9 + col;

                switch (posChar) {
                    case "X" -> this.setSlot(index, new GuiElementBuilder(Items.AIR));
                    case "N", "P" -> { }
                    case "Z" -> this.displayIndexes.add(index);
                    case "O" -> this.setSlot(index, new Slot(this.blockEntity.getInventory(),
                            KitchenwareBlockEntity.OUTPUT_SLOT, 0, 0) {
                        @Override
                        public boolean mayPlace(ItemStack stack) {
                            return false;
                        }
                    });
                    default -> {
                        Integer invIndex = CHAR2INDEX.get(posChar);
                        if (invIndex != null) {
                            this.setSlot(index, new Slot(this.blockEntity.getInventory(), invIndex, 0, 0));
                        }
                    }
                }
            }
        }
    }

    private void handleCrafting(ItemStack output, List<IngredientStack> inputs, KitchenRecipe recipe) {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        SimpleContainer inventory = this.blockEntity.getInventory();
        for (int i = 0; i < 5; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                UseRemainder useRemainderComponent = stack.get(DataComponents.USE_REMAINDER);
                if (useRemainderComponent != null) {
                    ItemStack itemStack = useRemainderComponent.convertInto().create();
                    var pos = this.blockEntity.getBlockPos();
                    Containers.dropItemStack(this.blockEntity.getLevel(), pos.getX(), pos.getY(), pos.getZ(), itemStack);
                }
                stack.shrink(1);
            }
        }
        this.blockEntity.setOutput(new IngredientStack(output.copy()), recipe.getCostTime() * 20.0 + 20 * 0.25 * inputs.size());
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
        if (!this.canUseKitchen()) {
            this.close();
            return;
        }

        List<IngredientStack> inputs = this.readInputs();
        List<KitchenRecipe> matches = this.getRecipeTypeInstance().getMatches(this.recipeType, inputs).stream()
                .sorted(Comparator.comparing(KitchenRecipe::getId))
                .toList();
        int recipesPerPage = this.displayIndexes.size();
        this.maxPage = Math.max(0, (matches.size() - 1) / recipesPerPage);
        this.page = Math.min(this.page, this.maxPage);
        List<KitchenRecipe> pageRecipes = getRecipesForCurrentPage(matches, recipesPerPage);

        for (int index = 0; index < recipesPerPage; index++) {
            int slot = this.displayIndexes.get(index);
            if (index >= pageRecipes.size()) {
                this.clearSlot(slot);
                continue;
            }
            KitchenRecipe recipe = pageRecipes.get(index);
            ItemStack preview = ItemUtils.buildFoodTags(recipe, new IngredientStack(recipe.getOutput().build()), inputs).build();
            GuiElementBuilder builder = new GuiElementBuilder(preview)
                    .addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.kitchen.start", "Click to start cooking"))
                    .addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.kitchen.consume", "Uses one item from each occupied ingredient slot"))
                    .setCallback((slotIndex, clickType, input, slotBasedGui) -> this.startCooking(recipe));
            this.setSlot(slot, builder);
        }

        this.setSlot(STATUS_SLOT, new GuiElementBuilder(matches.isEmpty() ? Items.PAPER : Items.BOOK)
                .setName(matches.isEmpty()
                        ? Component.translatableWithFallback("gui.reverie_dreams.kitchen.no_matches", "No dishes match these ingredients")
                        : Component.translatableWithFallback("gui.reverie_dreams.kitchen.page", "Dishes %s/%s · %s available",
                                this.page + 1, this.maxPage + 1, matches.size()))
                .addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.kitchen.help",
                        "Place ingredients below and select a dish above"))
                .setCallback(() -> SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f)));
        this.setPageControl(PREVIOUS_SLOT, -1);
        this.setPageControl(NEXT_SLOT, 1);
    }

    private boolean canUseKitchen() {
        var level = this.blockEntity.getLevel();
        return level != null && level.getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity
                && level.getBlockState(this.blockEntity.getBlockPos()).getBlock() instanceof AbstractKitchenwareBlock
                && (this.blockEntity.getPreOutput() == null || this.blockEntity.getPreOutput().isEmpty());
    }

    private List<IngredientStack> readInputs() {
        List<IngredientStack> inputs = new ArrayList<>(5);
        for (int slot = 0; slot < 5; slot++) {
            inputs.add(new IngredientStack(this.blockEntity.getInventory().getItem(slot).copy()));
        }
        return inputs;
    }

    private void setPageControl(int slot, int step) {
        if (this.page + step < 0 || this.page + step > this.maxPage) {
            this.clearSlot(slot);
            return;
        }
        var item = step < 0 ? RDGuiPlaceholderItems.PREV.value() : RDGuiPlaceholderItems.NEXT.value();
        this.setSlot(slot, new GuiElementBuilder(item).setCallback((index, click, action, gui) -> {
            this.page += step;
            SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
            this.onTick();
        }));
    }

    private void startCooking(KitchenRecipe selected) {
        if (!this.canUseKitchen()) {
            this.close();
            return;
        }
        List<IngredientStack> inputs = this.readInputs();
        KitchenRecipe recipe = this.getRecipeTypeInstance().getRecipeView(this.recipeType).get(selected.getId());
        if (recipe == null || !this.getRecipeTypeInstance().isMatches(recipe, inputs)) {
            this.onTick();
            return;
        }
        ItemStack preview = ItemUtils.buildFoodTags(recipe, new IngredientStack(recipe.getOutput().build()), inputs).build();
        ItemStack output = preview;
        for (CraftingConflict conflict : BuiltInRegistryProviders.CRAFTING_CONFLICT.values()) {
            if (conflict.test(preview)) {
                SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_DARK_CUISINE).trigger(this.player);
                output = RDCuisineItems.DARK_CUISINE.createStack();
                break;
            }
        }
        if (FoodProperties.get(preview).size() >= 5) {
            SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING_AMOUNT_OF_5_TAG).trigger(this.player);
        }
        SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING).trigger(this.player);
        this.handleCrafting(output, inputs, recipe);
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
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        this.blockEntity.setChanged();
    }

}
