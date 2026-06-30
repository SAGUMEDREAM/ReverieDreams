package cc.thonly.reverie_dreams.compat.rrv.danmaku_crafting_table;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.thonly.reverie_dreams.compat.rrv.RRVPlugin;
import cc.thonly.reverie_dreams.compat.rrv.RRVRecipeTypes;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class DanmakuCraftingTableRecipe implements ReliableClientRecipe {
    private final Identifier id;
    private final DanmakuRecipe recipe;
    private final List<SlotContent> inputs = new ArrayList<>();
    private final SlotContent output;

    public DanmakuCraftingTableRecipe(Identifier id, DanmakuRecipe recipe) {
        this.id = id;
        this.recipe = recipe;
        this.inputs.add(SlotContent.of(recipe.getDye().build()));
        this.inputs.add(SlotContent.of(recipe.getCore().build()));
        this.inputs.add(SlotContent.of(recipe.getPower().build()));
        this.inputs.add(SlotContent.of(recipe.getPoint().build()));
        this.inputs.add(SlotContent.of(recipe.getMaterial().build()));
        this.output = SlotContent.of(recipe.getOutput().build());
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
            if (slotContent.isEmpty()) {
                context.bindOptionalSlot(i,  SlotContent.of(RDGuiItems.EMPTY_ITEM.asItem()), RRVPlugin.RENDERER);
                continue;
            }
            context.bindOptionalSlot(i, slotContent, RRVPlugin.RENDERER);
        }
        context.bindOptionalSlot(5, this.output, RRVPlugin.RENDERER);
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return RRVRecipeTypes.DANMAKU_CRAFTING_TABLE;
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
