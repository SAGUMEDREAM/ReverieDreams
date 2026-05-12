package cc.thonly.reverie_dreams.gui.item;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayer;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FastRecipeBookGui extends SimpleGui {
    public static final String[][] GRID = {
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"P", "W", "W", "W", "W", "W", "W", "W", "N"},
    };
    private final ItemStack itemStack;
    private final GuiElementBuilder next = new GuiElementBuilder(RDGuiItems.NEXT.createStack()).setItemName(Component.nullToEmpty("Next Page")).setCallback(this::next);
    private final GuiElementBuilder prev = new GuiElementBuilder(RDGuiItems.PREV.createStack()).setItemName(Component.nullToEmpty("Prev Page")).setCallback(this::prev);
    private final List<KitchenRecipe> data = new ArrayList<>();
    private int page = 0;
    private int maxPage = -1;
    private boolean updateNext = false;

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
            }
        }
        this.updateNext = true;
    }

    public List<GuiElementBuilder> getPageContents(int page) {
        List<GuiElementBuilder> list = new ArrayList<>();

        int pageSize = 5 * 9;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, this.data.size());

        for (int i = start; i < end; i++) {
            KitchenRecipe recipe = this.data.get(i);
            GuiElementBuilder builder = new GuiElementBuilder(recipe.getOutput().build());
            Identifier recipeKey = RecipeManager.KITCHEN_TYPE.getRecipeKey(recipe);
            MutableComponent component = Component.empty();
            component.append(" ");
            component.append(Component.translatable(recipe.getType().toTranslateKey()));
            component.append("| ");
            for (IngredientStack ingredient : recipe.getIngredients()) {
                component.append(ingredient.getLazyStack().getHoverName()).append(" ");
            }
            builder.setLore(List.of(component));
            builder.setCallback(() -> {
                SoundEventPlayer.playUISound(this.player, 1.0f, 1.0f);
                this.itemStack.set(RDDataComponents.RECIPE_MEMORY.value(), recipeKey);
                this.updateNext = true;
            });
            if (Objects.equals(this.itemStack.get(RDDataComponents.RECIPE_MEMORY.value()), recipeKey)) {
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
        List<GuiElementBuilder> contents = this.getPageContents(this.page);

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

    private void next() {
        SoundEventPlayer.playUISound(this.player, 1.0f, 1.0f);
        if (this.page < this.maxPage) {
            this.page++;
            this.updateNext = true;
        }
    }

    private void prev() {
        SoundEventPlayer.playUISound(this.player, 1.0f, 1.0f);
        if (this.page > 0) {
            this.page--;
            this.updateNext = true;
        }
    }
}
