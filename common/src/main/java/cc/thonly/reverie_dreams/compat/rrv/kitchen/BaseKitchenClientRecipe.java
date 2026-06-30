package cc.thonly.reverie_dreams.compat.rrv.kitchen;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.thonly.reverie_dreams.compat.rrv.RRVPlugin;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BaseKitchenClientRecipe implements ReliableClientRecipe {
    private final Identifier id;
    private final KitchenRecipe recipe;
    private final BaseKitchenClientRecipeType recipeType;
    private final List<SlotContent> inputs = new ArrayList<>();
    private final SlotContent output;

    public BaseKitchenClientRecipe(Identifier id, KitchenRecipe recipe, BaseKitchenClientRecipeType recipeType) {
        this.id = id;
        this.recipe = recipe;
        this.recipeType = recipeType;
        this.inputs.addAll(this.recipe.getIngredients().stream().map(IngredientStack::build).map(SlotContent::of).toList());
        this.output = SlotContent.of(this.recipe.getOutput().build());
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return this.recipeType;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        for (int i = 0; i < 5; i++) {
            if (i > this.inputs.size() - 1) {
                SlotContent slotContent = SlotContent.of(RDGuiItems.EMPTY_ITEM.asItem());
                context.bindOptionalSlot(i, slotContent, RRVPlugin.RENDERER);
                continue;
            }
            SlotContent slotContent = this.inputs.get(i);
            context.bindOptionalSlot(i, slotContent, RRVPlugin.RENDERER);
        }
        context.bindOptionalSlot(5, this.output, RRVPlugin.RENDERER);
        context.bindSlot(6, SlotContent.of(RDGuiItems.PROGRESS_TO_RESULT_DOWN.asItem()));
    }

    @Override
    public List<SlotContent> getIngredients() {
        return new ArrayList<>(this.inputs);
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.output);
    }

}
