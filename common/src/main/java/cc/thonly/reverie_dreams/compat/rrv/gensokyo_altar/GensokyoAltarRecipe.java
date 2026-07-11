package cc.thonly.reverie_dreams.compat.rrv.gensokyo_altar;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.thonly.reverie_dreams.compat.rrv.RRVPlugin;
import cc.thonly.reverie_dreams.compat.rrv.RRVRecipeTypes;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class GensokyoAltarRecipe implements ReliableClientRecipe {
    private final Identifier id;
    private final cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe recipe;
    private final List<SlotContent> inputs;
    private final SlotContent output;

    public GensokyoAltarRecipe(Identifier id, cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
        this.inputs = new ArrayList<>();
        this.inputs.add(SlotContent.of(recipe.getCore().build()));
        for (IngredientStack slot : recipe.getSlots()) {
            this.inputs.add(SlotContent.of(slot.build()));
        }
        this.output = SlotContent.of(recipe.getOutput().build());
    }

    @Override
    public ReliableClientRecipeType getType() {
        return RRVRecipeTypes.GENSOKYO_ALTAR_RECIPE_TYPE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        for (int i = 0; i < 8; i++) {
            SlotContent input;
            if (i > this.recipe.getSlots().size() - 1) {
                input = SlotContent.of(RDGuiItems.EMPTY_ITEM.value());
            } else {
                input = SlotContent.of(this.recipe.getSlots().get(i).build());
            }
            if (input.isEmpty()) {
                input = SlotContent.of(RDGuiItems.EMPTY_ITEM.value());
            }
            context.bindOptionalSlot(i, input, RRVPlugin.RENDERER);
        }
        context.bindOptionalSlot(9, SlotContent.of(this.recipe.getCore().build()), RRVPlugin.RENDERER);
        context.bindOptionalSlot(10, this.output, RRVPlugin.RENDERER);
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.copyOf(this.inputs);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.output);
    }
}
