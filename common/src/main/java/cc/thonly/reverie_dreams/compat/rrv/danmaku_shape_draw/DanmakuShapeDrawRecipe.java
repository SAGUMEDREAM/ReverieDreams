package cc.thonly.reverie_dreams.compat.rrv.danmaku_shape_draw;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.thonly.reverie_dreams.compat.rrv.RRVPlugin;
import cc.thonly.reverie_dreams.compat.rrv.RRVRecipeTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import net.minecraft.resources.Identifier;

import java.util.List;

public class DanmakuShapeDrawRecipe implements ReliableClientRecipe {
    private final Identifier id;
    private final cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe recipe;
    private final SlotContent result;

    public DanmakuShapeDrawRecipe(Identifier id, cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
        this.result = SlotContent.of(recipe.getOutput().build());
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        var shape = this.recipe.getShape();
        int idx = 0;
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                boolean state = false;
                if (y < shape.size() && x < shape.get(y).size()) {
                    state = shape.get(y).get(x);
                }
                context.bindSlot(idx, SlotContent.of(state? RDGuiPlaceholderItems.ENABLE.createStack() : RDGuiPlaceholderItems.DISABLE.createStack()));
                idx++;
            }
        }
        context.bindOptionalSlot(idx, this.result, RRVPlugin.RENDERER);
    }

    @Override
    public ReliableClientRecipeType getType() {
        return RRVRecipeTypes.DANMAKU_SHAPE_DRAW_RECIPE_TYPE;
    }

    @Override
    public List<SlotContent> getIngredients() {
        return List.of();
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(this.result);
    }
}
