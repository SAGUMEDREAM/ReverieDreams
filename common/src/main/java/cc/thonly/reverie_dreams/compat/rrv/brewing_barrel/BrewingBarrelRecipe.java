package cc.thonly.reverie_dreams.compat.rrv.brewing_barrel;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.thonly.reverie_dreams.compat.rrv.RRVPlugin;
import cc.thonly.reverie_dreams.compat.rrv.RRVRecipeTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BrewingBarrelRecipe implements ReliableClientRecipe {
    private final Identifier id;
    private final cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe recipe;
    private final List<SlotContent> inputs;
    private final SlotContent output;

    public BrewingBarrelRecipe(
            Identifier id,
            cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe recipe
    ) {
        this.id = id;
        this.recipe = recipe;
        this.inputs = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            if (i < recipe.getMaterials().size()) {
                this.inputs.add(
                        SlotContent.of(
                                recipe.getMaterials().get(i).build()
                        )
                );
            } else {
                this.inputs.add(
                        SlotContent.of(
                                RDGuiPlaceholderItems.EMPTY_ITEM.value()
                        )
                );
            }
        }

        this.output = SlotContent.of(recipe.getOutput().build());
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        for (int i = 0; i < 9; i++) {
            SlotContent input = this.inputs.get(i);

            if (input.isEmpty()) {
                input = SlotContent.of(
                        RDGuiPlaceholderItems.EMPTY_ITEM.value()
                );
            }

            context.bindOptionalSlot(
                    i,
                    input,
                    RRVPlugin.RENDERER
            );
        }

        context.bindOptionalSlot(
                9,
                this.output,
                RRVPlugin.RENDERER
        );
    }

    @Override
    public ReliableClientRecipeType getType() {
        return RRVRecipeTypes.BREWING_BARREL_RECIPE_TYPE;
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