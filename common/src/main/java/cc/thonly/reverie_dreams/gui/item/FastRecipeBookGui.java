package cc.thonly.reverie_dreams.gui.item;

import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class FastRecipeBookGui extends SimpleGui {
    public static final String[][] GRID = {
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"P", "W", "A", "B", "C", "D", "E", "W", "N"},
    };
    private final ItemStack itemStack;
    private final GuiElementBuilder next = new GuiElementBuilder(RDGuiItems.NEXT.createStack()).setItemName(Component.nullToEmpty("Next Page")).setCallback(this::next);
    private final GuiElementBuilder prev = new GuiElementBuilder(RDGuiItems.PREV.createStack()).setItemName(Component.nullToEmpty("Prev Page")).setCallback(this::prev);
    private final List<KitchenRecipe> data = new ArrayList<>();
    private int page = 0;
    private int maxPage = -1;
    private boolean updateNext = false;
    private DeferredBlock selectWork = null;

    public FastRecipeBookGui(ServerPlayer player, ItemStack itemStack) {
        super(MenuType.GENERIC_9x6, player, false);
        this.itemStack = itemStack;
        this.init();
    }

    public void init() {
        this.setTitle(RDItems.FAST_RECIPE_BOOK.createStack().getItemName());
        Map<Identifier, KitchenRecipe> registryView = RecipeManager.KITCHEN_TYPE.getRegistryView();
        this.data.addAll(registryView.values());

        int pageSize = 5 * 9;
        this.maxPage = Math.max(0, (this.data.size() - 1) / pageSize);
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String c = GRID[row][col];
                int slot = row * 9 + col;
                if (c.equalsIgnoreCase("N")) {
                    this.setSlot(slot, this.next);
                }
                if (c.equalsIgnoreCase("P")) {
                    this.setSlot(slot, this.prev);
                }
                if (c.equalsIgnoreCase("W")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(RDGuiItems.EMPTY_SLOT.asItem()));
                }
                if (c.equalsIgnoreCase("A")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(KitchenBlocks.COOKING_POT.asItem()).setCallback(() -> {
                        this.selectWork = KitchenBlocks.COOKING_POT;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
                if (c.equalsIgnoreCase("B")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(KitchenBlocks.CUTTING_BOARD.asItem()).setCallback(() -> {
                        this.selectWork = KitchenBlocks.CUTTING_BOARD;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
                if (c.equalsIgnoreCase("C")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(KitchenBlocks.FRYING_PAN.asItem()).setCallback(() -> {
                        this.selectWork = KitchenBlocks.FRYING_PAN;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
                if (c.equalsIgnoreCase("D")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(KitchenBlocks.GRILL.asItem()).setCallback(() -> {
                        this.selectWork = KitchenBlocks.GRILL;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
                if (c.equalsIgnoreCase("E")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(KitchenBlocks.STEAMER.asItem()).setCallback(() -> {
                        this.selectWork = KitchenBlocks.STEAMER;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
            }
        }
        this.updateNext = true;
    }

    public List<GuiElementBuilder> getPageContents(int page, DeferredBlock blockType) {
        Block block = blockType.asBlock();
        if (!(block instanceof AbstractKitchenwareBlock kitchenwareBlock)) {
            return List.of();
        }
        List<GuiElementBuilder> list = new ArrayList<>();

        int pageSize = 5 * 9;
        int start = page * pageSize;
        List<KitchenRecipe> data = List.copyOf(this.data).stream()
                .filter(recipe -> Objects.equals(
                        recipe.getTypeInstance().defaultBlock(),
                        kitchenwareBlock
                ))
                .sorted(Comparator.comparing(recipe ->
                        BuiltInRegistries.ITEM.getKey(recipe.getOutput().getItem()).toString()
                ))
                .toList();
        int end = Math.min(start + pageSize, data.size());
        this.maxPage = this.computeMaxPage(data);
        this.page = Math.min(this.page, this.maxPage);

        for (int i = start; i < end; i++) {
            KitchenRecipe recipe = data.get(i);
            KitchenRecipe.IdEntry recipeIdEntry = new KitchenRecipe.IdEntry(recipe);
            GuiElementBuilder builder = new GuiElementBuilder(recipe.getOutput().build());
            MutableComponent component = Component.empty();
            component.append(" ");
            component.append(Component.translatable(recipe.getTypeInstance().toTranslateKey()));
            component.append("| ");
            for (IngredientStack ingredient : recipe.getIngredients()) {
                component.append(ingredient.getLazyStack().getHoverName()).append(" ");
            }
            builder.setLore(List.of(component));
            builder.setCallback(() -> {
                SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                this.itemStack.set(RDDataComponents.RECIPE_MEMORY.value(), recipeIdEntry);
                this.updateNext = true;
            });
            if (Objects.equals(this.itemStack.get(RDDataComponents.RECIPE_MEMORY.value()), recipeIdEntry)) {
                builder.glow(true);
            }

            list.add(builder);
        }

        return list;
    }

    public List<GuiElementBuilder> getPageContents(int page) {
        List<GuiElementBuilder> list = new ArrayList<>();
        List<KitchenRecipe> data = this.data.stream().sorted(Comparator.comparing(recipe ->
                BuiltInRegistries.ITEM.getKey(recipe.getOutput().getItem()).toString()
        )).toList();
        int pageSize = 5 * 9;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, data.size());

        for (int i = start; i < end; i++) {
            KitchenRecipe recipe = data.get(i);
            KitchenRecipe.IdEntry recipeIdEntry = new KitchenRecipe.IdEntry(recipe);
            GuiElementBuilder builder = new GuiElementBuilder(recipe.getOutput().build());
            MutableComponent component = Component.empty();
            component.append(" ");
            component.append(Component.translatable(recipe.getTypeInstance().toTranslateKey()));
            component.append("| ");
            for (IngredientStack ingredient : recipe.getIngredients()) {
                component.append(ingredient.getLazyStack().getHoverName()).append(" ");
            }
            builder.setLore(List.of(component));
            builder.setCallback(() -> {
                SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                this.itemStack.set(RDDataComponents.RECIPE_MEMORY.value(), recipeIdEntry);
                this.updateNext = true;
            });
            if (Objects.equals(this.itemStack.get(RDDataComponents.RECIPE_MEMORY.value()), recipeIdEntry)) {
                builder.glow(true);
            }

            list.add(builder);
        }

        return list;
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!this.updateNext) {
            return;
        }
        List<GuiElementBuilder> contents = this.selectWork == null ? this.getPageContents(this.page) : this.getPageContents(this.page, this.selectWork);

        int index = 0;

        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String c = GRID[row][col];
                int slot = row * 9 + col;

                if (c.equalsIgnoreCase("X")) {
                    if (index < contents.size()) {
                        this.setSlot(slot, contents.get(index));
                        index++;
                    } else {
                        this.clearSlot(slot);
                    }
                }
            }
        }
        this.updateNext = false;
    }

    private int computeMaxPage(List<KitchenRecipe> data) {
        int pageSize = 5 * 9;
        return Math.max(0, (data.size() - 1) / pageSize);
    }

    private void next() {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        if (this.page < this.maxPage) {
            this.page++;
            this.updateNext = true;
        }
    }

    private void prev() {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        if (this.page > 0) {
            this.page--;
            this.updateNext = true;
        }
    }
}
