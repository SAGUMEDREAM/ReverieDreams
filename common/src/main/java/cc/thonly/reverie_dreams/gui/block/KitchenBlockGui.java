package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.util.WeakHashSet;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.item.GuiElementBuilderSetter;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
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
    private final KitchenRecipeType.TypeInstance recipeType;
    private final Map<Integer, GuiElementBuilder> displayed = new HashMap<>();
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
        this.setTitle(
                Component.empty()
                         .append(Component.translatable("space.-8"))
                         .append(Component.literal("\ub003")
                                          .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                                                .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                         .append(Component.translatable("space.-168"))
                         .append(Component.translatable(this.block.getDescriptionId()))
        );
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String posChar = GRID[row][col];
                int index = row * 9 + col;

                switch (posChar) {
                    case "X" -> this.setSlot(index, new GuiElementBuilder(Items.AIR));
                    case "N" ->
                            this.setSlot(index, new GuiElementBuilder(RDGuiItems.NEXT.value()).setCallback((i, t, sat, sbg) -> {
                                SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                                if (this.page < this.maxPage) {
                                    this.page++;
                                    this.onTick();
                                }
                            }));
                    case "P" ->
                            this.setSlot(index, new GuiElementBuilder(RDGuiItems.PREV.value()).setCallback((i, t, sat, sbg) -> {
                                SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
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
                    this.blockEntity.throwItem((ServerLevel) blockEntity.getLevel(), itemStack);
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

        KitchenRecipeType kitchenRecipeType = this.getRecipeTypeInstance();
        SimpleContainer inventory = this.blockEntity.getInventory();
        List<IngredientStack> inputs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            inputs.add(new IngredientStack(inventory.getItem(i).copy()));
        }

        List<KitchenRecipe> matches = kitchenRecipeType.getMatches(this.recipeType, inputs);
        int recipesPerPage = this.displayed.size();
        this.maxPage = (matches.size() - 1) / recipesPerPage;
        List<KitchenRecipe> pageRecipes = getRecipesForCurrentPage(matches, recipesPerPage);

        // 清空旧显示
        for (GuiElementBuilder builder : this.displayed.values()) {
            GuiElementBuilderSetter.setter(builder, Items.AIR);
        }

        int i = 0;
        for (Map.Entry<Integer, GuiElementBuilder> entry : this.displayed.entrySet()) {
            if (i >= pageRecipes.size()) break;

            KitchenRecipe recipe = pageRecipes.get(i);
            ItemStack outputShow = ItemUtils.buildFoodTags(recipe, new IngredientStack(recipe.getOutput().build()), inputs).build();
            AtomicReference<ItemStack> output = new AtomicReference<>(outputShow);

            GuiElementBuilder builder = entry.getValue();
            GuiElementBuilderSetter.setter(builder, outputShow);

            builder.setCallback((slotIndex, clickType, input, slotBasedGui) -> {
                ItemStack itemStack = output.get();
                for (CraftingConflict conflict : RegistryImpls.CRAFTING_CONFLICT.values()) {
                    if (conflict.test(itemStack)) {
                        SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_DARK_CUISINE).trigger(this.player);
                        output.set(RDFoodItems.DARK_CUISINE.createStack());
                    }
                }
                if (outputShow.getOrDefault(RDDataComponents.FOOD_PROPERTIES.value(), List.of()).size() >= 5) {
                    SimpleTriggerFactory
                            .create(SimpleTriggerKeys.KITCHEN_COOKING_AMOUNT_OF_5_TAG)
                            .trigger(this.player);
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
    public void onManualClose() {
        super.onManualClose();
        this.blockEntity.setChanged();
    }

}
