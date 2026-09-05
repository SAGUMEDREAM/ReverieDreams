package cc.thonly.reverie_dreams.compat.rrv.strength_table;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.thonly.reverie_dreams.compat.rrv.RRVPlugin;
import cc.thonly.reverie_dreams.compat.rrv.RRVRecipeTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class StrengthTableRecipe implements ReliableClientRecipe {

    private final Identifier id;
    private final cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe recipe;
    private final List<SlotContent> inputs;
    private final SlotContent output;

    public StrengthTableRecipe(Identifier id, cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
        this.inputs = new ArrayList<>();
        this.inputs.add(SlotContent.of(recipe.getMainItem().build()));
        this.inputs.add(SlotContent.of(recipe.getOffItem().build()));
        this.inputs.add(SlotContent.of(recipe.getOffItem().build()));
        this.output = SlotContent.of(recipe.getOutput().build());
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return RRVRecipeTypes.STRENGTH_TABLE_RECIPE_TYPE;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        context.bindOptionalSlot(0, SlotContent.of(this.recipe.getMainItem().build()), RRVPlugin.RENDERER);
        context.bindOptionalSlot(1, SlotContent.of(this.recipe.getOffItem().build()), RRVPlugin.RENDERER);
        context.bindOptionalSlot(2, SlotContent.of(this.recipe.getOutput().build()), RRVPlugin.RENDERER);
    }

    @Override
    public void renderRecipe(RecipeViewScreen screen, RecipePosition recipePosition, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

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
